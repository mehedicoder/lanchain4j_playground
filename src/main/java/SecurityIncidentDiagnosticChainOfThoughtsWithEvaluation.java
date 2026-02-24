import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

/**
 * SecurityIncidentDiagnostic: An IT Security Incident Response tool using LangChain4j.
 * This class uses a Chain-of-Thought (CoT) approach with a secondary "Jury" LLM 
 * to validate the analysis before final output.
 */
public class SecurityIncidentDiagnosticChainOfThoughtsWithEvaluation {

    // Analyst Logic
    private static final String ANALYST_PROMPT = """
            Role: You are a Senior Tier-3 SOC (Security Operations Center) Analyst.
            Task: Perform a technical root cause analysis for a security incident.
            
            Instructions:
            Follow this three-step reasoning process for every response:
            1. Extract Indicators: Identify technical assets, attack vectors, and anomaly types.
            2. Threat Analysis: Explain the logical relationship. Why does Component A lead to Outcome B under Condition C?
            3. Containment & Remediation: Provide a concise final answer including the likely exploit type and the first technical step.
            """;

    // Jury Logic
    private static final String JURY_PROMPT_TEMPLATE = """
            Role: You are an IT Security Auditor and Quality Judge.
            Task: Evaluate the provided Security Analysis for accuracy and quality.
            
            Evaluation Criteria:
            - Well-organized: Does it strictly follow the 3-step process?
            - Clear: Is the logic technical and free of vague language?
            - Concise: Does it avoid unnecessary filler text?
            - Actionable: Is the containment step specific enough to execute?

            Analysis to Evaluate:
            ---
            %s
            ---

            Output Format:
            Start your response with 'VERDICT: [APPROVED/REJECTED]'. 
            Then, provide a brief bulleted list explaining your decision.
            """;

    public static void main(String[] args) {
        //Initialize the ChatModel
        ChatModel chatModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("GROQ_API_KEY"))
                .baseUrl("https://api.groq.com/openai/v1")
                .modelName("llama-3.3-70b-versatile")
                .build();

        //Define the Security Incident Background
        String incidentBackground = """
                Background:
                Our Java-based customer portal is experiencing unusual outbound traffic to an unknown IP 
                after the deployment of a new 'LoggingModule' dependency. SIEM logs show unauthorized 
                LDAP lookups originating from the web server. The activity occurs only when user 
                input containing '${' characters is processed. Internal systems are still reachable, 
                but data exfiltration is suspected.
                """;

        String taskDescription = "Task: Identify the likely exploit type and the first technical containment step.";

        //GENERATION
        String initialPrompt = String.format("%s\n%s\n%s", ANALYST_PROMPT, incidentBackground, taskDescription);
        String analystReport = chatModel.chat(initialPrompt);

        System.out.println("PHASE 1: SOC ANALYST REPORT\n");
        System.out.println(analystReport);
        System.out.println("\n" + "=".repeat(50) + "\n");

        //EVALUATION (The Jury)
        String juryPrompt = String.format(JURY_PROMPT_TEMPLATE, analystReport);
        String juryVerdict = chatModel.chat(juryPrompt);

        System.out.println("PHASE 2: JURY VERDICT");
        System.out.println(juryVerdict);

        //FINAL DECISION
        System.out.println("\n" + "=".repeat(50));
        if (juryVerdict.trim().startsWith("VERDICT: APPROVED")) {
            System.out.println("[RESULT] Analysis verified. Proceeding with remediation.");
        } else {
            System.out.println("[RESULT] Analysis rejected. Check for logic errors or hallucinations.");
        }
    }
}