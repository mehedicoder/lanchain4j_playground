# TextSummarizer

> **Part of the LangChain4j and LLM-powered RAG applications suite.**

TextSummarizer is a Java-based, multithreaded tool that leverages **LangChain4j** and Large Language Models (LLMs) to process and summarize text files. Designed for both laboratory experimentation and production environments, it features robust error handling, benchmarking, and multi-language support.

---

## 🚀 Features

* **Multi-level Summarization:** Supports various depths including extractive, thematic, executive, analytical, and more.
* **High Performance:** Multithreaded architecture for concurrent summarization of multiple files.
* **Global Reach:** Generate summaries in any target language (English, German, Italian, etc.).
* **Benchmarking:** Built-in execution time tracking for performance analysis.
* **Production-Ready:** Uses environment-based API keys and failsafe error handling.

---

## 🛠 Requirements

* **Java:** 17+
* **Build Tool:** Gradle 7+
* **API Access:** Groq API Key (Recommended for speed and regional availability in the EU).
* **IDE:** IntelliJ IDEA (preferred) or any Java-compliant IDE.

---

## ⚙️ Setup

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/mehedicoder/lanchain4j_playground
    cd TextSummarizer
    ```

2.  **Set your API key as an environment variable:**
    * **Linux/macOS:**
        ```bash
        export GROQ_API_KEY="your-api-key-here"
        ```
    * **Windows:**
        ```bash
        setx GROQ_API_KEY "your-api-key-here"
        ```

3.  **Add your documents:**
    Place the `.txt` files you wish to summarize in:
    `src/main/resources/`

---

## 💻 Running the Application

### Via IntelliJ IDEA
1.  Open IntelliJ and select **Open** → point to the project root.
2.  Allow IntelliJ to import the **Gradle** project.
3.  Go to `Run/Debug Configurations` and add the `GROQ_API_KEY` to the **Environment Variables** field.
4.  Run `TextSummarizer.java`.

### Via Command Line
1.  **Build the project:**
    ```bash
    ./gradlew build
    ```
2.  **Run the project:**
    ```bash
    ./gradlew run
    ```

### Interactive Prompts
When prompted in the console, enter your details:
* **File>** `example.txt`
* **Level>** `executive`
* **Language>** `English`

---

## 📊 Supported Summarization Levels

| Level | Description |
| :--- | :--- |
| **Extractive** | Verbatim selection of key sentences. |
| **Compressed** | Concise version of the main ideas. |
| **Abstractive** | Paraphrased and reformulated content. |
| **Thematic** | Focuses on conceptual themes and motifs. |
| **Analytical** | Interpretive breakdown of the text's logic. |
| **Executive** | Decision-oriented summary for leadership. |

---

## 📝 Technical Notes

* **File Location:** All input files must reside in `src/main/resources/`.
* **Concurrency:** To adjust the processing power, modify the thread pool in `TextSummarizer.java`:
    ```java
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );
    ```

---

## 📄 License

This project is licensed under the **MIT License** – feel free to use and modify it for your own personal or commercial projects.