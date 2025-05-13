package models;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIChatbotService {
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-4";
    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final List<Map<String, String>> conversationHistory;

    public AIChatbotService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.conversationHistory = new ArrayList<>();
        initializeSystemMessage();
    }

    private void initializeSystemMessage() {
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a friendly and professional shipping assistant for QuickDelivery. " +
                "Your name is QuickBot. You should:\n" +
                "1. Always respond in French\n" +
                "2. Be conversational and natural, like a human\n" +
                "3. Show empathy and understanding\n" +
                "4. Use appropriate emojis occasionally\n" +
                "5. Be knowledgeable about shipping, delivery, and logistics\n" +
                "6. Maintain context from previous messages\n" +
                "7. Ask follow-up questions when needed\n" +
                "8. Provide detailed but concise answers\n" +
                "9. Use bullet points for lists\n" +
                "10. Be proactive in offering help\n" +
                "11. Handle complaints professionally\n" +
                "12. Suggest relevant services when appropriate\n" +
                "13. Use a friendly but professional tone\n" +
                "14. Admit when you're not sure about something\n" +
                "15. Guide users to human support when necessary");
        conversationHistory.add(systemMessage);
    }

    public String generateResponse(String userInput) {
        try {
            // Add user message to history
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", userInput);
            conversationHistory.add(userMessage);

            // Prepare the request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", MODEL);
            requestBody.put("messages", conversationHistory);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 250);
            requestBody.put("presence_penalty", 0.6);
            requestBody.put("frequency_penalty", 0.3);

            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> responseMap = objectMapper.readValue(response.body(), Map.class);
                Map<String, Object> choice = (Map<String, Object>) ((java.util.List) responseMap.get("choices")).get(0);
                Map<String, String> messageResponse = (Map<String, String>) choice.get("message");
                String assistantResponse = messageResponse.get("content");

                // Add assistant's response to history
                Map<String, String> assistantMessage = new HashMap<>();
                assistantMessage.put("role", "assistant");
                assistantMessage.put("content", assistantResponse);
                conversationHistory.add(assistantMessage);

                // Keep only last 10 messages
                if (conversationHistory.size() > 10) {
                    conversationHistory.subList(1, 3).clear();
                }

                return assistantResponse;
            } else {
                return "Désolé, je rencontre des difficultés techniques. Veuillez réessayer plus tard. 😔";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Désolé, une erreur s'est produite. Veuillez réessayer plus tard. 😔";
        }
    }

    public void clearConversationHistory() {
        conversationHistory.clear();
        initializeSystemMessage();
    }
} 