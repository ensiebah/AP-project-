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
    public static String searchAdvertisement(String query , String category , Double minPrice , Double maxPrice){
        StringBuilder urlBuilder = new StringBuilder("/search?") ;
        if (query != null && !query.isBlank()) urlBuilder.append("query=").append(query).append("&") ;
        if (category!= null && !category.isBlank()) urlBuilder.append("category=").append(category).append("&")  ;
        if (minPrice != null) urlBuilder.append("minPrice=").append(minPrice).append("&") ;
        if (maxPrice != null) urlBuilder.append("maxPrice=").append(maxPrice).append("&") ;

        //deleting the last extra & or ?
        String path = urlBuilder.toString() ;
        if (path.endsWith("&") || path.endsWith("?")){
            path = path.substring(0 , path.length() -1 ) ;
        }
        return sendGetRequest(path) ;



    }
}