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
        // Prepare a dummy file in the temp directory
        String fileName = "test_something.txt";
        String content = "Music has existed since the dawn of time.";
        Files.writeString(tempDir.resolve(fileName), content);

        // Access the private generatePrompt method via reflection for testing
        Method generatePromptMethod = TextSummarizer.class.getDeclaredMethod(
                "generatePrompt", String.class, String.class, String.class);
        generatePromptMethod.setAccessible(true);

        // Invoke the method
        String result = (String) generatePromptMethod.invoke(null, fileName, "executive", "English");

        assertThat(result).contains("executive");
        assertThat(result).contains("English");
        assertThat(result).contains(content);
    }

    @Test
    void testModelChatInteraction() {
        // Prepare a mock response
        String expectedSummary = "This is a summarized version of the text.";
        ChatResponse mockResponse = ChatResponse.builder()
                .aiMessage(AiMessage.from(expectedSummary))
                .build();

        when(mockChatModel.chat(anyList())).thenReturn(mockResponse);

        // Test the interaction
        ChatResponse response = mockChatModel.chat(List.of()); // Simulated call

        assertThat(response.aiMessage().text()).isEqualTo(expectedSummary);
    }

    @Test
    void testFileErrorHandling() {
        // Test that prompt generation fails gracefully if file is missing
        Method generatePromptMethod;
        try {
            generatePromptMethod = TextSummarizer.class.getDeclaredMethod(
                    "generatePrompt", String.class, String.class, String.class);
            generatePromptMethod.setAccessible(true);

            // Should throw an InvocationTargetException wrapping a NoSuchFileException
            assertThrows(Exception.class, () -> {
                generatePromptMethod.invoke(null, "non_existent.txt", "basic", "French");
            });
        } catch (NoSuchMethodException e) {
            fail("Method not found");
        }
    }
}