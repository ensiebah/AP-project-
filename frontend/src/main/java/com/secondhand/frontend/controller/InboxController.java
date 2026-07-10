package com.secondhand.frontend.controller;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.secondhand.frontend.dto.ConversationDto;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
public class InboxController {
    @FXML private ListView<ConversationDto> conversationListView ;
    private final Gson gson = new Gson() ;

    @FXML
    public void initialize(){
        conversationListView.setCellFactory(param->new ListCell<>(){
            @Override
            protected void updateItem(ConversationDto item , boolean empty){
                super.updateItem(item , empty);
                if (empty || item == null){
                    setText(null);
                    setGraphic(null);
                } else {
                    String lastMsg = (item.getLastMessageContent() != null ) ? item.getLastMessageContent() : "No messages yet";
                    if (lastMsg.length() > 30) lastMsg = lastMsg.substring(0 , 27) + "..." ;
                    String cellText = String.format(
                            item.getAdvertisementTitle(),
                            item.getOpponentUsername() ,
                            lastMsg
                    ) ;
                    setText(cellText);
                    setStyle("-fx-padding: 10; -fx-border-color: #f1f2f6; -fx-border-width: 0 0 1 0;");
                }
            }
        }) ;

        conversationListView.setOnMouseClicked(event ->
        {
            if (event.getClickCount() == 2){
                ConversationDto selected = conversationListView.getSelectionModel().getSelectedItem() ;
                if (selected != null){
                    NavigationUtils.openChatBox(selected);
                }
            }
        });
        Platform.runLater(this::loadUserConversations);
    }

    @FXML
    private void loadUserConversations(){
        conversationListView.getItems().clear();

        Thread thread = new Thread(()-> {
            String jsonResponse = NetworkClient.getMyChats() ;
            System.out.println("Inbox Server Response: "+jsonResponse);
            if (jsonResponse != null && !jsonResponse.startsWith("ERROR")){
                try {
                    JSONArray array = new JSONArray(jsonResponse) ;
                    List<ConversationDto> convList = new ArrayList<>() ;

                    for (int i = 0 ; i < array.length() ; i++){
                        JSONObject obj = array.getJSONObject(i);
                        ConversationDto dto = new ConversationDto() ;
                        dto.setId(obj.getLong("id"));
                        dto.setAdvertisementId(obj.getLong("advertisementId"));
                        dto.setAdvertisementTitle(obj.getString("advertisementTitle")) ;
                        dto.setOpponentUsername(obj.optString("opponentUsername", "User"));
                        dto.setLastMessageContent(obj.optString("lastMessageContent", ""));

                        convList.add(dto) ;

                    }
                    Platform.runLater(()->conversationListView.getItems().addAll(convList));

                } catch (Exception e){
                    System.err.println("Error parsing inbox chats framework.");
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void handleBackToMarket(){
        NavigationUtils.navigateTo(conversationListView ,"/com/secondhand/frontend/view/main_market.fxml", "SecondHand Market" ) ;
    }
}
