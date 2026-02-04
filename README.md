Langchain4j and LLM powered RAG applications!


TextSummarizer (applicable to other console application's code pushed here!) 

TextSummarizer is a Java-based, multithreaded tool that uses LangChain4J and LLMs to summarize text files. It supports multiple summarization levels and languages, is benchmark-enabled, and designed for production use.

Features

Multi-level summarization: extractive, thematic, executive, analytical, and more

Multithreaded for concurrent summarization requests

Summaries in any language

Benchmarking execution time for each summary

Production-ready: environment-based API keys, robust error handling

Requirements

Java 17+

Gradle 7+

API Key for your LLM provider (e.g., Groq API)

IntelliJ IDEA or any IDE with Gradle support

Setup

Clone the repository

git clone <your-repo-url>
cd TextSummarizer


Set your API key as an environment variable

export GROK_API_KEY=<your-api-key>      # Linux/macOS
setx GROK_API_KEY "<your-api-key>"      # Windows


Place text files to summarize in:

src/main/resources/

Running in IntelliJ

Open IntelliJ IDEA and select Open → point to the project root.

Let IntelliJ import the Gradle project.

Ensure your GROK_API_KEY environment variable is set in Run/Debug Configurations.

Run TextSummarizer.java.

Enter:

File> → file name (e.g., example.txt)

Level> → summarization level (e.g., executive)

Language> → output language (e.g., English)

Running from Command Line

Build the project:

./gradlew build


Run the project:

./gradlew run


Input prompts interactively:

File> example.txt
Level> executive
Language> English


The summary and execution time will print in the console.

Supported Summarization Levels

Basic: extractive, compressed, abstractive, thematic, analytical, executive, ultra-brief, structured, audience-specific, comparative

Explicit variants: extractive (verbatim), compressed (concise), abstractive (paraphrased), thematic (conceptual), analytical (interpretive), executive (decision-oriented)

Notes

All input files must be in src/main/resources/.

Multithreaded execution allows multiple summarization requests to run concurrently.

For large-scale summarization, adjust the thread pool in TextSummarizer.java:

private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
Runtime.getRuntime().availableProcessors()
);

License

MIT License – feel free to use and modify for your own projects.