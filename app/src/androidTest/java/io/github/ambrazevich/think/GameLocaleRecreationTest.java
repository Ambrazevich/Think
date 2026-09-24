package io.github.ambrazevich.think;

import static org.junit.Assert.assertEquals;

import android.os.Build;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import io.github.ambrazevich.think.data.Settings;
import io.github.ambrazevich.think.gameutils.StorageHelper;

@RunWith(AndroidJUnit4.class)
public class GameLocaleRecreationTest {
    @Test
    public void changingLanguagePreservesActiveRound() throws Exception {
        String originalLocales = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en")));

        Settings settings = new Settings();
        settings.setRoundTimeMillis(120_000L);
        new StorageHelper(ApplicationProviderHolder.context()).saveSettings(settings);

        ActivityScenario<GameActivity> scenario = ActivityScenario.launch(GameActivity.class);
        AtomicReference<String> problem = new AtomicReference<>();
        AtomicLong roundEnd = new AtomicLong();

        try {
            scenario.onActivity(activity -> {
                setIntField(activity, "correctAnswers", 3);
                setIntField(activity, "incorrectAnswers", 2);
                activity.findViewById(R.id.button1).performClick();
                activity.findViewById(R.id.button2).performClick();
                assertEquals("12",
                        ((TextView) activity.findViewById(R.id.textViewInput)).getText().toString());
                assertEquals("12", GameRoundStateStore.get().getString("input"));
                problem.set(((TextView) activity.findViewById(R.id.textViewProblem)).getText().toString());
                roundEnd.set(getLongField(activity, "roundEndElapsedRealtime"));
            });

            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("he")));
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            scenario.onActivity(activity -> {
                assertEquals("he", normalizeLanguage(currentLanguage(activity)));
                assertEquals("תרגיל מתמטי", activity.getString(R.string.content_desc_problem_area));
                assertEquals(problem.get(),
                        ((TextView) activity.findViewById(R.id.textViewProblem)).getText().toString());
                assertEquals("12",
                        ((TextView) activity.findViewById(R.id.textViewInput)).getText().toString());
                assertEquals(3, getIntField(activity, "correctAnswers"));
                assertEquals(2, getIntField(activity, "incorrectAnswers"));
                assertEquals(roundEnd.get(), getLongField(activity, "roundEndElapsedRealtime"));
            });
        } finally {
            scenario.close();
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                    AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(originalLocales)));
        }
    }

    private static String currentLanguage(GameActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return activity.getResources().getConfiguration().getLocales().get(0).getLanguage();
        }
        //noinspection deprecation
        return activity.getResources().getConfiguration().locale.getLanguage();
    }

    private static String normalizeLanguage(String language) {
        return "iw".equals(language) ? "he" : language;
    }

    private static int getIntField(Object target, String name) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.getInt(target);
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
    }

    private static long getLongField(Object target, String name) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.getLong(target);
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
    }

    private static void setIntField(Object target, String name, int value) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.setInt(target, value);
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
    }
}
