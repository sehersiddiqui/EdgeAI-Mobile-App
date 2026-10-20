package com.edgeai.reader.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model representing a single reading session entry stored under
 * /users/{userId}/history/{sessionId}.
 */
public class ReadingHistoryItem {
    private String id;
    private String title;
    private long timestamp;
    private String displayDate;
    private int minutes;
    private int words;
    private long createdAt;

    public ReadingHistoryItem() {
        this.createdAt = System.currentTimeMillis();
    }

    public ReadingHistoryItem(String id, String title, long timestamp, String displayDate, int minutes, int words) {
        this.id = id;
        this.title = title;
        this.timestamp = timestamp;
        this.displayDate = displayDate;
        this.minutes = minutes;
        this.words = words;
        this.createdAt = System.currentTimeMillis();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id != null ? id : "");
        map.put("title", title != null ? title : "");
        map.put("timestamp", timestamp);
        map.put("displayDate", displayDate != null ? displayDate : "");
        map.put("minutes", minutes);
        map.put("words", words);
        map.put("createdAt", createdAt > 0 ? createdAt : System.currentTimeMillis());
        return map;
    }

    public static ReadingHistoryItem fromMap(Map<String, Object> map) {
        ReadingHistoryItem item = new ReadingHistoryItem();
        if (map == null) return item;
        item.id = (String) map.get("id");
        item.title = (String) map.get("title");
        item.displayDate = (String) map.get("displayDate");
        if (map.get("timestamp") instanceof Number) {
            item.timestamp = ((Number) map.get("timestamp")).longValue();
        }
        if (map.get("minutes") instanceof Number) {
            item.minutes = ((Number) map.get("minutes")).intValue();
        }
        if (map.get("words") instanceof Number) {
            item.words = ((Number) map.get("words")).intValue();
        }
        if (map.get("createdAt") instanceof Number) {
            item.createdAt = ((Number) map.get("createdAt")).longValue();
        }
        return item;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDisplayDate() { return displayDate; }
    public void setDisplayDate(String displayDate) { this.displayDate = displayDate; }

    public int getMinutes() { return minutes; }
    public void setMinutes(int minutes) { this.minutes = minutes; }

    public int getWords() { return words; }
    public void setWords(int words) { this.words = words; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
