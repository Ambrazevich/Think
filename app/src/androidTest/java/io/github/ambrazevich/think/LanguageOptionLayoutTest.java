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

@RunWith(AndroidJUnit4.class)
public class LanguageOptionLayoutTest {
    @Test
    public void automaticOptionFitsForEverySupportedLocale() {
        String originalLocales = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        String[] languageTags = {"en", "ru", "he", "fr", "it", "de", "pt"};
        String[] expectedLabels = {
                "Auto", "Автомат", "אוטומטי", "Auto", "Auto", "Autom.", "Auto"
        };

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

                        int inputId = activity.getResources().getIdentifier(
                                "numberpicker_input", "id", "android");
                        TextView input = picker.findViewById(inputId);
                        assertNotNull(input);
                        assertEquals(expectedLabel, input.getText().toString());

                        float textWidth = input.getPaint().measureText(expectedLabel);
                        int availableWidth = picker.getWidth()
                                - picker.getPaddingLeft()
                                - picker.getPaddingRight();
                        assertTrue(languageTag + " label is wider than its picker",
                                textWidth <= availableWidth);

                        Rect pickerBounds = new Rect();
                        Rect columnBounds = new Rect();
                        assertTrue(picker.getGlobalVisibleRect(pickerBounds));
                        assertTrue(((android.view.View) picker.getParent())
                                .getGlobalVisibleRect(columnBounds));
                        assertEquals(picker.getWidth(), pickerBounds.width());
                        assertTrue(languageTag + " picker extends outside its column",
                                columnBounds.contains(pickerBounds));
                    });
                }
            }
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(originalLocales)));
        }
    }
}
