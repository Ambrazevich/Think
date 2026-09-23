package io.github.ambrazevich.think.gameutils;

import android.content.Context;
import android.content.SharedPreferences;

import io.github.ambrazevich.think.data.GameResult;
import io.github.ambrazevich.think.data.OperationType;
import io.github.ambrazevich.think.data.Settings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class StorageHelper {
    private static final String PREFS_NAME = "ThinkPrefs";
    private static final String KEY_SETTINGS = "GameSettings";
    private static final String KEY_RESULTS = "GameResults";
    static final int MAX_RESULTS = 100;

    private SharedPreferences sharedPreferences;
    private Gson gson;

    public StorageHelper(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveSettings(Settings settings) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        // Serialize Settings object to JSON string
        String settingsJson = gson.toJson(settings);
        editor.putString(KEY_SETTINGS, settingsJson);
        editor.apply();
    }

    public Settings loadSettings() {
        String settingsJson = sharedPreferences.getString(KEY_SETTINGS, null);
        if (settingsJson != null) {
            try {
                Settings loadedSettings = gson.fromJson(settingsJson, Settings.class);
                if (loadedSettings == null) return new Settings();
                if (loadedSettings.getDifficultyLevel() == null) {
                    loadedSettings.setDifficultyLevel(Settings.Difficulty.EASY);
                }
                if (loadedSettings.getEnabledOperations().isEmpty()) {
                    loadedSettings.getEnabledOperations().add(OperationType.ADDITION);
                }
                long roundTime = loadedSettings.getRoundTimeMillis();
                if (roundTime != 0L && roundTime != 60_000L && roundTime != 120_000L) {
                    loadedSettings.setRoundTimeMillis(60_000L);
                }
                return loadedSettings;
            } catch (Exception e) {
                // Log error or handle corruption
                return new Settings(); // Return default if error
            }
        }
        return new Settings(); // Return default if not found
    }

    public void saveGameResult(GameResult result) {
        List<GameResult> results = loadGameResults();
        if (results == null) { // Should not happen if loadGameResults handles nulls
            results = new ArrayList<>();
        }
        results.add(0, result); // Add new result at the beginning
        if (results.size() > MAX_RESULTS) {
            results = new ArrayList<>(results.subList(0, MAX_RESULTS));
        }
        String resultsJson = gson.toJson(results);
        sharedPreferences.edit().putString(KEY_RESULTS, resultsJson).apply();
    }

    public List<GameResult> loadGameResults() {
        String resultsJson = sharedPreferences.getString(KEY_RESULTS, null);
        if (resultsJson != null) {
            Type type = new TypeToken<ArrayList<GameResult>>() {}.getType();
            try {
                List<GameResult> loadedResults = gson.fromJson(resultsJson, type);
                if (loadedResults == null) return new ArrayList<>();
                if (loadedResults.size() > MAX_RESULTS) {
                    return new ArrayList<>(loadedResults.subList(0, MAX_RESULTS));
                }
                return loadedResults;
            } catch (Exception e) {
                // Log error or handle corruption
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }

    public void clearGameResults() {
        sharedPreferences.edit().remove(KEY_RESULTS).apply();
    }
}
