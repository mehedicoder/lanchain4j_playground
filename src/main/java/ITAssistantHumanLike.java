import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.time.Duration;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <h1>ITAssistantHumanLike</h1>
 * <p>
 * A terminal-based conversational agent designed to simulate an elite IT consultant.
 * This implementation leverages the <b>LangChain4j</b> framework and <b>Groq's</b>
 * Llama-3 infrastructure to provide low-latency, streaming AI interactions.
 * </p>
 * * <h3>Key Capabilities:</h3>
 * <ul>
 * <li><b>Reactive Streaming:</b> Utilizes {@link TokenStream} to provide real-time, "human-like" typing feedback.</li>
 * <li><b>Stateful Conversation:</b> Implements {@link MessageWindowChatMemory} to retain a sliding window of the last 20 messages.</li>
 * <li><b>Performance Auditing:</b> Includes hooks for benchmarking Time to First Token (TTFT) and total generation latency.</li>
 * </ul>
 * * @see dev.langchain4j.service.AiServices
 * @see dev.langchain4j.model.chat.StreamingChatModel
 */
public class ITAssistantHumanLike {

    /**
     * Declarative interface for the AI Service.
     * <p>
     * LangChain4j proxies this interface at runtime to handle prompt engineering,
     * memory injection, and model communication.
     * </p>
     */
    interface Assistant {
        /**
         * Dispatches a user query to the LLM and returns a reactive stream.
         * * @param message The raw text input provided by the user via terminal.
         * @return A {@link TokenStream} that emits partial response strings as they are generated.
         */
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

    /**
     * Application entry point.
     * Configures the Groq API client and initializes the main command loop.
     */
    public static void main(String[] args) {
        // Build the streaming model with a 120s timeout for complex reasoning tasks
        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("GROQ_API_KEY"))
                .baseUrl("https://api.groq.com/openai/v1")
                .modelName("llama-3.3-70b-versatile")
                .timeout(Duration.ofSeconds(120))
                .maxTokens(1024)
                .build();

        // Instantiate the Assistant service with conversation persistence
        Assistant assistant = AiServices.builder(Assistant.class)
                .streamingChatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();

        Set<String> exitCommands = Set.of("exit", "quit");

        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("--- IT Guru Chatbot Active (Type 'exit' to quit) ---");

            while (true) {
                System.out.print("\nAsk> ");
                String question = scanner.nextLine();

                if (exitCommands.contains(question.toLowerCase().trim())) break;
                if (question.isBlank()) continue;

                executeStreamingChat(assistant, question);
            }
        }
    }

    /**
     * Handles the asynchronous orchestration of the LLM stream.
     * <p>
     * This method blocks the main thread using {@link CompletableFuture#join()} to ensure
     * that the command prompt (Cmd>) does not reappear before the AI has finished its response.
     * </p>
     * * @param assistant The proxied AI Service instance.
     * @param question The user's input string.
     */
    private static void executeStreamingChat(Assistant assistant, String question) {
        AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
        AtomicLong firstTokenTime = new AtomicLong(0);

        // Future used to synchronize the async stream with the synchronous console loop
        CompletableFuture<ChatResponse> future = new CompletableFuture<>();

        assistant.doChat(question)
                .onPartialResponse(token -> {
                    // Record TTFT on the very first token received
                    if (firstTokenTime.get() == 0) {
                        firstTokenTime.set(System.currentTimeMillis());
                    }
                    chatWriter(token);
                })
                .onCompleteResponse(response -> {
                    long endTime = System.currentTimeMillis();
                    // Uncomment to visualize performance stats
                    // printBenchmark(startTime.get(), firstTokenTime.get(), endTime);
                    future.complete(response);
                })
                .onError(future::completeExceptionally)
                .start();

        // Block until the stream is exhausted or an error occurs
        future.join();
    }

    /**
     * Proxy method for the teletype writing effect.
     * * @param chunk The text fragment received from the LLM.
     */
    static void chatWriter(String chunk) {
        // Delegates to the utility method with a 50ms character delay
        ITAssistant.chatWriter(chunk, 50);
    }

    /**
     * Utility for logging performance metrics to standard output.
     * * @param start Epoch millis at request start.
     * @param firstToken Epoch millis when first token was received.
     * @param end Epoch millis when stream was closed.
     */
    private static void printBenchmark(long start, long firstToken, long end) {
        System.out.println("\n\n--- [GURU AUDIT] ---");
        System.out.printf("Time to First Token (TTFT): %d ms%n", (firstToken - start));
        System.out.printf("Total Generation Time:      %d ms%n", (end - start));
        System.out.println("--------------------");
    }
}