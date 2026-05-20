package com.marcus.urlshortener.functions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.marcus.urlshortener.model.UrlEntry;
import com.marcus.urlshortener.service.DynamoDbService;

import java.util.HashMap;
import java.util.Map;

public class RedirectFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbService dynamoDb = new DynamoDbService();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

        try {
            String code = input.getPathParameters().get("code");

            if (code == null || code.isBlank()) {
                return errorResponse(400, "Missing 'code' in path parameters");
            }

            UrlEntry entry = dynamoDb.findByCode(code);

            if (entry == null) {
                return errorResponse(404, "Short URL not found");
            }

            if (entry.isExpired()) {
                return errorResponse(410, "Short URL has expired");
            }

            try {
                dynamoDb.incrementClickCount(code);
            } catch (Exception e) {

                context.getLogger().log("Failed to increment click count for code: " + code);
            }

            return redirectResponse(entry.getOriginalUrl());
        } catch (Exception e) {
            context.getLogger().log("Error in RedirectFunction: " + e.getMessage());
            return errorResponse(500, "Internal Server Error");
        }
    }

        private APIGatewayProxyResponseEvent redirectResponse(String url) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Location", url);
            headers.put("Access-Control-Allow-Origin", "*");

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(301)
                    .withHeaders(headers);
        }

        private APIGatewayProxyResponseEvent errorResponse(int statusCode, String message) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(headers)
                    .withBody("{\"error\": \"" + message + "\"}");
        }
    }
