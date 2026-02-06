import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.time.Duration;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ITAssistantChatbot is a terminal-based AI consultant powered by LangChain4j and Groq.
 * It demonstrates streaming LLM responses, chat memory persistence, and basic performance benchmarking.
 */
public class ITAssistantChatbot {

    /**
     * The AI Service interface defining the personality and contract for the LLM.
     */
    interface Assistant {
        /**
         * Streams a chat response based on user input.
         * @param message The raw text input from the user.
         * @return A TokenStream for reactive handling of the LLM response.
         */
        @SystemMessage("You are the IT Guru! Give expert advice on how to stay safe while using computers and the internet.")
        TokenStream doChat(@UserMessage String message);
    }

    public static void main(String[] args) {
        // Initialize the Streaming Model via Groq's OpenAI-compatible endpoint
        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("GROQ_API_KEY"))
                .baseUrl("https://api.groq.com/openai/v1")
                .modelName("llama-3.3-70b-versatile")
                .timeout(Duration.ofSeconds(120))
                .maxTokens(1024)
                .build();

        // Build the AI Service with 20-message sliding window memory
        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();

        Set<String> exitCommands = Set.of("exit", "quit");

        // Use try-with-resources to ensure the Scanner closes properly
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("--- IT Guru Chatbot Active (Type 'exit' to quit) ---");

            while (true) {
                System.out.print("\nCmd> ");
                String question = scanner.nextLine();

                if (exitCommands.contains(question.toLowerCase().trim())) break;
                if (question.isBlank()) continue;

                executeStreamingChat(assistant, question);
            }
        }
    }

    /**
     * Executes the chat request and benchmarks the performance.
     * @param assistant The initialized AI service.
     * @param question The user's query.
     */
    private static void executeStreamingChat(Assistant assistant, String question) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
        AtomicLong firstTokenTime = new AtomicLong(0);

        assistant.doChat(question)
                .onPartialResponse(token -> {
                    if (firstTokenTime.get() == 0) {
                        firstTokenTime.set(System.currentTimeMillis());
                    }
                    System.out.print(token);
                })
                .onCompleteResponse(response -> {
                    long endTime = System.currentTimeMillis();
                    printBenchmark(startTime.get(), firstTokenTime.get(), endTime);
                    latch.countDown();
                })
                .onError(e -> {
                    System.err.println("\nError: " + e.getMessage());
                    latch.countDown();
                })
                .start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Prints simple performance metrics to the console.
     */
    private static void printBenchmark(long start, long firstToken, long end) {
        System.out.println("\n\n--- Performance Metrics ---");
        System.out.printf("Time to First Token (TTFT): %d ms%n", (firstToken - start));
        System.out.printf("Total Generation Time:      %d ms%n", (end - start));
        System.out.println("---------------------------");
    }
}