package com.devops.numix.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Settings implements Serializable {

    public enum Difficulty { EASY, MEDIUM, HARD }

    private long roundTimeMillis;
    private Difficulty difficultyLevel;
    private Set<OperationType> enabledOperations;

    // Default settings
    public Settings() {
        this.roundTimeMillis = 60 * 1000; // 1 minute
        this.difficultyLevel = Difficulty.EASY;
        this.enabledOperations = new HashSet<>();
        // Default to Addition being enabled on a fresh install
        this.enabledOperations.add(OperationType.ADDITION);
    }

    public long getRoundTimeMillis() {
        return roundTimeMillis;
    }

    public void setRoundTimeMillis(long roundTimeMillis) {
        this.roundTimeMillis = roundTimeMillis;
    }

    public Difficulty getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(Difficulty difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Set<OperationType> getEnabledOperations() {
        // Ensure the set is never null when accessed
        if (this.enabledOperations == null) {
            this.enabledOperations = new HashSet<>();
        }
        return enabledOperations;
    }

    public void setEnabledOperations(Set<OperationType> enabledOperations) {
        this.enabledOperations = enabledOperations;
    }

    // Corrected isOperationEnabled method. It no longer needs special logic.
    public boolean isOperationEnabled(OperationType type) {
        if (type == null || enabledOperations == null) {
            return false;
        }
        return enabledOperations.contains(type);
    }

    public boolean hasAtLeastOneOperationSelected() {
        return enabledOperations != null && !enabledOperations.isEmpty();
    }
}
