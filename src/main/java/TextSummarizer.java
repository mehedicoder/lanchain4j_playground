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

/**
 * TextSummarizer provides an interactive CLI tool to summarize text files
 * using an LLM. Users specify the file, summarization level, and output language.
 *
 * <p>Supported summarization levels include:
 * extractive, compressed, abstractive, thematic, analytical, executive,
 * ultra-brief, structured, audience-specific, and comparative.
 *
 * <p>The class is designed for production use, reading API keys from environment variables
 * and handling file input from a standard resources directory.
 */
public class TextSummarizer {

    private static final String RESOURCE_DIR = System.getProperty("user.dir") + "/src/main/resources/";

    public static void main(String[] args) throws IOException {
        // System-level instructions for the LLM
        SystemMessage systemMessage = SystemMessage.from("You are an expert text summarizer.");

        // Initialize the chat model with credentials from environment
        ChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("GROK_API_KEY"))
                .baseUrl("https://api.groq.com/openai/v1")
                .modelName("llama-3.3-70b-versatile")
                .build();

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(systemMessage);

        while (true) {
            String fileName = getUserInput("File> ");
            String summarizationLevel = getUserInput("Level> ");
            String language = getUserInput("Language> ");

            // Skip iteration if any input is missing
            if (fileName.isBlank() || summarizationLevel.isBlank() || language.isBlank()) {
                System.out.println("All inputs are required. Please try again.");
                continue;
            }

            UserMessage userMessage = UserMessage.from(
                    genPrompt(fileName, summarizationLevel, language)
            );
            messages.add(userMessage);

            ChatResponse response = model.chat(messages);
            System.out.println("\n--- Summary ---\n" + response.aiMessage().text() + "\n");

            // Clear user message to allow new input without duplicating system message
            messages.remove(userMessage);
        }
    }

    /**
     * Generates a prompt string for the LLM based on the file content, summarization level,
     * and desired output language.
     *
     * @param fileName       The name of the text file located in the resources directory.
     * @param summaryLevel   The requested summarization level (e.g., extractive, thematic, executive).
     * @param language       The language in which the summary should be returned.
     * @return A fully populated prompt string ready to send to the LLM.
     * @throws IOException If the file cannot be read from the resources directory.
     */
    public static String genPrompt(String fileName, String summaryLevel, String language) throws IOException {
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
     * Prompts the user for input from the command line.
     *
     * @param promptMessage The message displayed to guide the user.
     * @return The trimmed input entered by the user, or an empty string if blank.
     */
    public static String getUserInput(String promptMessage) {
        System.out.print(promptMessage);
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().trim();
    }
}
