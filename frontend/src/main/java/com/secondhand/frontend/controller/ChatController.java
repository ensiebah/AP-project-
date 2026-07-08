package com.secondhand.frontend.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.secondhand.frontend.dto.ConversationDto;
import com.secondhand.frontend.dto.MessageDto;
import com.secondhand.frontend.network.NetworkClient;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;

public class ChatController {

    @FXML private Label chatTitleLabel;
    @FXML private Label adTitleLabel;
    @FXML private ListView<String> messageListView;
    @FXML private TextField messageInputField;

    private ConversationDto currentConversation;
    private final Gson gson = new Gson();
    private Timeline autoRefreshTimeline; // 🟢 تایمر برای چت واقعی و همزمان

    public void setConversationData(ConversationDto conversation) {
        this.currentConversation = conversation;
        if (adTitleLabel != null) adTitleLabel.setText("Ad: " + conversation.getAdvertisementTitle());
        if (chatTitleLabel != null) chatTitleLabel.setText("Chat room ID: " + conversation.getId());

        loadMessages();
        startAutoRefresh(); // 🟢 فعال‌سازی همگام‌سازی چت دوطرفه به محض ورود
    }

    private void loadMessages() {
        if (currentConversation == null) return;

        String jsonResponse = NetworkClient.getConversationMessages(currentConversation.getId());

        if (jsonResponse != null && !jsonResponse.startsWith("ERROR|")) {
            List<MessageDto> messages = gson.fromJson(jsonResponse, new TypeToken<List<MessageDto>>(){}.getType());
            if (messages != null) {
                Platform.runLater(() -> {
                    // برای اینکه اسکرول چت به هم نخورد، فقط اگر تعداد پیام‌ها تغییر کرده لیست را آپدیت می‌کنیم
                    if (messages.size() != messageListView.getItems().size()) {
                        messageListView.getItems().clear();
                        for (MessageDto msg : messages) {
                            messageListView.getItems().add(msg.getSenderUsername() + ": " + msg.getContent());
                        }
                    }
                });
            }
        } else {
            System.err.println("Failed to load messages: " + jsonResponse);
        }
    }

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
                messageListView.getItems().add(sentMessage.getSenderUsername() + ": " + sentMessage.getContent());
                messageInputField.clear();
            });
        } else {
            System.err.println("SERVER ERROR ON SEND: " + jsonResponse);
        }
    }

    /**
     * 🟢 هر ۳ ثانیه یک‌بار تاریخچه پیام‌ها را از بک‌اَند می‌گیرد تا پیام‌های طرف مقابل زنده رندر شوند.
     */
    private void startAutoRefresh() {
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();

        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> loadMessages()));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    /**
     * متدی برای متوقف کردن تایمر زمانی که صفحه چت بسته می‌شود (جلوگیری از نشت حافظه)
     */
    public void shutdown() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
    }
}