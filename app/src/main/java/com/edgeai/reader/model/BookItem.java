package com.edgeai.reader.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model representing a book in the user's library stored under
 * /users/{userId}/books/{bookId}.
 */
public class BookItem {
    private String id;
    private String title;
    private String type;
    private String content;
    private String uri;
    private long addedAt;

    public BookItem() {
        this.addedAt = System.currentTimeMillis();
    }

    public BookItem(String id, String title, String type, String content, String uri, long addedAt) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.content = content;
        this.uri = uri;
        this.addedAt = addedAt > 0 ? addedAt : System.currentTimeMillis();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id != null ? id : "");
        map.put("title", title != null ? title : "");
        map.put("type", type != null ? type : "TXT");
        map.put("content", content != null ? content : "");
        map.put("uri", uri != null ? uri : "");
        map.put("addedAt", addedAt > 0 ? addedAt : System.currentTimeMillis());
        return map;
    }

    public static BookItem fromMap(Map<String, Object> map) {
        BookItem item = new BookItem();
        if (map == null) return item;
        item.id = (String) map.get("id");
        item.title = (String) map.get("title");
        item.type = (String) map.get("type");
        item.content = (String) map.get("content");
        item.uri = (String) map.get("uri");
        if (map.get("addedAt") instanceof Number) {
            item.addedAt = ((Number) map.get("addedAt")).longValue();
        }
        return item;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }

    public long getAddedAt() { return addedAt; }
    public void setAddedAt(long addedAt) { this.addedAt = addedAt; }
}
