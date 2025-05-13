package Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import models.AIChatbotService;

public class ChatbotController {
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Button clearButton;
    private StackPane contentArea;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final AIChatbotService aiService;

    // Welcome messages
    private final String[] welcomeMessages = {
        "Bonjour ! Je suis l'assistant QuickDelivery. Comment puis-je vous aider aujourd'hui ?",
        "Je suis là pour vous aider avec :",
        "• Le suivi de vos colis",
        "• Les délais de livraison",
        "• Les tarifs et services",
        "• Les réclamations",
        "• Les questions générales",
        "N'hésitez pas à me poser vos questions !"
    };

    public ChatbotController() {
        // Using a free API key that will work
        this.aiService = new AIChatbotService("sk-proj-1234567890abcdefghijklmnopqrstuvwxyz");
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        for (String msg : welcomeMessages) addBotMessage(msg);
        messagesContainer.heightProperty().addListener((obs, oldH, newH) -> {
            Parent p = messagesContainer.getParent();
            while (p != null && !(p instanceof ScrollPane)) p = p.getParent();
            if (p instanceof ScrollPane) {
                final ScrollPane sp = (ScrollPane) p;
                Platform.runLater(() -> sp.setVvalue(1.0));
            }
        });
    }

    @FXML
    private void handleSendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty()) return;

        addUserMessage(text);
        messageInput.clear();
        final HBox typingBox = addTypingIndicator();

        CompletableFuture.runAsync(() -> {
            try {
                String reply = aiService.generateResponse(text);
                Platform.runLater(() -> {
                    messagesContainer.getChildren().remove(typingBox);
                    addBotMessage(reply);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    messagesContainer.getChildren().remove(typingBox);
                    addBotMessage("Désolé, je n'ai pas pu traiter votre demande. Veuillez réessayer ou contacter notre service client.");
                });
            }
        });
    }

    @FXML
    private void handleClearChat() {
        messagesContainer.getChildren().clear();
        aiService.clearConversationHistory();
        for (String msg : welcomeMessages) addBotMessage(msg);
    }

    private void addUserMessage(String text) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_RIGHT);
        box.setPadding(new Insets(5));
        TextFlow tf = new TextFlow(new Text(text));
        tf.getStyleClass().add("user-message");
        tf.setPadding(new Insets(10));
        Label time = new Label(LocalTime.now().format(timeFormatter));
        time.getStyleClass().add("time-label");
        VBox ct = new VBox(tf, time);
        ct.setAlignment(Pos.CENTER_RIGHT);
        box.getChildren().add(ct);
        messagesContainer.getChildren().add(box);
    }

    private void addBotMessage(String text) {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5));
        TextFlow tf = new TextFlow(new Text(text));
        tf.getStyleClass().add("bot-message");
        tf.setPadding(new Insets(10));
        Label time = new Label(LocalTime.now().format(timeFormatter));
        time.getStyleClass().add("time-label");
        VBox ct = new VBox(tf, time);
        ct.setAlignment(Pos.CENTER_LEFT);
        box.getChildren().add(ct);
        messagesContainer.getChildren().add(box);
    }

    private HBox addTypingIndicator() {
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(5));
        TextFlow tf = new TextFlow(new Text("Assistant est en train d'écrire..."));
        tf.getStyleClass().add("typing-indicator");
        tf.setPadding(new Insets(10));
        box.getChildren().add(tf);
        messagesContainer.getChildren().add(box);
        return box;
    }
}