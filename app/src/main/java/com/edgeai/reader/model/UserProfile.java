package com.edgeai.reader.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model representing a user profile stored in Firestore under /users/{userId}.
 */
public class UserProfile {
    private String uid;
    private String displayName;
    private String email;
    private String mobileNumber;
    private String readingAge;
    private String gender;
    private String address;
    private String photoUrl;
    private long createdAt;
    private long updatedAt;

    public UserProfile() {
        // Required for Firestore deserialization
    }

    public UserProfile(String uid, String displayName, String email) {
        this.uid = uid;
        this.displayName = displayName != null ? displayName : "Reader";
        this.email = email != null ? email : "";
        this.mobileNumber = "Not provided";
        this.readingAge = "Not provided";
        this.gender = "Prefer not to say";
        this.address = "Not provided";
        this.photoUrl = "";
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid != null ? uid : "");
        map.put("displayName", displayName != null ? displayName : "Reader");
        map.put("email", email != null ? email : "");
        map.put("mobileNumber", mobileNumber != null ? mobileNumber : "Not provided");
        map.put("readingAge", readingAge != null ? readingAge : "Not provided");
        map.put("gender", gender != null ? gender : "Prefer not to say");
        map.put("address", address != null ? address : "Not provided");
        map.put("photoUrl", photoUrl != null ? photoUrl : "");
        map.put("createdAt", createdAt > 0 ? createdAt : System.currentTimeMillis());
        map.put("updatedAt", System.currentTimeMillis());
        return map;
    }

    public static UserProfile fromMap(Map<String, Object> map) {
        if (map == null) return new UserProfile();
        UserProfile p = new UserProfile();
        p.uid = (String) map.get("uid");
        p.displayName = (String) map.get("displayName");
        p.email = (String) map.get("email");
        p.mobileNumber = (String) map.get("mobileNumber");
        p.readingAge = (String) map.get("readingAge");
        p.gender = (String) map.get("gender");
        p.address = (String) map.get("address");
        p.photoUrl = (String) map.get("photoUrl");
        if (map.get("createdAt") instanceof Number) {
            p.createdAt = ((Number) map.get("createdAt")).longValue();
        }
        if (map.get("updatedAt") instanceof Number) {
            p.updatedAt = ((Number) map.get("updatedAt")).longValue();
        }
        return p;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getDisplayName() { return displayName != null ? displayName : "Reader"; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getEmail() { return email != null ? email : ""; }
    public void setEmail(String email) { this.email = email; }

    public String getMobileNumber() { return mobileNumber != null ? mobileNumber : "Not provided"; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getReadingAge() { return readingAge != null ? readingAge : "Not provided"; }
    public void setReadingAge(String readingAge) { this.readingAge = readingAge; }

    public String getGender() { return gender != null ? gender : "Prefer not to say"; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address != null ? address : "Not provided"; }
    public void setAddress(String address) { this.address = address; }

    public String getPhotoUrl() { return photoUrl != null ? photoUrl : ""; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
