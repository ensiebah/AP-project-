package com.secondhand.frontend.controller;

import com.secondhand.frontend.dto.ConversationDto;
import com.secondhand.frontend.dto.MessageDto;
import com.secondhand.frontend.network.NetworkClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.List;
import java.util.Map;

public class ChatController {

    @FXML private Label chatTitleLabel;
    @FXML private Label adTitleLabel;
    @FXML private ListView<String> messageListView;
    @FXML private TextField messageInputField;

    private ConversationDto currentConversation;
    private final Gson gson = new Gson();

    public void setConversationData(ConversationDto conversation) {
        this.currentConversation = conversation;
        if (adTitleLabel != null) adTitleLabel.setText("Ad: " + conversation.getAdvertisementTitle());
        if (chatTitleLabel != null) chatTitleLabel.setText("Chat room ID: " + conversation.getId());

        loadMessages();
    }

    private void loadMessages() {
        messageListView.getItems().clear();
        String jsonResponse = NetworkClient.getConversationMessages(currentConversation.getId());

        if (jsonResponse != null && !jsonResponse.startsWith("ERROR|")) {
            List<MessageDto> messages = gson.fromJson(jsonResponse, new TypeToken<List<MessageDto>>(){}.getType());
            for (MessageDto msg : messages) {
                messageListView.getItems().add(msg.getSenderUsername() + ": " + msg.getContent());
            }
        }
    }

    @FXML
    private void handleSendMessage() {
        String text = messageInputField.getText().trim();
        if (text.isEmpty()) return;

        // ساخت قالب بدنه JSON مورد نیاز بک‌اَند
        Map<String, Object> body = Map.of(
                "conversationId", currentConversation.getId(),
                "content", text
        );
        String jsonBody = gson.toJson(body);

        String jsonResponse = NetworkClient.sendMessage(jsonBody);

        if (jsonResponse != null && !jsonResponse.startsWith("ERROR|")) {
            MessageDto sentMessage = gson.fromJson(jsonResponse, MessageDto.class);
            Platform.runLater(() -> {
                messageListView.getItems().add(sentMessage.getSenderUsername() + ": " + sentMessage.getContent());
                messageInputField.clear();
            });
        } else {
            System.err.println("Could not send message: " + jsonResponse);
        }
    }
}