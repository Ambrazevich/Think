package io.github.ambrazevich.think.gameutils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class AppLanguageManagerTest {
    @Test
    public void legacyHebrewCodeIsNormalized() {
        assertEquals("he", AppLanguageManager.normalizeLegacyLanguage("iw"));
        assertEquals("he", AppLanguageManager.normalizeLegacyLanguage("he"));
        assertEquals("en", AppLanguageManager.normalizeLegacyLanguage("en"));
    }
}
