package models;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

public class AIChatbotService {
    private static AIChatbotService instance;
    private final Map<String, String> responses;
    private final Map<Pattern, String> patternResponses;
    private final List<String> conversationHistory;

    private AIChatbotService() {
        responses = new HashMap<>();
        patternResponses = new HashMap<>();
        conversationHistory = new ArrayList<>();
        initializeResponses();
    }

    public static AIChatbotService getInstance() {
        if (instance == null) {
            instance = new AIChatbotService();
        }
        return instance;
    }

    private void initializeResponses() {
        // Réponses directes
        responses.put("bonjour", "Bonjour ! Je suis l'assistant QuickDelivery. Comment puis-je vous aider aujourd'hui ?");
        responses.put("salut", "Salut ! Je suis là pour vous aider avec vos questions sur QuickDelivery.");
        responses.put("aide", "Je peux vous aider avec :\n• Les réclamations\n• Le suivi des colis\n• Les délais de livraison\n• Les tarifs et services\n• Les questions générales\nN'hésitez pas à me poser vos questions !");
        responses.put("merci", "Je vous en prie ! N'hésitez pas si vous avez d'autres questions.");
        responses.put("au revoir", "Au revoir ! N'hésitez pas à revenir si vous avez besoin d'aide.");
        responses.put("hello", "Bonjour ! Comment puis-je vous aider aujourd'hui ?");
        responses.put("hi", "Salut ! Je suis là pour vous aider.");
        responses.put("help", "Je peux vous aider avec :\n• Les réclamations\n• Le suivi des colis\n• Les délais de livraison\n• Les tarifs et services\n• Les questions générales");
        responses.put("thanks", "Je vous en prie ! N'hésitez pas si vous avez d'autres questions.");
        responses.put("bye", "Au revoir ! N'hésitez pas à revenir si vous avez besoin d'aide.");

        // Réponses basées sur des patterns
        // Réclamations
        patternResponses.put(
            Pattern.compile(".*réclamation.*", Pattern.CASE_INSENSITIVE),
            "Pour gérer vos réclamations :\n1. Allez dans la section Support\n2. Cliquez sur 'Nouvelle réclamation' pour en créer une\n3. Ou consultez la liste de vos réclamations existantes"
        );
        patternResponses.put(
            Pattern.compile(".*comment.*créer.*réclamation.*", Pattern.CASE_INSENSITIVE),
            "Pour créer une réclamation :\n1. Allez dans la section Support\n2. Cliquez sur 'Nouvelle réclamation'\n3. Remplissez le formulaire avec les détails de votre problème\n4. Soumettez votre réclamation"
        );
        patternResponses.put(
            Pattern.compile(".*statut.*réclamation.*", Pattern.CASE_INSENSITIVE),
            "Pour vérifier le statut de votre réclamation :\n1. Allez dans la section Support\n2. Consultez la liste de vos réclamations\n3. Le statut est indiqué pour chaque réclamation"
        );

        // Livraisons
        patternResponses.put(
            Pattern.compile(".*suivre.*colis.*|.*suivi.*colis.*", Pattern.CASE_INSENSITIVE),
            "Pour suivre votre colis :\n1. Allez dans la section 'Gérer les commandes'\n2. Trouvez votre commande dans la liste\n3. Cliquez sur 'Suivre' pour voir les détails de livraison"
        );
        patternResponses.put(
            Pattern.compile(".*délai.*livraison.*|.*temps.*livraison.*", Pattern.CASE_INSENSITIVE),
            "Les délais de livraison dépendent de votre localisation :\n• Zone urbaine : 1-2 jours ouvrables\n• Zone rurale : 2-3 jours ouvrables\n• Livraison express : 24h (supplément)"
        );
        patternResponses.put(
            Pattern.compile(".*tarif.*livraison.*|.*prix.*livraison.*", Pattern.CASE_INSENSITIVE),
            "Nos tarifs de livraison :\n• Standard : à partir de 5€\n• Express : à partir de 10€\n• Livraison gratuite à partir de 50€ d'achat"
        );

        // Support technique
        patternResponses.put(
            Pattern.compile(".*problème.*technique.*|.*bug.*|.*erreur.*", Pattern.CASE_INSENSITIVE),
            "Pour les problèmes techniques :\n1. Créez une réclamation dans la section Support\n2. Décrivez le problème en détail\n3. Notre équipe vous répondra dans les plus brefs délais"
        );
        patternResponses.put(
            Pattern.compile(".*contacter.*support.*|.*appeler.*support.*", Pattern.CASE_INSENSITIVE),
            "Vous pouvez contacter notre support :\n• Par email : support@quickdelivery.com\n• Par téléphone : 01 23 45 67 89\n• Via le chat en ligne (24/7)"
        );

        // Questions générales
        patternResponses.put(
            Pattern.compile(".*horaires.*|.*heures.*ouverture.*", Pattern.CASE_INSENSITIVE),
            "Nos horaires de service :\n• Support client : 24/7\n• Livraisons : 8h-20h, du lundi au samedi\n• Service client : 9h-18h, du lundi au vendredi"
        );
        patternResponses.put(
            Pattern.compile(".*zone.*livraison.*|.*où.*livrer.*", Pattern.CASE_INSENSITIVE),
            "Nous livrons dans toute la France métropolitaine. Pour vérifier si nous livrons dans votre zone :\n1. Allez dans la section 'Tarifs'\n2. Entrez votre code postal\n3. Vérifiez la disponibilité"
        );
    }

    public String getResponse(String userInput) {
        if (userInput == null || userInput.trim().isEmpty()) {
            return "Je n'ai pas compris votre message. Pouvez-vous reformuler ?";
        }

        // Nettoyer l'entrée utilisateur
        String cleanInput = userInput.toLowerCase().trim();

        // Ajouter à l'historique
        conversationHistory.add("User: " + userInput);

        // Vérifier les réponses directes
        String directResponse = responses.get(cleanInput);
        if (directResponse != null) {
            conversationHistory.add("Bot: " + directResponse);
            return directResponse;
        }

        // Vérifier les patterns
        for (Map.Entry<Pattern, String> entry : patternResponses.entrySet()) {
            if (entry.getKey().matcher(cleanInput).matches()) {
                String response = entry.getValue();
                conversationHistory.add("Bot: " + response);
                return response;
            }
        }

        // Vérifier la base de connaissances avancée
        String kbResponse = ChatbotKnowledgeBase.searchAnswer(userInput);
        if (kbResponse != null && !kbResponse.trim().isEmpty()) {
            conversationHistory.add("Bot: " + kbResponse);
            return kbResponse;
        }

        // Réponse par défaut
        String defaultResponse = "Je ne suis pas sûr de comprendre. Voici ce que je peux vous aider avec :\n" +
                               "• Les réclamations\n" +
                               "• Le suivi des colis\n" +
                               "• Les délais de livraison\n" +
                               "• Les tarifs et services\n" +
                               "• Le support technique\n" +
                               "Pouvez-vous reformuler votre question ?";
        conversationHistory.add("Bot: " + defaultResponse);
        return defaultResponse;
    }

    public String generateResponse(String userInput) {
        return getResponse(userInput);
    }

    public void clearConversationHistory() {
        conversationHistory.clear();
    }

    public List<String> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }

    public void addCustomResponse(String trigger, String response) {
        responses.put(trigger.toLowerCase(), response);
    }

    public void addPatternResponse(String pattern, String response) {
        patternResponses.put(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE), response);
    }
}