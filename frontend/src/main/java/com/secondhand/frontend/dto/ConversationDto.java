package com.secondhand.frontend.dto;


    public class ConversationDto {

        private Long id;

        private Long buyerId;

        private String buyerUsername;

        private Long sellerId;

        private String sellerUsername;

        private Long advertisementId;

        private String advertisementTitle;

        public Long getBuyerId() {
            return buyerId;
        }

        public String getBuyerUsername() {
            return buyerUsername;
        }

        public Long getSellerId() {
            return sellerId;
        }

        public String getSellerUsername() {
            return sellerUsername;
        }

        public Long getAdvertisementId() {
            return advertisementId;
        }

        public String getAdvertisementTitle() {
            return advertisementTitle;
        }


        public Long getId() {
            return id ;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setBuyerId(Long buyerId) {
            this.buyerId = buyerId;
        }

        public void setBuyerUsername(String buyerUsername) {
            this.buyerUsername = buyerUsername;
        }

        public void setSellerId(Long sellerId) {
            this.sellerId = sellerId;
        }

        public void setSellerUsername(String sellerUsername) {
            this.sellerUsername = sellerUsername;
        }

        public void setAdvertisementId(Long advertisementId) {
            this.advertisementId = advertisementId;
        }

        public void setAdvertisementTitle(String advertisementTitle) {
            this.advertisementTitle = advertisementTitle;
        }
    }

