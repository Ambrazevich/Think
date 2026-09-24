package io.github.ambrazevich.think.gameutils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public final class AppLanguageManager {
    public static final String SYSTEM_LANGUAGE = "system";
    static final String LEGACY_LANGUAGE_KEY = "Locale.Helper.Selected.Language";
    static final String MIGRATION_COMPLETE_KEY = "appcompat_locale_migration_complete_v1";

    private AppLanguageManager() {
    }

    public static void migrateLegacyLocaleIfNeeded(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getBoolean(MIGRATION_COMPLETE_KEY, false)) {
            return;
        }

        String legacyLanguage = preferences.getString(LEGACY_LANGUAGE_KEY, null);
        LocaleListCompat currentLocales = AppCompatDelegate.getApplicationLocales();
        if (currentLocales.isEmpty() && legacyLanguage != null) {
            String normalizedLanguage = normalizeLegacyLanguage(legacyLanguage);
            if ("en".equals(normalizedLanguage) || "he".equals(normalizedLanguage)) {
                AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(normalizedLanguage));
            }
        }

        preferences.edit()
                .remove(LEGACY_LANGUAGE_KEY)
                .putBoolean(MIGRATION_COMPLETE_KEY, true)
                .apply();
    }

    public static String getSelectedLanguage() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        return locales.isEmpty()
                ? SYSTEM_LANGUAGE
                : normalizeLegacyLanguage(locales.get(0).getLanguage());
    }

    public static void setSelectedLanguage(String languageTag) {
        LocaleListCompat locales = languageTag == null
                || languageTag.isEmpty()
                || SYSTEM_LANGUAGE.equals(languageTag)
                ? LocaleListCompat.getEmptyLocaleList()
                : LocaleListCompat.forLanguageTags(languageTag);
        AppCompatDelegate.setApplicationLocales(locales);
    }

    static String normalizeLegacyLanguage(String language) {
        return "iw".equals(language) ? "he" : language;
    }
}
