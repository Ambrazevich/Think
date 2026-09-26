package io.github.ambrazevich.think.gameutils;

import java.util.concurrent.TimeUnit;

public final class InAppReviewPromptPolicy {
    static final int REQUIRED_MEANINGFUL_ROUNDS = 3;
    static final long RETRY_INTERVAL_MILLIS = TimeUnit.DAYS.toMillis(90);

    private InAppReviewPromptPolicy() {
    }

    public static boolean isMeaningfulRound(
            int correctAnswers, int incorrectAnswers, boolean endedWithError) {
        return !endedWithError && (correctAnswers > 0 || incorrectAnswers > 0);
    }

    public static int updatedMeaningfulRoundCount(
            int currentCount,
            int correctAnswers,
            int incorrectAnswers,
            boolean endedWithError) {
        int safeCount = Math.max(0, currentCount);
        if (!isMeaningfulRound(correctAnswers, incorrectAnswers, endedWithError)) {
            return safeCount;
        }
        return safeCount == Integer.MAX_VALUE ? safeCount : safeCount + 1;
    }

    public static boolean shouldRequestReview(
            int meaningfulRoundCount, long lastAttemptAtMillis, long nowMillis) {
        if (meaningfulRoundCount < REQUIRED_MEANINGFUL_ROUNDS) {
            return false;
        }
        if (lastAttemptAtMillis <= 0L) {
            return true;
        }
        return nowMillis >= lastAttemptAtMillis
                && nowMillis - lastAttemptAtMillis >= RETRY_INTERVAL_MILLIS;
    }
}
