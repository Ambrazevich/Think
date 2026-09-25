package io.github.ambrazevich.think;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.EnumSet;

import io.github.ambrazevich.think.data.OperationType;
import io.github.ambrazevich.think.data.Settings;
import io.github.ambrazevich.think.gameutils.AppLanguageManager;
import io.github.ambrazevich.think.gameutils.StorageHelper;

@RunWith(AndroidJUnit4.class)
public class SettingsLanguageChangeTest {
    @Test
    public void pickerImmediatelyRecreatesSettingsAndPreservesUiState() {
        String originalLocales = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        StorageHelper storage = new StorageHelper(ApplicationProviderHolder.context());
        Settings originalSettings = storage.loadSettings();

        Settings testSettings = new Settings();
        testSettings.setDifficultyLevel(Settings.Difficulty.HARD);
        testSettings.setRoundTimeMillis(120_000L);
        testSettings.setEnabledOperations(EnumSet.of(
                OperationType.DIVISION, OperationType.COMMON_FRACTIONS));
        testSettings.setDarkMode(originalSettings.isDarkMode());
        storage.saveSettings(testSettings);
        setLocale("en");

        try (ActivityScenario<SettingsActivity> scenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            scenario.onActivity(activity -> {
                ScrollView scroll = activity.findViewById(R.id.settingsScroll);
                scroll.scrollTo(0, 96);
                assertEquals(1,
                        ((NumberPicker) activity.findViewById(R.id.numberPickerLanguage)).getValue());
            });

            selectLanguage(scenario, 2);

            scenario.onActivity(activity -> {
                assertEquals("ru", AppLanguageManager.getSelectedLanguage());
                assertEquals("Настройки",
                        ((TextView) activity.findViewById(
                                R.id.textViewSettingsTitle)).getText().toString());
                assertEquals(2,
                        ((NumberPicker) activity.findViewById(R.id.numberPickerLanguage)).getValue());
                assertEquals(Settings.Difficulty.HARD.ordinal(),
                        ((NumberPicker) activity.findViewById(R.id.numberPickerDifficulty)).getValue());
                assertEquals(1,
                        ((NumberPicker) activity.findViewById(R.id.numberPickerTime)).getValue());
                assertTrue(((android.widget.CompoundButton) activity.findViewById(
                        R.id.switchDivision)).isChecked());
                assertTrue(((android.widget.CompoundButton) activity.findViewById(
                        R.id.switchCommonFractions)).isChecked());
                assertEquals(96,
                        ((ScrollView) activity.findViewById(R.id.settingsScroll)).getScrollY());
            });

            selectLanguage(scenario, 1);
            selectLanguage(scenario, 0);

            scenario.onActivity(activity -> {
                assertEquals(AppLanguageManager.SYSTEM_LANGUAGE,
                        AppLanguageManager.getSelectedLanguage());
                assertEquals(0,
                        ((NumberPicker) activity.findViewById(R.id.numberPickerLanguage)).getValue());
                assertEquals(SettingsActivity.class, activity.getClass());
                assertEquals(96,
                        ((ScrollView) activity.findViewById(R.id.settingsScroll)).getScrollY());
            });
        } finally {
            storage.saveSettings(originalSettings);
            setLocale(originalLocales);
        }
    }

    private static void selectLanguage(
            ActivityScenario<SettingsActivity> scenario, int languageIndex) {
        scenario.onActivity(activity -> {
            NumberPicker picker = activity.findViewById(R.id.numberPickerLanguage);
            picker.setValue(languageIndex);
            activity.handleLanguageSelection(languageIndex);
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    private static void setLocale(String languageTags) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(languageTags)));
    }
}
