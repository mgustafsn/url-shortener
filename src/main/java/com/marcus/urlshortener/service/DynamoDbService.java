package com.marcus.urlshortener.service;

import com.marcus.urlshortener.model.UrlEntry;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

public class DynamoDbService {

    private final DynamoDbClient dynamoDb;
    private final String tableName;

    public DynamoDbService() {

        this.dynamoDb = DynamoDbClient.create();
        this.tableName = System.getenv("TABLE_NAME");
    }

    /**
     * Saves a new URL entry to DynamoDB
     * @param entry the UrlEntry object containing the code, original URL, timestamps, etc
     */

    public void save(UrlEntry entry) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("code", AttributeValue.fromS(entry.getCode()));
        item.put("originalUrl", AttributeValue.fromS(entry.getOriginalUrl()));
        item.put("createdAt", AttributeValue.fromN(Long.toString(entry.getCreatedAt())));
        item.put("expiresAt", AttributeValue.fromN(Long.toString(entry.expiresAt())));
        item.put("clickCount", AttributeValue.fromN("0"));

        // Only saves the code if it doens't already exist to prevent overwriting existing entries
        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .conditionExpression("attribute_not_exists(code)") // Ensure no duplicate codes
                .build();

        try {
            dynamoDb.putItem(request);
        } catch (ConditionalCheckFailedException e) {
            // Handle the case where the code already exists (e.g., generate a new code and retry)
            throw new RuntimeException("Code already exists. Please try again.");
        }
    }

    /**
     * Looks up a UrlEntry by its short code
     * Returns null if not found
     */

    public UrlEntry findByCode(String code) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("code", AttributeValue.fromS(code));

        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        GetItemResponse response = dynamoDb.getItem(request);

        if (!response.hasItem() || response.item().isEmpty()) {
            return null;
        }

        return mapToUrlEntry(response.item());
    }

    /**
     * Increments the click counter for a given code by 1
     */
    public void incrementClickCount(String code) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("code", AttributeValue.fromS(code));

        Map<String, AttributeValue> expressionValues = new HashMap<>();
        expressionValues.put(":inc", AttributeValue.fromN("1"));

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .updateExpression("ADD clickCount :inc")
                .expressionAttributeValues(expressionValues)
                .build();

        dynamoDb.updateItem(request);
    }

    /**
     * Returns stats for a given code
     * Returns null if code does not exist
     */
    public UrlEntry getStats(String code) {
        return findByCode(code);
    }

    /**
     * Maps a DynamoDB item to a UrlEntry object
     */
    private UrlEntry mapToUrlEntry(Map<String, AttributeValue> item) {
        UrlEntry entry = new UrlEntry();
        entry.setCode(item.get("code").s());
        entry.setOriginalUrl(item.get("originalUrl").s());
        entry.setCreatedAt(Long.parseLong(item.get("createdAt").n()));
        entry.setExpiresAt(Long.parseLong(item.get("expiresAt").n()));

        if (item.containsKey("clickCount")) {
            entry.setClickCount(Long.parseLong(item.get("clickCount").n()));
        } else {
            entry.setClickCount(0);
        }
        return entry;
    }
}

