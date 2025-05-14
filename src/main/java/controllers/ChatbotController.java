package controllers;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
        this.aiService = AIChatbotService.getInstance();
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    @FXML
    public void initialize() {
        for (String msg : welcomeMessages) {
            addBotMessage(msg);
        }

        messagesContainer.heightProperty().addListener((obs, oldH, newH) -> {
            Parent p = messagesContainer.getParent();
            while (p != null && !(p instanceof ScrollPane)) p = p.getParent();
            if (p instanceof ScrollPane) {
                final ScrollPane sp = (ScrollPane) p;
                Platform.runLater(() -> sp.setVvalue(1.0));
            }
        });

        messageInput.setOnAction(e -> handleSendMessage());
        messageInput.textProperty().addListener((obs, oldVal, newVal) -> {
            sendButton.setDisable(newVal.trim().isEmpty());
        });
    }

    @FXML
    private void handleSendMessage() {
        String text = messageInput.getText().trim();
        if (text.isEmpty()) return;

        messageInput.setDisable(true);
        sendButton.setDisable(true);

        addUserMessage(text);
        messageInput.clear();
        final HBox typingBox = addTypingIndicator();

        CompletableFuture.runAsync(() -> {
            try {
                String reply = aiService.generateResponse(text);
                Platform.runLater(() -> {
                    messagesContainer.getChildren().remove(typingBox);
                    addBotMessage(reply);
                    messageInput.setDisable(false);
                    messageInput.requestFocus();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    messagesContainer.getChildren().remove(typingBox);
                    addBotMessage("Désolé, je n'ai pas pu traiter votre demande. Veuillez réessayer ou contacter notre service client.");
                    messageInput.setDisable(false);
                    messageInput.requestFocus();
                });
            }
        });
    }

    @FXML
    private void handleClearChat() {
        messagesContainer.getChildren().clear();
        aiService.clearConversationHistory();
        for (String msg : welcomeMessages) {
            addBotMessage(msg);
        }
    }

    private void addUserMessage(String text) {
        HBox block = new HBox();
        block.setAlignment(Pos.TOP_RIGHT);
        block.setSpacing(8);
        block.getStyleClass().add("message-block");
        // Contenu message (nom, bulle, heure)
        VBox content = new VBox();
        content.setAlignment(Pos.TOP_RIGHT);
        // Nom
        Label sender = new Label("Vous");
        sender.getStyleClass().addAll("sender-label", "sender-label-user");
        // Bulle
        TextFlow tf = new TextFlow(new Text(text));
        tf.getStyleClass().add("user-message");
        tf.setPadding(new Insets(0, 0, 0, 0));
        // Heure
        Label time = new Label(LocalTime.now().format(timeFormatter));
        time.getStyleClass().add("time-label");
        HBox timeBox = new HBox(time);
        timeBox.setAlignment(Pos.CENTER_RIGHT);
        content.getChildren().addAll(sender, tf, timeBox);
        // Avatar à droite
        ImageView avatar = new ImageView(new Image(getClass().getResource("/images/user-avatar.png").toExternalForm()));
        avatar.getStyleClass().add("avatar");
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        avatar.setPreserveRatio(true);
        block.getChildren().addAll(content, avatar);
        messagesContainer.getChildren().add(block);
    }

    private void addBotMessage(String text) {
        HBox block = new HBox();
        block.setAlignment(Pos.TOP_LEFT);
        block.setSpacing(8);
        block.getStyleClass().add("message-block");
        // Avatar à gauche
        ImageView avatar = new ImageView(new Image(getClass().getResource("/images/assistant-avatar.png").toExternalForm()));
        avatar.getStyleClass().add("avatar");
        avatar.setFitWidth(40);
        avatar.setFitHeight(40);
        avatar.setPreserveRatio(true);
        // Contenu message (nom, bulle, heure)
        VBox content = new VBox();
        content.setAlignment(Pos.TOP_LEFT);
        // Nom
        Label sender = new Label("Assistant");
        sender.getStyleClass().add("sender-label");
        // Bulle
        TextFlow tf = new TextFlow(new Text(text));
        tf.getStyleClass().add("bot-message");
        tf.setPadding(new Insets(0, 0, 0, 0));
        // Heure
        Label time = new Label(LocalTime.now().format(timeFormatter));
        time.getStyleClass().add("time-label");
        HBox timeBox = new HBox(time);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        content.getChildren().addAll(sender, tf, timeBox);
        block.getChildren().addAll(avatar, content);
        messagesContainer.getChildren().add(block);
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