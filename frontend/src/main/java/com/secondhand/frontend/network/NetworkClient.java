package com.secondhand.frontend.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NetworkClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    // توکن امنیتی که پس از لاگین/ثبت‌نام اینجا ذخیره می‌شود تا در درخواست‌های بعدی ارسال شود
    public static String authToken = null;

    /**
     * متد عمومی برای ارسال درخواست‌های POST به صورت JSON
     * @param endpoint مسیر اندپوینت (مثلاً "/users/login")
     * @param jsonBody بدنه درخواست به فرمت جی‌سان
     * @return پاسخ سرور به صورت رشته متن (جی‌سان)
     */
    public static String sendPostRequest(String endpoint, String jsonBody) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            // اگر توکن موجود بود، آن را در هدر درخواست قرار بده تا بک‌آند ما را احراز هویت کند
            if (authToken != null) {
                requestBuilder.header("Authorization", "Bearer " + authToken);
            }

            HttpRequest httpRequest = requestBuilder.build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // اگر عملیات موفق بود (200 یا 201)، بادی پاسخ را برگردان
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                return response.body();
            } else {
                // مدیریت خطاهای بک‌آند طبق قالب داک (پیام خطا)
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
}