package com.devops.numix.data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Settings implements Serializable { // Implement Serializable for passing via Intent

    public enum Difficulty { EASY, MEDIUM, HARD }

    private long roundTimeMillis; // 0 for endless
    private Difficulty difficultyLevel;
    private Set<OperationType> enabledOperations; // Using a Set for active operations

    // Default settings
    public Settings() {
        this.roundTimeMillis = 60 * 1000; // 1 minute
        this.difficultyLevel = Difficulty.EASY;
        this.enabledOperations = new HashSet<>();
        this.enabledOperations.add(OperationType.ADDITION); // Default operation
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
        return enabledOperations;
    }

    public void setEnabledOperations(Set<OperationType> enabledOperations) {
        this.enabledOperations = enabledOperations;
    }

    public boolean isOperationEnabled(OperationType type) {
        if (type == null || enabledOperations == null) return false;

        // If checking for a specific fraction operation (ADD, SUB, etc.),
        // it's enabled if the general FRACTIONS category switch is on.
        if (type == OperationType.FRACTION_ADD || type == OperationType.FRACTION_SUBTRACT ||
                type == OperationType.FRACTION_MULTIPLY || type == OperationType.FRACTION_DIVIDE) {
            return enabledOperations.contains(OperationType.FRACTIONS);
        }
        return enabledOperations.contains(type);
    }

    public void setOperationEnabled(OperationType type, boolean enabled) {
        if (enabled) {
            enabledOperations.add(type);
        } else {
            enabledOperations.remove(type);
        }
    }

    public boolean hasAtLeastOneOperationSelected() {
        return enabledOperations != null && !enabledOperations.isEmpty();
    }
}
