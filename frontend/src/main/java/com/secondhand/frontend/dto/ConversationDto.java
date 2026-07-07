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
    }

