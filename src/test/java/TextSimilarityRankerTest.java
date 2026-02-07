import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TextSimilarityRankerTest {

    @Test
    @DisplayName("Cosine Similarity should return 1.0 for identical vectors")
    void similarityOfIdenticalVectors() {
        float[] vector = {1.0f, 0.0f, 0.0f};
        double result = TextSimilarityRanker.calculateCosineSimilarity(vector, vector);
        assertThat(result).isCloseTo(1.0, within(0.0001));
    }

    @Test
    @DisplayName("Cosine Similarity should return 0.0 for orthogonal vectors")
    void similarityOfOrthogonalVectors() {
        float[] vectorA = {1.0f, 0.0f};
        float[] vectorB = {0.0f, 1.0f};
        double result = TextSimilarityRanker.calculateCosineSimilarity(vectorA, vectorB);
        assertThat(result).isCloseTo(0.0, within(0.0001));
    }

    @Test
    @DisplayName("Calculated similarity matches known geometric results ([3,4] and [5,12])")
    void similarityOfKnownVectors() {
        // Known result for these vectors is approx 0.9692
        float[] vectorA = {3.0f, 4.0f};
        float[] vectorB = {5.0f, 12.0f};

        double result = TextSimilarityRanker.calculateCosineSimilarity(vectorA, vectorB);
        assertThat(result).isCloseTo(0.9692, within(0.0001));
    }

    @Test
    @DisplayName("Should handle zero vectors safely without crashing")
    void handleZeroVectors() {
        float[] vectorA = {0.0f, 0.0f};
        float[] vectorB = {1.0f, 2.0f};

        double result = TextSimilarityRanker.calculateCosineSimilarity(vectorA, vectorB);
        assertThat(result).isEqualTo(0.0);
    }

    /**
     * This test demonstrates how to test your logic using a Mock/Fake model
     * so you don't need to have Ollama running during your build process.
     */
    @Test
    @DisplayName("Integration check: Batch embedding works with Fake Model")
    void mockModelIntegrationTest() {
        EmbeddingModel fakeModel = new FakeEmbeddingModel();
        String query = "test";
        List<String> docs = List.of("apple", "orange");

        // We are checking if the method executes and returns the correct map size
        var results = TextSimilarityRanker.mapSimilaritiesByRelevance(fakeModel, query, docs);

        assertThat(results).hasSize(2);
        assertThat(results.keySet()).contains("apple", "orange");
    }

    /**
     * A simple Fake Model for testing purposes.
     * It returns a static vector based on string length.
     */
    private static class FakeEmbeddingModel implements EmbeddingModel {
        @Override
        public Response<Embedding> embed(String text) {
            return Response.from(Embedding.from(new float[]{text.length(), 1.0f}));
        }

        @Override
        public Response<List<Embedding>> embedAll(List<dev.langchain4j.data.segment.TextSegment> segments) {
            List<Embedding> embeddings = segments.stream()
                    .map(s -> Embedding.from(new float[]{s.text().length(), 1.0f}))
                    .toList();
            return Response.from(embeddings);
        }
    }
}