package com.devops.numix.data;

import com.devops.numix.gameutils.Fraction;

public class Problem {
    private String displayEquation; // e.g., "5 + 3", "2^3", "1/2 + 1/4" (without the trailing " =")
    private String correctAnswerString; // For display and comparison, e.g., "8", "3/4"
    private double correctAnswerNumeric; // For decimal problems or quick numeric checks
    private Fraction correctAnswerFraction; // For fraction problems
    private boolean isFractionProblem;
    private OperationType operationType; // To know what kind of problem it is

    // Constructor for Integer/Decimal problems
    public Problem(String displayEquation, String correctAnswerString, double correctAnswerNumeric, OperationType type) {
        this.displayEquation = displayEquation;
        this.correctAnswerString = correctAnswerString;
        this.correctAnswerNumeric = correctAnswerNumeric;
        this.isFractionProblem = false;
        this.operationType = type;
    }

    // Constructor for Fraction problems
    public Problem(String displayEquation, Fraction correctAnswerFraction, OperationType type) {
        this.displayEquation = displayEquation;
        this.correctAnswerFraction = correctAnswerFraction;
        this.correctAnswerString = correctAnswerFraction.toString(); // toString() already simplifies
        this.correctAnswerNumeric = correctAnswerFraction.toDouble();
        this.isFractionProblem = true;
        this.operationType = type;
    }

    public String getDisplayEquation() {
        return displayEquation;
    }

    public String getCorrectAnswerString() {
        return correctAnswerString;
    }

    public double getCorrectAnswerNumeric() {
        return correctAnswerNumeric;
    }

    public Fraction getCorrectAnswerFraction() {
        return correctAnswerFraction;
    }

    public boolean isFractionProblem() {
        return isFractionProblem;
    }

    public OperationType getOperationType() {
        return operationType;
    }
}
