package io.github.ambrazevich.think.gameutils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import java.time.Instant;
import java.util.Locale;

public class LocalizedDateFormatterTest {
    @Test
    public void dateAndTimeFollowTheSelectedLocale() {
        long timestamp = Instant.parse("2026-09-24T13:45:00Z").toEpochMilli();

        String english = LocalizedDateFormatter.format(timestamp, Locale.ENGLISH);
        String german = LocalizedDateFormatter.format(timestamp, Locale.GERMAN);

        assertFalse(english.isEmpty());
        assertFalse(german.isEmpty());
        assertNotEquals(english, german);
    }
}
