package com.edgeai.reader.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model representing user reading progress metrics stored under
 * /users/{userId}/progress/stats.
 */
public class ReadingProgress {
    private int sessions = 0;
    private int minutes = 0;
    private int wordsRead = 0;
    private int regressions = 0;
    private int dailyGoalMinutes = 5;
    private long updatedAt;

    public ReadingProgress() {
        this.updatedAt = System.currentTimeMillis();
    }

    public ReadingProgress(int sessions, int minutes, int wordsRead, int regressions, int dailyGoalMinutes) {
        this.sessions = sessions;
        this.minutes = minutes;
        this.wordsRead = wordsRead;
        this.regressions = regressions;
        this.dailyGoalMinutes = dailyGoalMinutes;
        this.updatedAt = System.currentTimeMillis();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("sessions", sessions);
        map.put("minutes", minutes);
        map.put("wordsRead", wordsRead);
        map.put("regressions", regressions);
        map.put("dailyGoalMinutes", dailyGoalMinutes);
        map.put("updatedAt", System.currentTimeMillis());
        return map;
    }

    public static ReadingProgress fromMap(Map<String, Object> map) {
        ReadingProgress p = new ReadingProgress();
        if (map == null) return p;
        if (map.get("sessions") instanceof Number) {
            p.sessions = ((Number) map.get("sessions")).intValue();
        }
        if (map.get("minutes") instanceof Number) {
            p.minutes = ((Number) map.get("minutes")).intValue();
        }
        if (map.get("wordsRead") instanceof Number) {
            p.wordsRead = ((Number) map.get("wordsRead")).intValue();
        }
        if (map.get("regressions") instanceof Number) {
            p.regressions = ((Number) map.get("regressions")).intValue();
        }
        if (map.get("dailyGoalMinutes") instanceof Number) {
            p.dailyGoalMinutes = ((Number) map.get("dailyGoalMinutes")).intValue();
        }
        if (map.get("updatedAt") instanceof Number) {
            p.updatedAt = ((Number) map.get("updatedAt")).longValue();
        }
        return p;
    }

    public int getSessions() { return sessions; }
    public void setSessions(int sessions) { this.sessions = sessions; }

    public int getMinutes() { return minutes; }
    public void setMinutes(int minutes) { this.minutes = minutes; }

    public int getWordsRead() { return wordsRead; }
    public void setWordsRead(int wordsRead) { this.wordsRead = wordsRead; }

    public int getRegressions() { return regressions; }
    public void setRegressions(int regressions) { this.regressions = regressions; }

    public int getDailyGoalMinutes() { return dailyGoalMinutes; }
    public void setDailyGoalMinutes(int dailyGoalMinutes) { this.dailyGoalMinutes = dailyGoalMinutes; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
