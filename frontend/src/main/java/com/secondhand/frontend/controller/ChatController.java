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
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Conversation UI with persisted seen status.
 * One check means the message was sent; two blue checks mean the recipient
 * opened the conversation and the message was marked as seen by the server.
 */
public class ChatController {

    @FXML private Label chatTitleLabel;
    @FXML private Label adTitleLabel;
    @FXML private ListView<MessageDto> messageListView;
    @FXML private TextField messageInputField;

    private ConversationDto currentConversation;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final AtomicBoolean loadingMessages = new AtomicBoolean(false);

    /**
     * Product decision: the sender sees an updated read tick after reopening
     * this chat. Therefore the first load takes the server state; later auto
     * refreshes keep the tick state already shown in this open chat window.
     */
    private final Map<Long, Boolean> displayedSeenStates = new HashMap<>();

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                @Override
                public void write(JsonWriter writer, LocalDateTime value) throws IOException {
                    if (value == null) {
                        writer.nullValue();
                    } else {
                        writer.value(value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    }
                }

                @Override
                public LocalDateTime read(JsonReader reader) throws IOException {
                    if (reader.peek() == com.google.gson.stream.JsonToken.NULL) {
                        reader.nextNull();
                        return null;
                    }
                    return LocalDateTime.parse(reader.nextString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }
            })
            .create();

    private Timeline autoRefreshTimeline;

    @FXML
    public void initialize() {
        messageListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(MessageDto message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                boolean mine = isMyMessage(message);
                Label content = new Label(message.getContent());
                content.setWrapText(true);
                content.setMaxWidth(340);
                content.getStyleClass().add(mine ? "message-bubble-mine" : "message-bubble-other");

                String time = message.getSentAt() == null ? "" : message.getSentAt().format(timeFormatter);
                Label metadata = new Label(time);
                metadata.getStyleClass().add("message-meta");

                HBox metadataRow = new HBox(5, metadata);
                if (mine) {
                    boolean seen = displayedSeenStates.getOrDefault(message.getId(), message.isSeen());
                    Label tick = new Label(seen ? "✓✓" : "✓");
                    tick.getStyleClass().add(seen ? "message-seen-tick" : "message-sent-tick");
                    metadataRow.getChildren().add(tick);
                }
                metadataRow.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                VBox bubble = new VBox(4, content, metadataRow);
                bubble.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                Region leftSpacer = new Region();
                Region rightSpacer = new Region();
                HBox.setHgrow(leftSpacer, Priority.ALWAYS);
                HBox.setHgrow(rightSpacer, Priority.ALWAYS);

                HBox row = mine
                        ? new HBox(leftSpacer, bubble)
                        : new HBox(bubble, rightSpacer);
                row.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                row.setStyle("-fx-padding: 4 8 4 8;");
                setText(null);
                setGraphic(row);
            }
        });
    }

    public void setConversationData(ConversationDto conversation) {
        currentConversation = conversation;
        displayedSeenStates.clear();
        if (adTitleLabel != null) {
            adTitleLabel.setText("Ad: " + conversation.getAdvertisementTitle());
        }
        if (chatTitleLabel != null) {
            chatTitleLabel.setText("Conversation with " + conversation.getOpponentUsername());
        }
        loadMessages();
        startAutoRefresh();
    }

    /** Fetches messages away from the JavaFX thread, then renders them safely. */
    private void loadMessages() {
        if (currentConversation == null || currentConversation.getId() == null
                || !loadingMessages.compareAndSet(false, true)) {
            return;
        }

        Thread loader = new Thread(() -> {
            String jsonResponse = NetworkClient.getConversationMessages(currentConversation.getId());
            try {
                if (jsonResponse != null && !jsonResponse.startsWith("ERROR")) {
                    List<MessageDto> messages = gson.fromJson(
                            jsonResponse, new TypeToken<List<MessageDto>>() {}.getType()
                    );
                    Platform.runLater(() -> renderMessages(messages == null ? List.of() : messages));
                } else {
                    System.err.println("Failed to sync chat messages: " + jsonResponse);
                }
            } finally {
                loadingMessages.set(false);
            }
        }, "chat-load-thread");
        loader.setDaemon(true);
        loader.start();
    }

    private void renderMessages(List<MessageDto> messages) {
        boolean hasUnreadIncoming = false;
        for (MessageDto message : messages) {
            if (isMyMessage(message)) {
                // Preserve shown state during this open chat. A new ChatController
                // instance (reopen) reads the latest state from the server.
                displayedSeenStates.putIfAbsent(message.getId(), message.isSeen());
            } else if (!message.isSeen()) {
                hasUnreadIncoming = true;
            }
        }
        messageListView.getItems().setAll(messages);
        if (hasUnreadIncoming) {
            markVisibleMessagesAsSeen();
        }
    }

    private boolean isMyMessage(MessageDto message) {
        return message.getSenderUsername() != null
                && message.getSenderUsername().equalsIgnoreCase(NetworkClient.currentUsername);
    }

    /** The receiver has rendered messages; persist that fact asynchronously. */
    private void markVisibleMessagesAsSeen() {
        if (currentConversation == null) {
            return;
        }
        Thread marker = new Thread(
                () -> NetworkClient.markConversationMessagesAsSeen(currentConversation.getId()),
                "chat-seen-marker-thread"
        );
        marker.setDaemon(true);
        marker.start();
    }

    @FXML
    private void handleSendMessage() {
        String text = messageInputField.getText().trim();
        if (text.isEmpty() || currentConversation == null) {
            return;
        }
        messageInputField.clear();
        messageInputField.setDisable(true);

        String jsonBody = gson.toJson(Map.of(
                "conversationId", currentConversation.getId(),
                "content", text
        ));
        Thread sender = new Thread(() -> {
            String jsonResponse = NetworkClient.sendMessage(jsonBody);
            Platform.runLater(() -> {
                messageInputField.setDisable(false);
                if (jsonResponse != null && !jsonResponse.startsWith("ERROR")) {
                    MessageDto sentMessage = gson.fromJson(jsonResponse, MessageDto.class);
                    if (sentMessage != null) {
                        displayedSeenStates.put(sentMessage.getId(), false);
                        messageListView.getItems().add(sentMessage);
                        messageListView.scrollTo(sentMessage);
                    }
                } else {
                    messageInputField.setText(text);
                    System.err.println("Server transmission failure: " + jsonResponse);
                }
            });
        }, "chat-send-thread");
        sender.setDaemon(true);
        sender.start();
    }

    private void startAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
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
