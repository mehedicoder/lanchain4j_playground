import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

import java.time.Duration;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <h1>ITAssistantEnhancedHumanLike</h1>
 * <p>
 * An elite IT Consultant simulator that combines deep technical knowledge with
 * a stochastic typing engine to provide a realistic, human-like interaction experience.
 * </p>
 * * <h3>Core Enhancements:</h3>
 * <ul>
 * <li><b>Stochastic Typing:</b> Implements variable delays and contextual pausing (punctuation/whitespace)
 * to mimic human cadence rather than mechanical output.</li>
 * <li><b>Synchronous UX:</b> Orchestrates asynchronous LLM streams into a stable CLI flow
 * using {@link CompletableFuture}.</li>
 * <li><b>Zero-Trust Persona:</b> Strict system prompting to ensure safe, expert-level IT advice.</li>
 * </ul>
 * * @author IT Supervisor
 */
public class ITAssistantEnhancedHumanLike {

    private static final Set<String> EXIT_COMMANDS = Set.of("exit", "quit", "bye");

    /**
     * AI Service contract for the IT Guru.
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

    public static void main(String[] args) {
        Assistant assistant = initializeAssistant();
        runConversationLoop(assistant);
    }

    /**
     * Initializes the LangChain4j service with Groq/Llama-3 and Chat Memory.
     */
    private static Assistant initializeAssistant() {
        StreamingChatModel model = OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("GROQ_API_KEY"))
                .baseUrl("https://api.groq.com/openai/v1")
                .modelName("llama-3.3-70b-versatile")
                .timeout(Duration.ofSeconds(120))
                .maxTokens(1024)
                .build();

        return AiServices.builder(Assistant.class)
                .streamingChatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(20))
                .build();
    }

    /**
     * Standard CLI loop for user interaction.
     */
    private static void runConversationLoop(Assistant assistant) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("--- [ IT GURU SYSTEM ONLINE ] ---");

            while (true) {
                System.out.print("\nAsk> ");
                String question = scanner.nextLine().trim();

                if (EXIT_COMMANDS.contains(question.toLowerCase())) break;
                if (question.isBlank()) continue;

                executeStreamingChat(assistant, question);
            }
        }
    }

    /**
     * Executes the LLM request and applies the typing effect.
     * Captures performance metrics for TTFT (Time to First Token).
     */
    private static void executeStreamingChat(Assistant assistant, String question) {
        AtomicLong startTime = new AtomicLong(System.currentTimeMillis());
        AtomicLong firstTokenTime = new AtomicLong(0);
        CompletableFuture<Void> future = new CompletableFuture<>();

        assistant.doChat(question)
                .onPartialResponse(token -> {
                    if (firstTokenTime.get() == 0) {
                        firstTokenTime.set(System.currentTimeMillis());
                    }
                    stochasticPrint(token);
                })
                .onCompleteResponse(response -> {
                    long endTime = System.currentTimeMillis();
                    // printBenchmark(startTime.get(), firstTokenTime.get(), endTime);
                    future.complete(null);
                })
                .onError(err -> {
                    System.err.println("\n[ERROR]: " + err.getMessage());
                    future.complete(null);
                })
                .start();

        future.join(); // Blocks CLI prompt until typing is complete
    }

    /**
     * Stochastic Typing Engine.
     * Mimics human typing by varying delays based on character context and randomness.
     * * @param chunk The text fragment emitted by the LLM.
     */
    private static void stochasticPrint(String chunk) {
        Random random = ThreadLocalRandom.current();

        for (char c : chunk.toCharArray()) {
            System.out.print(c);
            System.out.flush();

            try {
                // Base typing speed (40-80ms per char)
                int delay = 40 + random.nextInt(40);

                // Contextual Delays
                if (c == '.' || c == '?' || c == '!') {
                    delay += 350; // Thoughts end with a pause
                } else if (c == ',' || c == ':' || c == ';') {
                    delay += 150; // Mid-sentence breath
                } else if (Character.isWhitespace(c)) {
                    delay += 30;  // Natural gap between words
                }

                // Stochastic Burst: 10% chance to type very fast (muscle memory simulation)
                if (random.nextDouble() > 0.90) {
                    delay = 10;
                }

                Thread.sleep(delay);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Performance audit logger.
     */
    private static void printBenchmark(long start, long firstToken, long end) {
        System.out.println("\n\n--- [PERFORMANCE AUDIT] ---");
        System.out.printf("Time to First Token: %d ms%n", (firstToken - start));
        System.out.printf("Total Latency:       %d ms%n", (end - start));
        System.out.println("---------------------------");
    }
}