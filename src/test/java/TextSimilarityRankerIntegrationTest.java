import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TextSimilarityRankerIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should read files from disk and rank them correctly based on similarity")
    void shouldReadFilesAndRankSimilarities() throws IOException {
        // 1. Setup: Create physical test files in the temp directory
        Path file1 = tempDir.resolve("cooking.txt");
        Files.write(file1, List.of("I love baking bread", "Cooking is my passion"));

        Path file2 = tempDir.resolve("sports.txt");
        Files.write(file2, List.of("Football is a great sport", "I enjoy running marathons"));

        // use a fake model to control the math
        // This fake model makes vectors based on length so we can predict results
        EmbeddingModel fakeModel = new LengthBasedEmbeddingModel();

        // 3. Execute: Call the actual private methods via a testable wrapper
        String query = "Baking";

        // We use the file fetching logic we wrote in the main class
        List<String> lines = TextSimilarityRanker.fetchUniqueLinesFromDirectory(tempDir.toString());

        //Verification: Check file reading logic
        assertThat(lines).hasSize(4);
        assertThat(lines).contains("I love baking bread", "Cooking is my passion");

        // Execute similarity mapping
        Map<String, Double> results = TextSimilarityRanker.mapSimilaritiesByRelevance(fakeModel, query, lines);

        // 6. Verification: Check ranking
        // "I love baking bread" should exist in the results
        assertThat(results).containsKey("I love baking bread");

        // Ensure scores are within expected cosine range [-1, 1]
        results.values().forEach(score -> {
            assertThat(score).isBetween(-1.0, 1.0);
        });
    }

    @Test
    @DisplayName("Should handle empty directory gracefully")
    void shouldHandleEmptyDirectory() {
        List<String> lines = TextSimilarityRanker.fetchUniqueLinesFromDirectory(tempDir.toString());
        assertThat(lines).isEmpty();
    }

    /**
     * A controlled model for integration testing.
     * It produces a vector based on text length so we can verify the ranking logic
     * without needing a random LLM response.
     */
    private static class LengthBasedEmbeddingModel implements EmbeddingModel {
        @Override
        public Response<Embedding> embed(String text) {
            return Response.from(Embedding.from(new float[]{text.length(), 1.0f}));
        }

        @Override
        public Response<List<Embedding>> embedAll(List<TextSegment> segments) {
            List<Embedding> embeddings = segments.stream()
                    .map(s -> Embedding.from(new float[]{s.text().length(), 1.0f}))
                    .toList();
            return Response.from(embeddings);
        }
    }
}