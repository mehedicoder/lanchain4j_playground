import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * SecurityIncidentDiagnostic: An IT Security Incident Response tool using LangChain4j.
 * This class uses a Chain-of-Thought (CoT) approach to diagnose security vulnerabilities
 * and suggest immediate remediation steps.
 */
public class SecurityIncidentDiagnosticChainOfThoughts {

    // Define the core security logic and instructions as a static template
    private static final String SYSTEM_PROMPT = """
            Role: You are a Senior Tier-3 SOC (Security Operations Center) Analyst.
            Task: Perform a technical root cause analysis for a security incident.
            
            Instructions:
            Follow this three-step reasoning process for every response:
            1. Extract Indicators: Identify technical assets, attack vectors (e.g., protocols, IPs, or patterns), and anomaly types from the background.
            2. Threat Analysis: Explain the logical relationship. Why does Component A (the vulnerability) lead to Outcome B (the alert/breach) under Condition C (the specific trigger)?
            3. Containment & Remediation: Provide a concise final answer including the likely exploit type (e.g., SQLi, RCE, XSS) and the first technical step to stop the threat.
            """;

    public static void main(String[] args) {

        //Define the Security Incident Background
        String incidentBackground = """
                Background:
                Our Java-based customer portal is experiencing unusual outbound traffic to an unknown IP 
                after the deployment of a new 'LoggingModule' dependency. SIEM logs show unauthorized 
                LDAP lookups originating from the web server. The activity occurs only when user 
                input containing '${' characters is processed. Internal systems are still reachable, 
                but data exfiltration is suspected.
                """;

        String taskDescription = """
                Task:
                Identify the likely exploit type and the first technical containment step.
                """;

        // Combine the instructions, background, and task into a single prompt
        String finalPrompt = String.format("%s\n%s\n%s", SYSTEM_PROMPT, incidentBackground, taskDescription);

        //Initialize the ChatModel (Configured for Groq/Llama-3)
        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("GROQ_API_KEY")) // Ensure this environment variable is set
                .baseUrl("https://api.groq.com/openai/v1")
                .modelName("llama-3.3-70b-versatile")
                .build();

        //Execute the call and display the structured security analysis
        System.out.println("--- IT Security Incident Analysis ---");
        String response = chatModel.chat(finalPrompt);
        System.out.println(response);
    }
}