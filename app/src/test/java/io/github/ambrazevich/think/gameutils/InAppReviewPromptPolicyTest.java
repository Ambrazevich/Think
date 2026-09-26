package io.github.ambrazevich.think.gameutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class InAppReviewPromptPolicyTest {
    private static final long NOW = 1_800_000_000_000L;

    @Test
    public void requestsReviewOnlyAfterThirdMeaningfulRound() {
        assertFalse(InAppReviewPromptPolicy.shouldRequestReview(0, 0L, NOW));
        assertFalse(InAppReviewPromptPolicy.shouldRequestReview(2, 0L, NOW));
        assertTrue(InAppReviewPromptPolicy.shouldRequestReview(3, 0L, NOW));
    }

    @Test
    public void ignoresErrorAndExitWithoutAnswers() {
        int count = 2;
        count = InAppReviewPromptPolicy.updatedMeaningfulRoundCount(
                count, 0, 0, false);
        assertEquals(2, count);

        count = InAppReviewPromptPolicy.updatedMeaningfulRoundCount(
                count, 5, 2, true);
        assertEquals(2, count);

        count = InAppReviewPromptPolicy.updatedMeaningfulRoundCount(
                count, 0, 1, false);
        assertEquals(3, count);
    }

    @Test
    public void scoreDoesNotAffectEligibility() {
        assertTrue(InAppReviewPromptPolicy.isMeaningfulRound(1, 0, false));
        assertTrue(InAppReviewPromptPolicy.isMeaningfulRound(0, 1, false));
        assertTrue(InAppReviewPromptPolicy.isMeaningfulRound(20, 20, false));
    }

    @Test
    public void enforcesNinetyDayRetryInterval() {
        long lastAttempt = NOW;
        long justBeforeNinetyDays = NOW
                + InAppReviewPromptPolicy.RETRY_INTERVAL_MILLIS - 1L;
        long atNinetyDays = NOW + InAppReviewPromptPolicy.RETRY_INTERVAL_MILLIS;

        assertFalse(InAppReviewPromptPolicy.shouldRequestReview(
                4, lastAttempt, justBeforeNinetyDays));
        assertTrue(InAppReviewPromptPolicy.shouldRequestReview(
                4, lastAttempt, atNinetyDays));
        assertFalse(InAppReviewPromptPolicy.shouldRequestReview(
                4, lastAttempt, NOW - 1L));
    }
}
