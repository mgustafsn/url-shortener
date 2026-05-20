package com.marcus.urlshortener.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.marcus.urlshortener.model.UrlEntry;
import com.marcus.urlshortener.service.DynamoDbService;
import com.marcus.urlshortener.util.CodeGenerator;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ShortenFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbService DynamoDb = new DynamoDbService();
    private final Gson gson = new Gson();

    private final String baseUrl = System.getenv("BASE_URL");

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context){

        try {
            JsonObject body = gson.fromJson(input.getBody(), JsonObject.class);

            if(body == null || !body.has("url")) {
                return errorResponse(400, "Missing 'url' in request body");
            }

            String originalUrl = body.get("url").getAsString();

            if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
                return errorResponse(400, "Invalid URL format. URL must start with http:// or https://");
            }

            String code;
            if (body.has("customSlug") && !body.get("customSlug").getAsString().isBlank()) {
                code = body.get("customSlug").getAsString().trim();

                if (!code.matches("^[a-zA-Z0-9_-]+$")) {
                    return errorResponse(400, "Custom slug can only contain letters, numbers, underscores, and hyphens");
                }
            } else {
                code = CodeGenerator.generate();
            }

            long expiresAt = 0;
            if (body.has("expiresInDays")) {
                int days = body.get("expiresInDays").getAsInt();
                if (days > 0) {
                    expiresAt = Instant.now().getEpochSecond() + ((long) days * 86400);
                }
            }

            long createdAt = Instant.now().getEpochSecond();
            UrlEntry entry = new UrlEntry(code, originalUrl, createdAt, expiresAt);
            DynamoDb.save(entry);

            JsonObject responseBody = new JsonObject();
            responseBody.addProperty("shortUrl", baseUrl + "/" + code);
            responseBody.addProperty("code",code);
            responseBody.addProperty("originalUrl", originalUrl);
            responseBody.addProperty("expiresAt", expiresAt == 0 ? "never" : String.valueOf(expiresAt));

            return successResponse(201, responseBody.toString());
        } catch (RuntimeException e) {

            if (e.getMessage() != null && e.getMessage().contains("Code already exists")) {
                return errorResponse(409, "Custom slug already in use. Please choose a different one.");
            }

            context.getLogger().log("Error in ShortenFunction "+ e.getMessage());
            return errorResponse(500, "Internal server error");
        }
    }

    private APIGatewayProxyResponseEvent successResponse(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(corsHeader())
                .withBody(body);
    }

    private APIGatewayProxyResponseEvent errorResponse(int statusCode, String body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(corsHeader())
                .withBody("{\"error\": \"" + body + "\"}");
    }

    private Map<String, String> corsHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        return headers;
    }
}
