package com.edgeai.reader;

import com.edgeai.reader.model.BookItem;
import com.edgeai.reader.model.ReadingHistoryItem;
import com.edgeai.reader.model.ReadingPreferences;
import com.edgeai.reader.model.ReadingProgress;
import com.edgeai.reader.model.UserProfile;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class FirebaseModelsTest {

    @Test
    public void testUserProfileSerialization() {
        UserProfile profile = new UserProfile("test-uid", "Jane Doe", "jane@example.com");
        profile.setMobileNumber("+1234567890");
        profile.setReadingAge("12-14");
        profile.setGender("Female");
        profile.setAddress("Baker Street, London");

        Map<String, Object> map = profile.toMap();
        assertEquals("test-uid", map.get("uid"));
        assertEquals("Jane Doe", map.get("displayName"));
        assertEquals("jane@example.com", map.get("email"));
        assertEquals("+1234567890", map.get("mobileNumber"));
        assertEquals("12-14", map.get("readingAge"));
        assertEquals("Female", map.get("gender"));
        assertEquals("Baker Street, London", map.get("address"));

        UserProfile restored = UserProfile.fromMap(map);
        assertEquals(profile.getUid(), restored.getUid());
        assertEquals(profile.getDisplayName(), restored.getDisplayName());
        assertEquals(profile.getEmail(), restored.getEmail());
        assertEquals(profile.getMobileNumber(), restored.getMobileNumber());
        assertEquals(profile.getReadingAge(), restored.getReadingAge());
        assertEquals(profile.getGender(), restored.getGender());
        assertEquals(profile.getAddress(), restored.getAddress());
    }

    @Test
    public void testReadingPreferencesSerialization() {
        ReadingPreferences prefs = new ReadingPreferences(1.3f, 1.4f, false, true, true, true);
        Map<String, Object> map = prefs.toMap();
        assertEquals(1.3f, ((Number) map.get("textScale")).floatValue(), 0.01f);
        assertEquals(1.4f, ((Number) map.get("spacing")).floatValue(), 0.01f);
        assertEquals(false, map.get("warmReader"));
        assertEquals(true, map.get("highContrast"));
        assertEquals(true, map.get("reduceMotion"));
        assertEquals(true, map.get("focusMode"));

        ReadingPreferences restored = ReadingPreferences.fromMap(map);
        assertEquals(prefs.getTextScale(), restored.getTextScale(), 0.01f);
        assertEquals(prefs.getSpacing(), restored.getSpacing(), 0.01f);
        assertEquals(prefs.isWarmReader(), restored.isWarmReader());
        assertEquals(prefs.isHighContrast(), restored.isHighContrast());
        assertEquals(prefs.isReduceMotion(), restored.isReduceMotion());
        assertEquals(prefs.isFocusMode(), restored.isFocusMode());
    }

    @Test
    public void testReadingProgressSerialization() {
        ReadingProgress progress = new ReadingProgress(8, 72, 2560, 4, 15);
        Map<String, Object> map = progress.toMap();
        assertEquals(8, map.get("sessions"));
        assertEquals(72, map.get("minutes"));
        assertEquals(2560, map.get("wordsRead"));
        assertEquals(4, map.get("regressions"));
        assertEquals(15, map.get("dailyGoalMinutes"));

        ReadingProgress restored = ReadingProgress.fromMap(map);
        assertEquals(progress.getSessions(), restored.getSessions());
        assertEquals(progress.getMinutes(), restored.getMinutes());
        assertEquals(progress.getWordsRead(), restored.getWordsRead());
        assertEquals(progress.getRegressions(), restored.getRegressions());
        assertEquals(progress.getDailyGoalMinutes(), restored.getDailyGoalMinutes());
    }

    @Test
    public void testReadingHistoryItemSerialization() {
        long now = 1718000000000L;
        ReadingHistoryItem item = new ReadingHistoryItem("hist-123", "The Secret Garden", now, "20 Oct, 20:00", 10, 340);
        Map<String, Object> map = item.toMap();
        assertEquals("hist-123", map.get("id"));
        assertEquals("The Secret Garden", map.get("title"));
        assertEquals(now, map.get("timestamp"));
        assertEquals("20 Oct, 20:00", map.get("displayDate"));
        assertEquals(10, map.get("minutes"));
        assertEquals(340, map.get("words"));

        ReadingHistoryItem restored = ReadingHistoryItem.fromMap(map);
        assertEquals(item.getId(), restored.getId());
        assertEquals(item.getTitle(), restored.getTitle());
        assertEquals(item.getTimestamp(), restored.getTimestamp());
        assertEquals(item.getDisplayDate(), restored.getDisplayDate());
        assertEquals(item.getMinutes(), restored.getMinutes());
        assertEquals(item.getWords(), restored.getWords());
    }

    @Test
    public void testBookItemSerialization() {
        long added = 1718000000000L;
        BookItem book = new BookItem("book-1", "Space Exploration", "TXT", "Space is vast.", "uri://sample", added);
        Map<String, Object> map = book.toMap();
        assertEquals("book-1", map.get("id"));
        assertEquals("Space Exploration", map.get("title"));
        assertEquals("TXT", map.get("type"));
        assertEquals("Space is vast.", map.get("content"));
        assertEquals("uri://sample", map.get("uri"));
        assertEquals(added, map.get("addedAt"));

        BookItem restored = BookItem.fromMap(map);
        assertEquals(book.getId(), restored.getId());
        assertEquals(book.getTitle(), restored.getTitle());
        assertEquals(book.getType(), restored.getType());
        assertEquals(book.getContent(), restored.getContent());
        assertEquals(book.getUri(), restored.getUri());
        assertEquals(book.getAddedAt(), restored.getAddedAt());
    }
}
