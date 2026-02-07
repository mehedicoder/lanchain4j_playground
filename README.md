# 🤖 LangChain4j AI Playground

> **A suite of high-performance, LLM-powered Java applications for Cybersecurity Consulting and Document Intelligence.**

This repository contains a collection of enterprise-grade tools built with **LangChain4j**, leveraging the **Groq Llama-3** infrastructure for ultra-fast inference. Whether you need an elite IT security consultant or a scalable document summarizer, this playground provides the blueprints.

---

## 🚀 Applications in this Suite

### 1. 🛡️ IT Guru Chatbot (`ITAssistant`)
A terminal-based AI consultant designed as an elite Systems Architect.
* **Persona:** Zero-Trust expert with 30 years of experience.
* **Features:** Streaming responses, contextual chat memory (20 messages), and real-time performance benchmarking.
* **Best for:** Quick security audits, networking advice, and learning digital hygiene.

### 2. 🎭 Human-Like IT Assistant (`ITAssistantHumanLike`)
An evolution of the IT Guru that focuses on the user experience.
* **Features:** Simulates human typing speeds (Teletype effect) while maintaining full asynchronous processing via `CompletableFuture`.
* **Best for:** Applications requiring a more natural, engaging "live chat" feel.

### 3. 📄 Multithreaded Text Summarizer (`TextSummarizer`)
A scalable tool for processing large volumes of text files.
* **Features:** Multithreaded execution, dynamic prompt templating, and support for 10+ summarization levels.
* **Best for:** Processing reports, research papers, and executive briefings.

---

## 🛠 Requirements

* **Java:** 17+ (Uses Text Blocks and Records)
* **Build Tool:** Gradle 7+ or Maven
* **API Access:** Groq API Key (Set as `GROQ_API_KEY`)
* **Environment:** Linux, macOS, or Windows

---

## ⚙️ Setup & Installation

1.  **Clone the Repository:**
    ```bash
    git clone [https://github.com/mehedicoder/lanchain4j_playground](https://github.com/mehedicoder/lanchain4j_playground)
    cd lanchain4j_playground
    ```

2.  **Configure Environment Variables:**
    The applications pull your API key from the system environment for security.
    * **Windows (PowerShell):** `$env:GROQ_API_KEY="your_gsk_key_here"`
    * **Windows (CMD):** `set GROQ_API_KEY=your_gsk_key_here`
    * **Linux/macOS:** `export GROQ_API_KEY="your_gsk_key_here"`

3.  **Prepare Resources:**
    For the **TextSummarizer**, place your `.txt` files in:
    `src/main/resources/`

---

## 💻 How to Use

### Using the IT Guru Chatbots
Run `ITAssistant.java` or `ITAssistantHumanLike.java`.
* **Interaction:** Type your IT or security questions directly into the `Ask>` prompt.
* **Benchmarking:** After every response, the system outputs **TTFT** (Time to First Token) and **Total Latency**.
* **Termination:** Type `exit` or `quit` to end the session.

### Using the Text Summarizer
Run `TextSummarizer.java`.
* **Concurrent Processing:** Submit multiple files in a row; the `ExecutorService` handles them in the background.
* **Interactive Prompt Sequence:**
    1.  **File>:** `example.txt`
    2.  **Level>:** `executive` (See table below for options)
    3.  **Language>:** `English`

---

## 📊 Summary Level Reference

| Level | Goal | Output Style |
| :--- | :--- | :--- |
| **Extractive** | Accuracy | Verbatim sentences from the source. |
| **Executive** | Action | High-level, decision-oriented bullet points. |
| **Thematic** | Understanding | Focuses on core concepts and underlying motifs. |
| **Analytical** | Insight | Interprets the "why" and "how" behind the text. |
| **Ultra-brief** | Speed | A single, high-impact sentence. |

---

## 📝 Technical Architecture

### **Concurrency & Async**
* **Streaming:** Uses LangChain4j `TokenStream` for reactive UI updates.
* **Synchronization:** Uses `CountDownLatch` or `CompletableFuture` to coordinate between the async LLM responses and the synchronous CLI.
* **Thread Pooling:** `TextSummarizer` utilizes a `FixedThreadPool` based on `availableProcessors()` to ensure the UI remains responsive.

### **Prompt Engineering**
All tools use **Java Text Blocks** for clean, maintainable prompt management. The **IT Guru** persona is strictly enforced via `@SystemMessage` to ensure "Zero-Trust" security principles are always prioritized.

---

## 📄 License
This project is licensed under the **MIT License**.