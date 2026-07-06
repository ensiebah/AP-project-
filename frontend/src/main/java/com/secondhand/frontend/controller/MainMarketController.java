package com.secondhand.frontend.controller;

import com.secondhand.frontend.model.AdItem;


import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import com.secondhand.frontend.model.AdItem;
import com.secondhand.frontend.network.NetworkClient;
import com.secondhand.frontend.util.NavigationUtils;
import javafx.stage.Stage;

public class MainMarketController {

    @FXML private TextField searchField ;
    @FXML private ListView<AdItem> adListView ;

    @FXML
    public void initialize(){
        fetchAdsFromServer("GET_ADS") ;
        adListView.setOnMouseClicked(event-> {
            if (event.getClickCount() == 2){
                AdItem selectedAd = adListView.getSelectionModel().getSelectedItem() ;
                if (selectedAd != null){
                    openAdDetail(selectedAd) ;
                }
            }
        });
    }
    private void openAdDetail(AdItem ad){
        try{
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/secondhand/frontend/view/ad_details.fxml")) ;
            javafx.scene.Parent root = loader.load() ;

            AdDetailsController controller = loader.getController() ;
            controller.setAdData(ad);

            javafx.stage.Stage stage = (Stage) adListView.getScene().getWindow() ;
            stage.setScene(new javafx.scene.Scene(root));
            stage.setTitle("Ad Detail - "+ad.getTitle());
            stage.show();

        } catch (java.io.IOException e){
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSearch(){
        String query = searchField.getText().trim() ;
        if (query.isBlank()){
            fetchAdsFromServer("GET_ADS") ;
        } else{
            fetchAdsFromServer("SEARCH_ADS|" + query);
        }
    }
    private void fetchAdsFromServer(String requestString){
        adListView.getItems().clear(); ;
        String response = NetworkClient.sendRequest(requestString) ;

        if (response == null || response.isBlank() || response.startsWith("CONNECTION_FAILED") || response.startsWith("ERROR")){
            System.err.println("Failed to fetch advertisements: " +response);
            return;
        }

        String[] adsRaw = response.split(";") ;
        for (String adRaw : adsRaw){
            String tokens[] = adRaw.split("\\|") ;
            if (tokens.length >= 6){
                AdItem items = new AdItem(
                        tokens[0], // ID
                        tokens[1], // Title
                        tokens[2], // Description
                        tokens[3], // Price
                        tokens[4], // City
                        tokens[5]  // Category

                ) ;
                adListView.getItems().add(items) ;
            }
        }
    }
    @FXML
    public void goToCreatAd(){
        // This will link to your future create_ad.fxml screen
        System.out.println("Redirecting to create advertisement workflow.");
    }
    @FXML
    public void handleLogout(){
        NavigationUtils.navigateTo(searchField , "/com/secondhand/frontend/view/login.fxml" , "SecondHand Market - Login");
    }
}
