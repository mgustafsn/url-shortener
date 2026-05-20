package com.marcus.urlshortener.model;

public class UrlEntry {

    private String code;

    private String originalUrl;

    private long createdAt;

    private long expiresAt;

    private long clickCount;

    private String customSlug;

    public UrlEntry() {}

    public UrlEntry(String code, String originalUrl, long createdAt, long expiresAt){
        this.code = code;
        this.originalUrl = originalUrl;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.clickCount = 0;
    }

    public String getCode() { return code; }
    public void setCode(String code)  { this.code = code; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long expiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public long getClickCount() { return clickCount; }
    public void setClickCount(long clickCount) { this.clickCount = clickCount; }

    public String getCustomSlug() { return customSlug; }
    public void setCustomSlug(String customSlug) { this.customSlug = customSlug; }

    public boolean isExpired() {
        if (expiresAt == 0){
            return false;
        }
       return System.currentTimeMillis() / 1000L > expiresAt;
    }
}
