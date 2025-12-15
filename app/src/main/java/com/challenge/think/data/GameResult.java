package com.challenge.think.data;

public class GameResult {
    private long timestamp; // System.currentTimeMillis()
    private int correctAnswers;
    private int incorrectAnswers;
    // Potentially store settings used for this game (e.g., difficulty, time)

    public GameResult(long timestamp, int correctAnswers, int incorrectAnswers) {
        this.timestamp = timestamp;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getIncorrectAnswers() {
        return incorrectAnswers;
    }
}
