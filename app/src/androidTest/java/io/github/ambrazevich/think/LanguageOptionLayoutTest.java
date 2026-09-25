package io.github.ambrazevich.think;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Rect;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import io.github.ambrazevich.think.data.Settings;
import io.github.ambrazevich.think.gameutils.StorageHelper;

@RunWith(AndroidJUnit4.class)
public class LanguageOptionLayoutTest {
    @Test
    public void allPickerOptionsFitForEverySupportedLocale() {
        String originalLocales = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        String[] languageTags = {"en", "ru", "he", "fr", "it", "de", "pt"};
        String[] expectedLabels = {
                "Auto", "Автомат", "אוטומטי", "Auto", "Auto", "Autom.", "Auto"
        };
        StorageHelper storage = new StorageHelper(ApplicationProviderHolder.context());
        Settings originalSettings = storage.loadSettings();

        try {
            for (int i = 0; i < languageTags.length; i++) {
                String languageTag = languageTags[i];
                String expectedLabel = expectedLabels[i];
                InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                        AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags(languageTag)));

                try (ActivityScenario<SettingsActivity> scenario =
                             ActivityScenario.launch(SettingsActivity.class)) {
                    scenario.onActivity(activity -> {
                        NumberPicker picker = activity.findViewById(R.id.numberPickerLanguage);
                        picker.setValue(0);

                        String[] options = activity.getResources()
                                .getStringArray(R.array.language_options);
                        assertEquals(expectedLabel, options[0]);
                        assertAllValuesFit(picker, options, languageTag + " language", activity);
                        assertAllValuesFit(
                                activity.findViewById(R.id.numberPickerDifficulty),
                                activity.getResources().getStringArray(R.array.difficulty_levels),
                                languageTag + " difficulty", activity);
                        assertAllValuesFit(
                                activity.findViewById(R.id.numberPickerTime),
                                activity.getResources().getStringArray(R.array.time_options),
                                languageTag + " time", activity);
                    });
                }
            }
        } finally {
            storage.saveSettings(originalSettings);
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(originalLocales)));
        }
    }

    private static void assertAllValuesFit(
            NumberPicker picker, String[] values, String label, android.app.Activity activity) {
        int inputId = activity.getResources().getIdentifier(
                "numberpicker_input", "id", "android");
        TextView input = picker.findViewById(inputId);
        assertNotNull(input);

        int availableWidth = picker.getWidth() - picker.getPaddingLeft() - picker.getPaddingRight();
        for (int value = picker.getMinValue(); value <= picker.getMaxValue(); value++) {
            picker.setValue(value);
            String expected = values[value - picker.getMinValue()];
            assertEquals(expected, input.getText().toString());
            assertTrue(label + " value is wider than its picker: " + expected,
                    input.getPaint().measureText(expected) <= availableWidth);
        }

        Rect pickerBounds = new Rect();
        Rect columnBounds = new Rect();
        assertTrue(picker.getGlobalVisibleRect(pickerBounds));
        assertTrue(((android.view.View) picker.getParent()).getGlobalVisibleRect(columnBounds));
        assertEquals(picker.getWidth(), pickerBounds.width());
        assertTrue(label + " picker extends outside its column",
                columnBounds.contains(pickerBounds));
    }
}
