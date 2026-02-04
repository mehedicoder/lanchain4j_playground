import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

/**
 * TextSummarizer provides an interactive CLI tool to summarize text files
 * using an LLM. Users specify the file, summarization level, and output language.
 *
 * <p>Enhanced version: multithreaded, benchmark-enabled, and scalable.
 * Can handle multiple summarization requests concurrently.
 */
public class TextSummarizer {

    private static final String RESOURCE_DIR = System.getProperty("user.dir") + "/src/main/resources/";

    // Thread pool for concurrent summarization requests
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    public static void main(String[] args) throws IOException {
        SystemMessage systemMessage = SystemMessage.from("You are an expert text summarizer.");

        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("GROK_API_KEY"))
                .baseUrl("https://api.groq.com/openai/v1")
                .modelName("llama-3.3-70b-versatile")
                .build();

        List<ChatMessage> systemMessages = List.of(systemMessage);

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String fileName = getUserInput(scanner, "File> ");
            String summaryLevel = getUserInput(scanner, "Level> ");
            String language = getUserInput(scanner, "Language> ");

            if (fileName.isBlank() || summaryLevel.isBlank() || language.isBlank()) {
                System.out.println("All inputs are required. Please try again.");
                continue;
            }

            // Submit summarization task to thread pool
            EXECUTOR.submit(() -> {
                long startTime = System.nanoTime();
                try {
                    String promptText = generatePrompt(fileName, summaryLevel, language);
                    UserMessage userMessage = UserMessage.from(promptText);

                    List<ChatMessage> messages = new ArrayList<>(systemMessages);
                    messages.add(userMessage);

                    ChatResponse response = chatModel.chat(messages);

                    long durationMs = (System.nanoTime() - startTime) / 1_000_000;
                    System.out.println("\n--- Summary ---\n" + response.aiMessage().text());
                    System.out.println(String.format("[Benchmark] Execution time: %d ms\n", durationMs));
                } catch (IOException e) {
                    System.err.println("Error reading file: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error during summarization: " + e.getMessage());
                }
            });
        }
    }

    /**
     * Generates a prompt string for the LLM based on file content, summarization level,
     * and language.
     *
     * @param fileName     The name of the file to summarize.
     * @param summaryLevel The summarization level (e.g., extractive, thematic).
     * @param language     The language for the summary.
     * @return A fully populated prompt string ready for the LLM.
     * @throws IOException If the file cannot be read.
     */
    private static String generatePrompt(String fileName, String summaryLevel, String language) throws IOException {
        String template = """
                Please create a summary from the following text at a {{level}} level,
                using a clear, concise paragraph that captures the core ideas,
                emphasizes key themes, and provides actionable insights.
                Respond in {{language}}. {{file}}
                """;

        PromptTemplate promptTemplate = PromptTemplate.from(template);

        Map<String, Object> variables = new HashMap<>();
        variables.put("level", summaryLevel);
        variables.put("language", language);
        Path filePath = Path.of(RESOURCE_DIR + fileName);
        variables.put("file", Files.readString(filePath));

        Prompt prompt = promptTemplate.apply(variables);
        return prompt.text();
    }

    /**
     * Reads input from the user.
     *
     * @param scanner       Scanner instance.
     * @param promptMessage Message to display.
     * @return Trimmed user input.
     */
    private static String getUserInput(Scanner scanner, String promptMessage) {
        System.out.print(promptMessage);
        return scanner.nextLine().trim();
    }
}