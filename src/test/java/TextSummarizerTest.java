import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

class TextSummarizerTest {

    private ChatModel mockChatModel;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mockChatModel = Mockito.mock(ChatModel.class);
    }

    @Test
    void testGeneratePromptLogic() throws Exception {
        // 1. Create the EXACT sub-directories the code is looking for: src/main/resources
        Path resourceDir = tempDir.resolve("src").resolve("main").resolve("resources");
        Files.createDirectories(resourceDir);

        // 2. Write the file inside that specific nested folder
        String fileName = "test_something.txt";
        String content = "Music has existed since the dawn of time.";
        Path file = resourceDir.resolve(fileName);
        Files.writeString(file, content);

        // 3. Point the JVM's "working directory" to our tempDir
        String originalUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", tempDir.toAbsolutePath().toString());

        try {
            Method generatePromptMethod = TextSummarizer.class.getDeclaredMethod(
                    "generatePrompt", String.class, String.class, String.class);
            generatePromptMethod.setAccessible(true);

            // 4. Pass ONLY the filename.
            // The code will prepend "src/main/resources/" and find it in tempDir
            String result = (String) generatePromptMethod.invoke(null, fileName, "executive", "English");

            assertThat(result).contains("executive");
            assertThat(result).contains("English");
            assertThat(result).contains(content);
        } finally {
            // 5. Cleanup the system property so other tests don't break
            System.setProperty("user.dir", originalUserDir);
        }
    }

    @Test
    void testModelChatInteraction() {
        String expectedSummary = "This is a summarized version of the text.";
        ChatResponse mockResponse = ChatResponse.builder()
                .aiMessage(AiMessage.from(expectedSummary))
                .build();

        when(mockChatModel.chat(anyList())).thenReturn(mockResponse);

        ChatResponse response = mockChatModel.chat(List.of());

        assertThat(response.aiMessage().text()).isEqualTo(expectedSummary);
    }

    @Test
    void testFileErrorHandling() {
        Method generatePromptMethod;
        try {
            generatePromptMethod = TextSummarizer.class.getDeclaredMethod(
                    "generatePrompt", String.class, String.class, String.class);
            generatePromptMethod.setAccessible(true);

            assertThrows(Exception.class, () -> {
                // Pass a name that definitely doesn't exist in that hardcoded path
                generatePromptMethod.invoke(null, "non_existent_file_123.txt", "basic", "French");
            });
        } catch (NoSuchMethodException e) {
            fail("Method not found");
        }
    }
}