package com.secondhand.frontend.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NetworkClient {

    public static String userRole = "USER"; // Default role: USER
    public static String currentUsername = "Guest";
    public static String currentFullName = "Guest";

    private static final String BASE_URL = "http://localhost:8080/api";
    // Authentication token populated upon login
    public static String authToken = null;

    /**
     * Dispatches a synchronous HTTP POST request with a JSON payload.
     */
    public static String sendPostRequest(String endpoint, String jsonBody) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json");

            if (jsonBody != null) {
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
            } else {
                requestBuilder.POST(HttpRequest.BodyPublishers.noBody());
            }

            if (authToken != null) {
                requestBuilder.header("Authorization", "Bearer " + authToken);
            }

            HttpRequest httpRequest = requestBuilder.build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return response.body();
            } else {
                return "ERROR|" + response.body();
            }

        } catch (java.net.ConnectException e) {
            System.err.println("Connection refused. Is Spring Boot running?");
            return "ERROR|Could not connect to the backend server.";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|An unexpected network error occurred: " + e.getMessage();
        }
    }

    /**
     * Dispatches a synchronous HTTP GET request to fetch protected cloud resources.
     */
    public static String sendGetRequest(String endpoint) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .GET();

            if (authToken != null) {
                requestBuilder.header("Authorization", "Bearer " + authToken);
            }

            HttpRequest httpRequest = requestBuilder.build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "ERROR|" + response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|An unexpected network error occurred: " + e.getMessage();
        }
    }

    /**
     * Fetches all active and pending advertisements submitted by the authenticated seller user.
     */
    public static String getMyAdvertisements() {
        return sendGetRequest("/advertisements/my-ads");
    }

    /**
     * Dispatches an HTTP DELETE request to erase or soft-delete an advertisement completely.
     */
    public static String deleteAdvertisement(Long adId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/advertisements/" + adId))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 ? "SUCCESS" : "ERROR|" + response.body();
        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    /**
     * Dispatches an HTTP PUT request to modify parameters of an already existing advertisement.
     */
    public static String updateAdvertisementRaw(Long adId, String jsonBody) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/advertisements/" + adId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 ? response.body() : "ERROR|" + response.body();
        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    public static String createConversation(Long advertisementId) {
        return sendPostRequest("/conversations/ad/" + advertisementId, null);
    }

    public static String sendMessage(String jsonBody) {
        return sendPostRequest("/messages", jsonBody);
    }

    public static String getConversationMessages(Long conversationId) {
        return sendGetRequest("/messages/conversation/" + conversationId);
    }

    public static String getMyChats() {
        return sendGetRequest("/conversations/my-chats");
    }

    /**
     * 🟢 متد اورلود شده و جدید سرچ برای انتقال پارامترهای تفکیک‌شده‌ی مرتب‌سازی به سمت سرور اسپرینگ‌بوت
     */
    public static String searchAdvertisement(String query, Long categoryId, Long cityId, Double minPrice, Double maxPrice, String sortBy, String order) {
        StringBuilder urlBuilder = new StringBuilder("/advertisements/search?");
        if (query != null && !query.isBlank()) urlBuilder.append("query=").append(query).append("&");
        if (categoryId != null) urlBuilder.append("categoryId=").append(categoryId).append("&");
        if (cityId != null) urlBuilder.append("cityId=").append(cityId).append("&");
        if (minPrice != null) urlBuilder.append("minPrice=").append(minPrice).append("&");
        if (maxPrice != null) urlBuilder.append("maxPrice=").append(maxPrice).append("&");

        // الصاق فیلدهای مرتب‌سازی پویا به انتهای آدرس
        if (sortBy != null && !sortBy.isBlank()) urlBuilder.append("sortBy=").append(sortBy).append("&");
        if (order != null && !order.isBlank()) urlBuilder.append("order=").append(order).append("&");

        String path = urlBuilder.toString();
        if (path.endsWith("&") || path.endsWith("?")) {
            path = path.substring(0, path.length() - 1);
        }
        return sendGetRequest(path);
    }

    // حفظ ساختار متد قبلی جهت جلوگیری از تداخل در بقیه کلاس‌ها
    public static String searchAdvertisement(String query, Long categoryId, Long cityId, Double minPrice, Double maxPrice) {
        return searchAdvertisement(query, categoryId, cityId, minPrice, maxPrice, "date", "desc");
    }

    public static String addFavorite(Long advertisementId) {
        return sendPostRequest("/favorites?advertisementId=" + advertisementId, null);
    }

    public static String removeFavorite(Long advertisementId) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String queryUrl = BASE_URL + "/favorites?advertisementId=" + advertisementId;
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(queryUrl))
                    .DELETE();

            if (authToken != null) {
                requestBuilder.header("Authorization", "Bearer " + authToken);
            }

            HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 ? "SUCCESS" : "ERROR|" + response.body();
        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    public static String getMyFavorites() {
        return sendGetRequest("/favorites/my-favorites");
    }

    public static String addAdComment(Long adId, String content) {
        String jsonBody = "{\"content\":\"" + content + "\"}";
        return sendPostRequest("/comments/ad/" + adId, jsonBody);
    }

    public static String getAdComments(Long adId) {
        return sendGetRequest("/comments/ad/" + adId);
    }

    public static String getAdDetailsRaw(Long adId) {
        return sendGetRequest("/advertisements/" + adId);
    }

    public static String checkRatingEligibility(Long adId) {
        return sendGetRequest("/ratings/check-eligibility/" + adId);
    }

    public static String updateAdStatus(Long adId, String jsonBody) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            // 🟢 صدا زدن مستقیم اندپوینت اختصاصی بک‌اند برای فروخته شدن آگهی
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/advertisements/" + adId + "/sold"))
                    .header("Authorization", "Bearer " + authToken)
                    .PUT(HttpRequest.BodyPublishers.noBody()) // بک‌اند شما به Body نیاز ندارد
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 ? "SUCCESS" : "ERROR|" + response.body();
        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

}