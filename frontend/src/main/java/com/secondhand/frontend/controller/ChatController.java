package com.secondhand.frontend.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.secondhand.frontend.dto.ConversationDto;
import com.secondhand.frontend.dto.MessageDto;
import com.secondhand.frontend.network.NetworkClient;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Controller responsible for managing the chat window between two users.
 * <p>
 * It loads conversation messages, sends new messages to the server,
 * refreshes the chat periodically, and updates the JavaFX interface.
 *
 * @author Ensie
 * @version 1.0
 */
public class ChatController {

    @FXML private Label chatTitleLabel;
    @FXML private Label adTitleLabel;
    @FXML private ListView<String> messageListView;
    @FXML private TextField messageInputField;

    private ConversationDto currentConversation;

    // فرمت‌کننده ساعت و دقیقه (مثال: 14:35)
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                @Override
                public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
                    if (localDateTime == null) {
                        jsonWriter.nullValue();
                    } else {
                        jsonWriter.value(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                }

                @Override
                public LocalDateTime read(JsonReader jsonReader) throws IOException {
                    if (jsonReader.peek() == com.google.gson.stream.JsonToken.NULL) {
                        jsonReader.nextNull();
                        return null;
                    } else {
                        return LocalDateTime.parse(jsonReader.nextString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }
                }
            })
            .create();

    private Timeline autoRefreshTimeline;

    public void setConversationData(ConversationDto conversation) {
        this.currentConversation = conversation;
        if (adTitleLabel != null) adTitleLabel.setText("Ad: " + conversation.getAdvertisementTitle());
        if (chatTitleLabel != null) chatTitleLabel.setText("Chat Room ID: " + conversation.getId());

        loadMessages();
        startAutoRefresh();
    }

    /**
     * Retrieves all messages belonging to the current conversation from the backend
     * and refreshes the message list if new messages are available.
     */
    private void loadMessages() {
        if (currentConversation == null || currentConversation.getId() == null) return;

        String jsonResponse = NetworkClient.getConversationMessages(currentConversation.getId());

        if (jsonResponse != null && !jsonResponse.startsWith("ERROR|")) {
            List<MessageDto> messages = gson.fromJson(jsonResponse, new TypeToken<List<MessageDto>>(){}.getType());
            if (messages != null) {
                Platform.runLater(() -> {
                    if (messages.size() != messageListView.getItems().size()) {
                        messageListView.getItems().clear();
                        for (MessageDto msg : messages) {
                            String sender = msg.getSenderUsername() != null ? msg.getSenderUsername() : "User " + msg.getSenderId();

                            // فرمت کردن زمان برای پیام‌های قدیمی دریافتی از سرور
                            String timeStr = "";
                            if (msg.getSentAt() != null) {
                                timeStr = "[" + msg.getSentAt().format(timeFormatter) + "] ";
                            }

                            messageListView.getItems().add(timeStr + sender + ": " + msg.getContent());
                        }
                    }
                });
            }
        } else {
            System.err.println("Failed to sync chat messages: " + jsonResponse);
        }
    }

    /**
     * Sends the user's message to the backend server and immediately displays
     * the successfully delivered message inside the chat window.
     */
    @FXML
    private void handleSendMessage() {
        String text = messageInputField.getText().trim();
        if (text.isEmpty() || currentConversation == null) return;

        Map<String, Object> body = Map.of(
                "conversationId", currentConversation.getId(),
                "content", text
        );
        String jsonBody = gson.toJson(body);
        String jsonResponse = NetworkClient.sendMessage(jsonBody);

        if (jsonResponse != null && !jsonResponse.startsWith("ERROR")) {
            MessageDto sentMessage = gson.fromJson(jsonResponse, MessageDto.class);
            Platform.runLater(() -> {
                String sender = (sentMessage != null && sentMessage.getSenderUsername() != null) ?
                        sentMessage.getSenderUsername() : "Me";

                // فرمت کردن زمان برای پیامی که همین الان به صورت آنی ارسال شد
                String timeStr = "";
                if (sentMessage != null && sentMessage.getSentAt() != null) {
                    timeStr = "[" + sentMessage.getSentAt().format(timeFormatter) + "] ";
                } else {
                    timeStr = "[" + LocalDateTime.now().format(timeFormatter) + "] ";
                }

                messageListView.getItems().add(timeStr + sender + ": " + text);
                messageInputField.clear();
            });
        } else {
            System.err.println("Server transmission failure: " + jsonResponse);
        }
    }

    /**
     * Starts an automatic refresh task that periodically synchronizes
     * the conversation with the backend server.
     */
    private void startAutoRefresh() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();

        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> loadMessages()));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    public void shutdown() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
    }

    @FXML
    public void handleCloseWindow() {
        shutdown();
        javafx.stage.Stage stage = (javafx.stage.Stage) messageListView.getScene().getWindow();
        stage.close();
    }
}