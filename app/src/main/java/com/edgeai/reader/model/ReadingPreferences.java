package com.edgeai.reader.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model representing user reading and accessibility preferences stored under
 * /users/{userId}/preferences/settings.
 */
public class ReadingPreferences {
    private float textScale = 1.0f;
    private float spacing = 1.0f;
    private boolean warmReader = true;
    private boolean highContrast = false;
    private boolean reduceMotion = false;
    private boolean focusMode = false;
    private long updatedAt;

    public ReadingPreferences() {
        this.updatedAt = System.currentTimeMillis();
    }

    public ReadingPreferences(float textScale, float spacing, boolean warmReader,
                              boolean highContrast, boolean reduceMotion, boolean focusMode) {
        this.textScale = textScale;
        this.spacing = spacing;
        this.warmReader = warmReader;
        this.highContrast = highContrast;
        this.reduceMotion = reduceMotion;
        this.focusMode = focusMode;
        this.updatedAt = System.currentTimeMillis();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("textScale", textScale);
        map.put("spacing", spacing);
        map.put("warmReader", warmReader);
        map.put("highContrast", highContrast);
        map.put("reduceMotion", reduceMotion);
        map.put("focusMode", focusMode);
        map.put("updatedAt", System.currentTimeMillis());
        return map;
    }

    public static ReadingPreferences fromMap(Map<String, Object> map) {
        ReadingPreferences p = new ReadingPreferences();
        if (map == null) return p;
        if (map.get("textScale") instanceof Number) {
            p.textScale = ((Number) map.get("textScale")).floatValue();
        }
        if (map.get("spacing") instanceof Number) {
            p.spacing = ((Number) map.get("spacing")).floatValue();
        }
        if (map.get("warmReader") instanceof Boolean) {
            p.warmReader = (Boolean) map.get("warmReader");
        }
        if (map.get("highContrast") instanceof Boolean) {
            p.highContrast = (Boolean) map.get("highContrast");
        }
        if (map.get("reduceMotion") instanceof Boolean) {
            p.reduceMotion = (Boolean) map.get("reduceMotion");
        }
        if (map.get("focusMode") instanceof Boolean) {
            p.focusMode = (Boolean) map.get("focusMode");
        }
        if (map.get("updatedAt") instanceof Number) {
            p.updatedAt = ((Number) map.get("updatedAt")).longValue();
        }
        return p;
    }

    public float getTextScale() { return textScale; }
    public void setTextScale(float textScale) { this.textScale = textScale; }

    public float getSpacing() { return spacing; }
    public void setSpacing(float spacing) { this.spacing = spacing; }

    public boolean isWarmReader() { return warmReader; }
    public void setWarmReader(boolean warmReader) { this.warmReader = warmReader; }

    public boolean isHighContrast() { return highContrast; }
    public void setHighContrast(boolean highContrast) { this.highContrast = highContrast; }

    public boolean isReduceMotion() { return reduceMotion; }
    public void setReduceMotion(boolean reduceMotion) { this.reduceMotion = reduceMotion; }

    public boolean isFocusMode() { return focusMode; }
    public void setFocusMode(boolean focusMode) { this.focusMode = focusMode; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
