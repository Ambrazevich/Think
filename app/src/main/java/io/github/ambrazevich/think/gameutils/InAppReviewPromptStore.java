package io.github.ambrazevich.think.gameutils;

import android.content.Context;
import android.content.SharedPreferences;

public final class InAppReviewPromptStore {
    private static final String PREFS_NAME = "InAppReviewPrompt";
    private static final String KEY_MEANINGFUL_ROUND_COUNT = "meaningfulRoundCount";
    private static final String KEY_LAST_ATTEMPT_AT = "lastAttemptAt";

    private final SharedPreferences preferences;

    public InAppReviewPromptStore(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void recordCompletedRound(
            int correctAnswers, int incorrectAnswers, boolean endedWithError) {
        int currentCount = getMeaningfulRoundCount();
        int updatedCount = InAppReviewPromptPolicy.updatedMeaningfulRoundCount(
                currentCount, correctAnswers, incorrectAnswers, endedWithError);
        if (updatedCount != currentCount) {
            preferences.edit()
                    .putInt(KEY_MEANINGFUL_ROUND_COUNT, updatedCount)
                    .apply();
        }
    }

    public boolean shouldRequestReview(long nowMillis) {
        return InAppReviewPromptPolicy.shouldRequestReview(
                getMeaningfulRoundCount(), getLastAttemptAtMillis(), nowMillis);
    }

    public void recordAttempt(long attemptedAtMillis) {
        preferences.edit().putLong(KEY_LAST_ATTEMPT_AT, attemptedAtMillis).apply();
    }

    int getMeaningfulRoundCount() {
        return preferences.getInt(KEY_MEANINGFUL_ROUND_COUNT, 0);
    }

    long getLastAttemptAtMillis() {
        return preferences.getLong(KEY_LAST_ATTEMPT_AT, 0L);
    }
}
