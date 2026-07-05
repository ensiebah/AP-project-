package com.secondhand.frontend.network;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NetworkClient {

    // Toggle this to true if you want to test UI offline without the Spring Boot server
    private static final boolean USE_MOCK_SERVER = false;

    // Spring Boot default local server address
    private static final String SERVER_URL = "http://localhost:8080/api/handle";

    /**
     * Sends a pipe-delimited text request to the backend service.
     * * @param request The formatted command string (e.g., "LOGIN|user|1234")
     * @return The plain text response from the server, or an error action string.
     */
    public static String sendRequest(String request) {
        if (USE_MOCK_SERVER) {
            return getMockResponse(request);
        }

        try {
            // 1. Create the modern HTTP Client with a 5-second connection timeout
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();

            // 2. Build the POST request containing our piping protocol string
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(SERVER_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "text/plain") // Sending raw text packet
                    .POST(HttpRequest.BodyPublishers.ofString(request))
                    .build();

            // 3. Send synchronously and capture the plain text body response
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // Check if the server responded with an HTTP OK (200) status
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "ERROR|Server returned unexpected status code: " + response.statusCode();
            }

        } catch (java.net.ConnectException e) {
            System.err.println("Connection refused. Make sure Spring Boot is running on port 8080.");
            return "ERROR|Could not connect to the backend server. Is it running?";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|An unexpected network error occurred: " + e.getMessage();
        }
    }

    /**
     * Fallback mock mechanism for standalone frontend debugging.
     */
    private static String getMockResponse(String request) {
        if (request == null || request.isBlank()) {
            return "ERROR|Empty request.";
        }

        String command = request.split("\\|")[0];
        switch (command) {
            case "LOGIN":
                return "LOGIN_SUCCESS|USER";
            case "REGISTER":
                return "REGISTER_SUCCESS|Registration successful.";
            case "GET_ADS":
                return "1|iPhone 13 Pro|Battery 90%|800|Tehran|Electronics|APPROVED;" +
                        "2|Giant Bicycle|21 speed bike|350|Shiraz|Sports|PENDING";
            case "CREATE_AD":
                return "CREATE_AD_SUCCESS|Mock ad created.";
            default:
                return "ERROR|Unknown mock command.";
        }
    }
}