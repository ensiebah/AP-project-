package com.secondhand.frontend.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NetworkClient {

    public static String userRole = "USER"; // مقدار پیش‌فرض کاربر عادی

    private static final String BASE_URL = "http://localhost:8080/api";
    // توکن امنیتی که پس از لاگین/ثبت‌نام اینجا ذخیره می‌شود تا در درخواست‌های بعدی ارسال شود
    public static String authToken = null;

    /**
     * متد عمومی برای ارسال درخواست‌های POST به صورت JSON (بدون تغییر - کد خودت)
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

            // هندل کردن بادی خالی یا پر برای متد POST
            if (jsonBody != null) {
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(jsonBody));
            } else {
                requestBuilder.POST(HttpRequest.BodyPublishers.noBody());
            }

            // اگر توکن موجود بود، آن را در هدر درخواست قرار بده
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
     * 👈 متد عمومی جدید برای ارسال درخواست‌های GET (جهت واکشی تاریخچه پیام‌ها و لیست چت‌ها)
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

            // ارسال توکن JWT در درخواست‌های GET
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

    // =========================================================================
    // 👈 متدهای اختصاصی بخش سیستم چت و مکالمات (Chat APIs)
    // =========================================================================

    // 👈 ورودی متد را از String advertisementId به Long advertisementId تغییر دهید
    public static String createConversation(Long advertisementId) {
        // مطابق اندپوینت بک‌اَند: /api/conversations/ad/{advertisementId}
        return sendPostRequest("/conversations/ad/" + advertisementId, null);
    }

    /**
     * ۲. ارسال پیام جدید درون یک چت‌روم
     * @param jsonBody رشته جی‌سان حاوی conversationId و content
     */
    public static String sendMessage(String jsonBody) {
        // مطابق اندپوینت بک‌اَند: /api/messages
        return sendPostRequest("/messages", jsonBody);
    }

    /**
     * ۳. دریافت تاریخچه کامل پیام‌های یک چت‌روم
     */
    public static String getConversationMessages(Long conversationId) {
        // مطابق اندپوینت بک‌اَند: /api/messages/conversation/{conversationId}
        return sendGetRequest("/messages/conversation/" + conversationId);
    }

    /**
     * ۴. دریافت لیست چت‌های کاربر لاگین شده (اختیاری - اگر صفحه صندوق پیام داری)
     */
    public static String getMyChats() {
        // مطابق اندپوینت بک‌اَند: /api/conversations/my-chats
        return sendGetRequest("/conversations/my-chats");
    }
    public static String searchAdvertisement(String query, Long categoryId, Long cityId, Double minPrice, Double maxPrice) {
        // اندپوینت هماهنگ با متد فیلتر بک‌اَند شما
        StringBuilder urlBuilder = new StringBuilder("/advertisements/search?");

        if (query != null && !query.isBlank()) urlBuilder.append("query=").append(query).append("&");
        if (categoryId != null) urlBuilder.append("categoryId=").append(categoryId).append("&");
        if (cityId != null) urlBuilder.append("cityId=").append(cityId).append("&");
        if (minPrice != null) urlBuilder.append("minPrice=").append(minPrice).append("&");
        if (maxPrice != null) urlBuilder.append("maxPrice=").append(maxPrice).append("&");

        String path = urlBuilder.toString();
        if (path.endsWith("&") || path.endsWith("?")) {
            path = path.substring(0, path.length() - 1);
        }
        return sendGetRequest(path);
    }
    //---------------------------------------
    //rating and favorite system
    //----------------------------------------
    //adding add to favorite
    public static String addFavorite(Long advertisementId) {
        // ارسال درخواست امن به بک‌اَند بدون نیاز به شناسه دستی کاربر
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
    //check their in favorite box

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
}