package models;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class AIChatbotService {
    private static final String HUGGINGFACE_API_URL = "https://api-inference.huggingface.co/models/deepset/roberta-base-squad2";
    private static final String HUGGINGFACE_TOKEN = "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";
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    // Fallback responses in case API fails
    private final String[] fallbackResponses = {
        "Je comprends votre question. Pourriez-vous me donner plus de détails ?",
        "Je peux vous aider avec cela. Que souhaitez-vous savoir exactement ?",
        "Pour cette information, je vous suggère de contacter notre service client au 0800-123-456.",
        "Je suis là pour vous aider. Pourriez-vous préciser votre demande ?",
        "Pour les questions concernant la livraison, nous proposons un service standard (2-3 jours) et express (24h).",
        "Pour toute réclamation, veuillez utiliser le formulaire dédié dans l'application."
    };

    public AIChatbotService(String apiKey) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public void clearConversationHistory() {
        // No conversation history needed for HuggingFace QA
    }

    public String generateResponse(String userInput) {
        try {
            // Use HuggingFace QA API with the knowledge base as context
            String context = getKnowledgeBaseContext();
            String answer = askHuggingFaceQA(userInput, context);
            if (answer != null && !answer.trim().isEmpty() && !answer.equalsIgnoreCase("no answer")) {
                return answer;
            }
        } catch (Exception e) {
            // Ignore and fallback
        }
        // Fallback to keyword-based knowledge base
        String kbAnswer = ChatbotKnowledgeBase.searchAnswer(userInput);
        if (kbAnswer != null) return kbAnswer;
        return getFallbackResponse();
    }

    private String askHuggingFaceQA(String question, String context) throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("question", question);
        requestBody.put("context", context);
        String requestBodyJson = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HUGGINGFACE_API_URL))
                .header("Authorization", "Bearer " + HUGGINGFACE_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
            Object answerObj = responseMap.get("answer");
            if (answerObj != null) {
                return answerObj.toString();
            }
        }
        return null;
    }

    private String getKnowledgeBaseContext() {
        // Concatenate all Q&A pairs as context
        StringBuilder sb = new StringBuilder();
        for (String category : ChatbotKnowledgeBase.getAllCategories()) {
            Map<String, String> entries = ChatbotKnowledgeBase.getCategoryKnowledge(category);
            for (String answer : entries.values()) {
                sb.append(answer).append("\n");
            }
        }
        return sb.toString();
    }

    private String getFallbackResponse() {
        return fallbackResponses[random.nextInt(fallbackResponses.length)];
    }
}