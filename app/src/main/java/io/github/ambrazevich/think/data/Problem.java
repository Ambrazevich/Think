package io.github.ambrazevich.think.data;

import io.github.ambrazevich.think.gameutils.Fraction;
import java.io.Serializable;

public class Problem implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum AnswerType {
        INTEGER,
        DECIMAL,
        FRACTION
    }

    private final String displayEquation;
    private final String correctAnswerString;
    private final double correctAnswerNumeric;
    private final Fraction correctAnswerFraction;
    private final OperationType operationType;
    private final AnswerType answerType;

    // THIS IS THE CONSTRUCTOR THAT WAS MISSING
    // Constructor for INTEGER problems (including Power, Sqrt, and compound integer results)
    public Problem(String displayEquation, String correctAnswerString, OperationType type) {
        this.displayEquation = displayEquation;
        this.correctAnswerString = correctAnswerString;
        this.correctAnswerNumeric = Double.parseDouble(correctAnswerString);
        this.correctAnswerFraction = null;
        this.operationType = type;
        this.answerType = AnswerType.INTEGER;
    }

    // Constructor for DECIMAL problems
    public Problem(String displayEquation, String correctAnswerString, double correctAnswerNumeric, OperationType type) {
        this.displayEquation = displayEquation;
        this.correctAnswerString = correctAnswerString;
        this.correctAnswerNumeric = correctAnswerNumeric;
        this.correctAnswerFraction = null;
        this.operationType = type;
        this.answerType = AnswerType.DECIMAL;
    }

    // Constructor for FRACTION problems
    public Problem(String displayEquation, Fraction correctAnswerFraction, OperationType type) {
        this.displayEquation = displayEquation;
        this.correctAnswerFraction = correctAnswerFraction;
        this.correctAnswerString = correctAnswerFraction.toString();
        this.correctAnswerNumeric = correctAnswerFraction.toDouble();
        this.operationType = type;
        this.answerType = AnswerType.FRACTION;
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

    public OperationType getOperationType() {
        return operationType;
    }

    public AnswerType getAnswerType() {
        return answerType;
    }
}
