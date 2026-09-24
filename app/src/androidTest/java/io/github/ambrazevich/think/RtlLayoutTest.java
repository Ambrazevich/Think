package io.github.ambrazevich.think;

import static org.junit.Assert.assertEquals;

import android.view.View;
import android.widget.Button;
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
public class RtlLayoutTest {
    @Test
    public void hebrewUsesRtlExceptForMathAndKeypad() {
        String originalLocales = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("he")));

        Settings settings = new Settings();
        settings.setRoundTimeMillis(120_000L);
        new StorageHelper(ApplicationProviderHolder.context()).saveSettings(settings);

        try (ActivityScenario<SettingsActivity> settingsScenario =
                     ActivityScenario.launch(SettingsActivity.class)) {
            settingsScenario.onActivity(activity ->
                    assertEquals(View.LAYOUT_DIRECTION_RTL,
                            activity.findViewById(R.id.switchAddition).getLayoutDirection()));
        }

        try (ActivityScenario<GameActivity> gameScenario =
                     ActivityScenario.launch(GameActivity.class)) {
            gameScenario.onActivity(activity -> {
                assertEquals(View.LAYOUT_DIRECTION_LTR,
                        activity.findViewById(R.id.gameRoot).getLayoutDirection());
                assertEquals(View.LAYOUT_DIRECTION_LTR,
                        activity.findViewById(R.id.keypadGrid).getLayoutDirection());
                assertEquals(View.TEXT_DIRECTION_LTR,
                        activity.findViewById(R.id.textViewProblem).getTextDirection());
                assertEquals(View.TEXT_DIRECTION_LTR,
                        activity.findViewById(R.id.textViewInput).getTextDirection());
                assertEquals(".",
                        ((Button) activity.findViewById(R.id.buttonDot)).getText().toString());
                assertEquals("תרגיל מתמטי",
                        activity.getString(R.string.content_desc_problem_area));
            });
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(originalLocales)));
        }
    }
}
