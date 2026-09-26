package io.github.ambrazevich.think;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.graphics.Color;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.os.LocaleListCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import io.github.ambrazevich.think.adapters.ResultsAdapter;
import io.github.ambrazevich.think.data.GameResult;
import io.github.ambrazevich.think.data.Settings;
import io.github.ambrazevich.think.gameutils.StorageHelper;

@RunWith(AndroidJUnit4.class)
public class UiThemeLayoutTest {
    private static final String[] LANGUAGE_TAGS = {"en", "ru", "he", "fr", "it", "de", "pt"};

    @Test
    public void everyScreenFitsInEveryLocaleAndTheme() {
        String originalLocales = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        StorageHelper storage = new StorageHelper(ApplicationProviderHolder.context());
        Settings originalSettings = storage.loadSettings();

        try {
            for (boolean dark : new boolean[]{false, true}) {
                Settings testSettings = storage.loadSettings();
                testSettings.setDarkMode(dark);
                testSettings.setRoundTimeMillis(0L);
                storage.saveSettings(testSettings);
                setNightMode(dark);

                for (String languageTag : LANGUAGE_TAGS) {
                    setLocale(languageTag);
                    assertActivityFits(MainActivity.class, languageTag, dark);
                    assertActivityFits(SettingsActivity.class, languageTag, dark);
                    assertActivityFits(ResultsActivity.class, languageTag, dark);

                    GameRoundStateStore.clear();
                    assertActivityFits(GameActivity.class, languageTag, dark);
                    GameRoundStateStore.clear();
                }
            }
        } finally {
            storage.saveSettings(originalSettings);
            GameRoundStateStore.clear();
            setLocale(originalLocales);
            setNightMode(originalSettings.isDarkMode());
        }
    }

    @Test
    public void semanticPaletteHasRequiredColorsAndContrast() {
        String originalLocales = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        StorageHelper storage = new StorageHelper(ApplicationProviderHolder.context());
        Settings originalSettings = storage.loadSettings();

        try {
            setLocale("en");
            for (boolean dark : new boolean[]{false, true}) {
                Settings settings = storage.loadSettings();
                settings.setDarkMode(dark);
                storage.saveSettings(settings);
                setNightMode(dark);

                try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
                    scenario.onActivity(activity -> assertPalette(activity, dark));
                }
            }
        } finally {
            storage.saveSettings(originalSettings);
            setLocale(originalLocales);
            setNightMode(originalSettings.isDarkMode());
        }
    }

    @Test
    public void resultRowsAreContiguousAndResultsScreenHasNoStaticLabels() {
        try (ActivityScenario<ResultsActivity> scenario =
                     ActivityScenario.launch(ResultsActivity.class)) {
            scenario.onActivity(activity -> {
                ViewGroup root = activity.findViewById(R.id.resultsRoot);
                assertEquals("Results screen should only contain the list and clear button",
                        2, root.getChildCount());

                RecyclerView recycler = new RecyclerView(activity);
                recycler.setLayoutManager(new LinearLayoutManager(activity));
                recycler.setAdapter(new ResultsAdapter(activity, Arrays.asList(
                        new GameResult(1_700_000_000_000L, 8, 1),
                        new GameResult(1_699_999_000_000L, 6, 2),
                        new GameResult(1_699_998_000_000L, 4, 3))));

                int width = root.getWidth();
                recycler.measure(
                        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(1200, View.MeasureSpec.EXACTLY));
                recycler.layout(0, 0, width, 1200);

                assertTrue("Synthetic result list did not lay out enough rows",
                        recycler.getChildCount() >= 3);
                for (int index = 1; index < recycler.getChildCount(); index++) {
                    View previous = recycler.getChildAt(index - 1);
                    View current = recycler.getChildAt(index);
                    assertEquals("Result rows should not have empty vertical gaps",
                            previous.getBottom(), current.getTop());
                }
            });
        }
    }

    private static void assertActivityFits(
            Class<? extends Activity> activityClass, String languageTag, boolean dark) {
        String label = activityClass.getSimpleName() + " " + languageTag
                + (dark ? " dark" : " light");
        try (ActivityScenario<? extends Activity> scenario = ActivityScenario.launch(activityClass)) {
            scenario.onActivity(activity -> {
                assertNoEllipsizedText(activity.findViewById(android.R.id.content), label);
                if (activity instanceof MainActivity) {
                    assertMainTitleCentered(activity, label);
                }
            });
        }
    }

    private static void assertMainTitleCentered(Activity activity, String label) {
        TextView title = activity.findViewById(R.id.textGameTitle);
        View content = activity.findViewById(android.R.id.content);
        assertEquals(label + " main title must use centered gravity",
                Gravity.CENTER_HORIZONTAL,
                title.getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK);

        Layout layout = title.getLayout();
        assertNotNull(label + " main title was not laid out", layout);
        float textCenter = title.getX() + title.getTotalPaddingLeft()
                + (layout.getLineLeft(0) + layout.getLineRight(0)) / 2f;
        float screenCenter = content.getWidth() / 2f;
        assertTrue(label + " main title is off center by "
                        + Math.abs(textCenter - screenCenter) + " px",
                Math.abs(textCenter - screenCenter) <= 1f);
    }

    private static void assertNoEllipsizedText(View view, String label) {
        if (view.getVisibility() != View.VISIBLE) return;

        if (view instanceof TextView && !isInsideNumberPicker(view)) {
            TextView textView = (TextView) view;
            if (textView.getText() != null && textView.getText().length() > 0) {
                Layout layout = textView.getLayout();
                assertNotNull(label + " has an unmeasured text view: " + textView.getText(), layout);
                assertTrue(label + " has no lines for: " + textView.getText(),
                        layout.getLineCount() > 0);
                for (int line = 0; line < layout.getLineCount(); line++) {
                    assertEquals(label + " ellipsizes: " + textView.getText(),
                            0, layout.getEllipsisCount(line));
                }
                int lastLine = layout.getLineCount() - 1;
                assertTrue(label + " clips: " + textView.getText(),
                        layout.getLineEnd(lastLine) >= layout.getText().length());
                int availableHeight = textView.getHeight()
                        - textView.getCompoundPaddingTop()
                        - textView.getCompoundPaddingBottom();
                assertTrue(label + " clips vertically: " + textView.getText(),
                        layout.getHeight() <= availableHeight);
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                assertNoEllipsizedText(group.getChildAt(i), label);
            }
        }
    }

    private static boolean isInsideNumberPicker(View view) {
        ViewParent parent = view.getParent();
        while (parent instanceof View) {
            if (parent instanceof NumberPicker) return true;
            parent = parent.getParent();
        }
        return false;
    }

    private static void assertPalette(Activity activity, boolean dark) {
        int background = color(activity, R.color.game_background);
        int surface = color(activity, R.color.game_surface);
        int primary = color(activity, R.color.game_primary);
        int onPrimary = color(activity, R.color.game_on_primary);
        int textPrimary = color(activity, R.color.text_primary);
        int textSecondary = color(activity, R.color.text_secondary);
        int outline = color(activity, R.color.outline_strong);
        int error = color(activity, R.color.game_error);
        int progressTrack = color(activity, R.color.progress_bar_background);

        assertEquals(Color.parseColor(dark ? "#15171C" : "#F7F5F2"), background);
        assertEquals(Color.parseColor(dark ? "#24272E" : "#FFFFFF"), surface);
        assertEquals(Color.parseColor(dark ? "#F4F4F2" : "#20242B"), textPrimary);
        assertEquals(Color.parseColor(dark ? "#FF6B78" : "#0E625F"), primary);
        assertEquals(Color.parseColor(dark ? "#15171C" : "#FFFFFF"), onPrimary);

        assertContrast("primary text/background", textPrimary, background, 4.5);
        assertContrast("primary text/surface", textPrimary, surface, 4.5);
        assertContrast("secondary text/surface", textSecondary, surface, 4.5);
        assertContrast("on-primary/primary", onPrimary, primary, 4.5);
        assertContrast("outline/surface", outline, surface, 3.0);
        assertContrast("error/surface", error, surface, 4.5);
        assertContrast("progress/track", primary, progressTrack, 3.0);
    }

    private static int color(Activity activity, int resourceId) {
        return ContextCompat.getColor(activity, resourceId);
    }

    private static void assertContrast(String role, int foreground, int background, double minimum) {
        double lighter = Math.max(luminance(foreground), luminance(background));
        double darker = Math.min(luminance(foreground), luminance(background));
        double ratio = (lighter + 0.05) / (darker + 0.05);
        assertTrue(role + " contrast was " + ratio + ", expected at least " + minimum,
                ratio >= minimum);
    }

    private static double luminance(int color) {
        return 0.2126 * linear(Color.red(color) / 255.0)
                + 0.7152 * linear(Color.green(color) / 255.0)
                + 0.0722 * linear(Color.blue(color) / 255.0);
    }

    private static double linear(double component) {
        return component <= 0.04045
                ? component / 12.92
                : Math.pow((component + 0.055) / 1.055, 2.4);
    }

    private static void setLocale(String languageTags) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(languageTags)));
    }

    private static void setNightMode(boolean dark) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() ->
                AppCompatDelegate.setDefaultNightMode(dark
                        ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO));
    }
}
