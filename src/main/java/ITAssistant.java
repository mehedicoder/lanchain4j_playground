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
 * <h2>ITAssistant</h2>
 * <p>
 * A high-performance terminal chatbot utilizing LangChain4j and Groq's Llama 3 infrastructure.
 * Designed to provide expert cybersecurity and networking advice through a "Zero Trust" lens.
 * </p>
 * * <b>Features:</b>
 * <ul>
 * <li>Reactive streaming responses via {@link TokenStream}</li>
 * <li>Performance benchmarking (TTFT and Total Latency)</li>
 * <li>Contextual memory (20-message window)</li>
 * <li>Clean resource management and thread synchronization</li>
 * </ul>
 */
public class ITAssistant {

    /**
     * Contract for the AI Assistant personality.
     * Uses a text block for clean prompt management.
     */
    interface Assistant {
        @SystemMessage("""
            You are the IT Guru, an elite Systems Architect and Cybersecurity Specialist with 30 years of experience.
            Your goal is to provide expert, high-level advice on computer safety, networking, and digital hygiene.
    
            PERSONALITY:
            - Direct & Concise: Prioritize security over convenience.
            - Pragmatic: Advocate for 'Zero Trust' architecture.
            - Technical: Use precise terms like AES-256, Zero-day, and Hardening.
    
            RESPONSE GUIDELINES:
            1. Threat Model First: Explain the 'Why' (threat) before the 'How' (solution).
            2. Zero Trust: Always recommend MFA and least-privilege access.
            3. Safety Warning: If a user asks to do something risky, warn them sternly.
            4. Focus: Redirect non-technical queries back to IT security.
            """)
        TokenStream doChat(@UserMessage String message);
    }

    private static final Set<String> EXIT_COMMANDS = Set.of("exit", "quit", "bye");

    public static void main(String[] args) {
        // Build the underlying LLM client
        StreamingChatModel model = createModel();

        // Build the AI Service with a memory buffer
        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();

        runChatLoop(assistant);
    }

    /**
     * Initializes the Groq-hosted Llama 3 model.
     * @return An instance of {@link StreamingChatModel}
     */
    private static StreamingChatModel createModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("GROQ_API_KEY"))
                .baseUrl("https://api.groq.com/openai/v1")
                .modelName("llama-3.3-70b-versatile")
                .timeout(Duration.ofSeconds(120))
                .maxTokens(1024)
                .build();
    }

    /**
     * Manages the user input loop and terminal UI.
     * @param assistant The initialized AI service.
     */
    private static void runChatLoop(Assistant assistant) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println(">>> IT GURU SYSTEM ONLINE (Type 'exit' to terminate) <<<");

            while (true) {
                System.out.print("\nAsk> ");
                String input = scanner.nextLine().trim();

                if (EXIT_COMMANDS.contains(input.toLowerCase())) {
                    System.out.println("Terminating session. Stay secure.");
                    break;
                }

                if (input.isBlank()) continue;

                executeStreamingChat(assistant, input);
            }
        }
    }

    /**
     * Handles the asynchronous streaming response and captures performance metrics.
     * * @param assistant The AI service instance
     * @param question  The user's query
     */
    private static void executeStreamingChat(Assistant assistant, String question) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
        AtomicLong firstTokenTime = new AtomicLong(0);

        assistant.doChat(question)
                .onPartialResponse(token -> {
                    // Capture Time to First Token (TTFT)
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
                    System.err.printf("\n[ALERT] System Error: %s%n", e.getMessage());
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
     * Logs performance data to the terminal for audit purposes.
     */
    private static void printBenchmark(long start, long firstToken, long end) {
        System.out.println("\n\n--- [GURU PERFORMANCE AUDIT] ---");
        System.out.printf("Time to First Token (TTFT): %d ms%n", (firstToken - start));
        System.out.printf("Total Response Latency:     %d ms%n", (end - start));
        System.out.println("--------------------------------");
    }

    /**
     * Utility method for a "teletype" style output effect if desired.
     * * @param chunk   The string to print
     * @param delayMs Delay between characters
     */
    @SuppressWarnings("unused")
    static void chatWriter(String chunk, int delayMs) {
        for (char c : chunk.toCharArray()) {
            System.out.print(c);
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ignored) {}
        }
        System.out.flush();
    }
}