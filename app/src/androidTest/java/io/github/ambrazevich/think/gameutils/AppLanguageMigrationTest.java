package io.github.ambrazevich.think.gameutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppLanguageMigrationTest {
    @Test
    public void newUserDefaultsToSystemLanguage() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String originalLocales = AppCompatDelegate.getApplicationLocales().toLanguageTags();

        try {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList()));
            preferences.edit()
                    .remove(AppLanguageManager.MIGRATION_COMPLETE_KEY)
                    .remove(AppLanguageManager.LEGACY_LANGUAGE_KEY)
                    .commit();

            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    AppLanguageManager.migrateLegacyLocaleIfNeeded(context));

            assertEquals(AppLanguageManager.SYSTEM_LANGUAGE,
                    AppLanguageManager.getSelectedLanguage());
            assertTrue(preferences.getBoolean(AppLanguageManager.MIGRATION_COMPLETE_KEY, false));
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(originalLocales)));
        }
    }

    @Test
    public void legacyHebrewSelectionMigratesOnlyOnce() {
        Context context = ApplicationProvider.getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String originalLocales = AppCompatDelegate.getApplicationLocales().toLanguageTags();

        try {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList()));
            preferences.edit()
                    .remove(AppLanguageManager.MIGRATION_COMPLETE_KEY)
                    .putString(AppLanguageManager.LEGACY_LANGUAGE_KEY, "iw")
                    .commit();

            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    AppLanguageManager.migrateLegacyLocaleIfNeeded(context));

            assertEquals("he", AppLanguageManager.getSelectedLanguage());
            assertTrue(preferences.getBoolean(AppLanguageManager.MIGRATION_COMPLETE_KEY, false));
            assertFalse(preferences.contains(AppLanguageManager.LEGACY_LANGUAGE_KEY));

            preferences.edit().putString(AppLanguageManager.LEGACY_LANGUAGE_KEY, "en").commit();
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    AppLanguageManager.migrateLegacyLocaleIfNeeded(context));
            assertEquals("he", AppLanguageManager.getSelectedLanguage());
        } finally {
            preferences.edit().remove(AppLanguageManager.LEGACY_LANGUAGE_KEY).commit();
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(originalLocales)));
        }
    }
}
