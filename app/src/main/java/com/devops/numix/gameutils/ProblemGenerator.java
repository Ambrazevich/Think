package com.devops.numix.gameutils;

import android.util.Log;
import com.devops.numix.data.OperationType;
import com.devops.numix.data.Problem;
import com.devops.numix.data.Settings;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class ProblemGenerator {
    private static final String TAG = "ProblemGenerator";
    private final Random random;
    private final Settings settings;
    private final DecimalFormat decimalFormat;
    private final DecimalFormat integerFormat;

    // --- NEW: Variable to store the last problem ---
    private String lastProblemDisplay = "";

    private static class Operand {
        final String display;
        final double value;
        final Fraction fractionValue;

        Operand(String display, double value) { this.display = display; this.value = value; this.fractionValue = null; }
        Operand(Fraction fraction) { this.display = fraction.toString(); this.value = fraction.toDouble(); this.fractionValue = fraction; }
        boolean isFraction() { return fractionValue != null; }
    }

    public ProblemGenerator(Settings settings) {
        this.settings = (settings != null) ? settings : new Settings();
        this.random = new Random();
        this.decimalFormat = new DecimalFormat("#.##");
        this.integerFormat = new DecimalFormat("#");
    }

    public Problem generateProblem() {
        Problem newProblem;
        int maxRetries = 5; // Safety break to prevent rare infinite loops
        int retries = 0;

        // --- NEW: Loop to ensure the next problem is not a duplicate ---
        do {
            newProblem = generateNewProblemInternal();
            retries++;
        } while (newProblem.getDisplayEquation().equals(lastProblemDisplay) && retries < maxRetries);

        if (retries == maxRetries) {
            Log.w(TAG, "Could not generate a unique problem after " + maxRetries + " tries. Returning a duplicate.");
        }

        // Store the new problem's display string for the next round
        lastProblemDisplay = newProblem.getDisplayEquation();
        return newProblem;
    }

    private Problem generateNewProblemInternal() {
        Set<OperationType> enabledOps = settings.getEnabledOperations();
        if (enabledOps == null || enabledOps.isEmpty()) {
            return generateIntegerProblem(OperationType.ADDITION);
        }

        List<OperationType> basicOps = new ArrayList<>();
        List<OperationType> complexOperandTypes = new ArrayList<>();

        if (enabledOps.contains(OperationType.ADDITION)) basicOps.add(OperationType.ADDITION);
        if (enabledOps.contains(OperationType.SUBTRACTION)) basicOps.add(OperationType.SUBTRACTION);
        if (enabledOps.contains(OperationType.MULTIPLICATION)) basicOps.add(OperationType.MULTIPLICATION);
        if (enabledOps.contains(OperationType.DIVISION)) basicOps.add(OperationType.DIVISION);

        if (enabledOps.contains(OperationType.POWER)) complexOperandTypes.add(OperationType.POWER);
        if (enabledOps.contains(OperationType.SQUARE_ROOT)) complexOperandTypes.add(OperationType.SQUARE_ROOT);
        if (enabledOps.contains(OperationType.COMMON_FRACTIONS)) complexOperandTypes.add(OperationType.COMMON_FRACTIONS);
        if (enabledOps.contains(OperationType.DECIMAL_FRACTIONS)) complexOperandTypes.add(OperationType.DECIMAL_FRACTIONS);

        boolean canDoCompound = !basicOps.isEmpty() && !complexOperandTypes.isEmpty();
        boolean shouldBeCompound = canDoCompound && random.nextInt(3) == 0;

        if (shouldBeCompound) {
            return generateCompoundProblem(basicOps, complexOperandTypes);
        } else {
            List<OperationType> allAvailableSimpleOps = new ArrayList<>(enabledOps);
            OperationType simpleOp = allAvailableSimpleOps.get(random.nextInt(allAvailableSimpleOps.size()));
            return generateSimpleProblem(simpleOp);
        }
    }

    private Problem generateSimpleProblem(OperationType opType){
        switch (opType) {
            case ADDITION: case SUBTRACTION: case MULTIPLICATION: case DIVISION: case POWER:
                return generateIntegerProblem(opType);
            case SQUARE_ROOT:
                return generateSquareRootProblem();
            case COMMON_FRACTIONS:
                List<OperationType> fracOps = Arrays.asList(OperationType.INTERNAL_FRACTION_ADD, OperationType.INTERNAL_FRACTION_SUB, OperationType.INTERNAL_FRACTION_MUL, OperationType.INTERNAL_FRACTION_DIV);
                return generateCommonFractionProblem(fracOps.get(random.nextInt(fracOps.size())));
            case DECIMAL_FRACTIONS:
                List<OperationType> decOps = Arrays.asList(OperationType.INTERNAL_DECIMAL_ADD, OperationType.INTERNAL_DECIMAL_SUB, OperationType.INTERNAL_DECIMAL_MUL, OperationType.INTERNAL_DECIMAL_DIV);
                return generateDecimalProblem(decOps.get(random.nextInt(decOps.size())));
            default:
                return generateIntegerProblem(OperationType.ADDITION);
        }
    }

    private Problem generateCompoundProblem(List<OperationType> basicOps, List<OperationType> operandTypes) {
        OperationType basicOp = basicOps.get(random.nextInt(basicOps.size()));
        OperationType type1 = operandTypes.get(random.nextInt(operandTypes.size()));
        OperationType type2 = operandTypes.get(random.nextInt(operandTypes.size()));

        if (type1 == OperationType.COMMON_FRACTIONS || type2 == OperationType.COMMON_FRACTIONS) {
            return generateCommonFractionProblem(mapBasicToInternalFractionOp(basicOp));
        }
        if (type1 == OperationType.DECIMAL_FRACTIONS || type2 == OperationType.DECIMAL_FRACTIONS) {
            return generateDecimalProblem(mapBasicToInternalDecimalOp(basicOp));
        }

        if (basicOp == OperationType.DIVISION) {
            int answerInt = random.nextInt(10) + 2;
            Operand op2 = generateOperand(type2);
            int op2IntValue = (int) Math.round(op2.value);
            if (op2IntValue == 0) op2IntValue = 2;
            double op1Value = op2IntValue * answerInt;
            Operand op1 = new Operand(integerFormat.format(op1Value), op1Value);
            Operand finalOp2 = new Operand(String.valueOf(op2IntValue), op2IntValue);
            String problemStr = op1.display + " " + getSymbolForOp(basicOp) + " " + finalOp2.display;
            return new Problem(problemStr, String.valueOf(answerInt), basicOp);
        }

        Operand op1 = generateOperand(type1);
        Operand op2 = generateOperand(type2);

        if (op1.isFraction() || op2.isFraction()) {
            return generateCommonFractionProblem(mapBasicToInternalFractionOp(basicOp));
        }

        if (basicOp == OperationType.SUBTRACTION && op1.value < op2.value) {
            Operand temp = op1; op1 = op2; op2 = temp;
        }

        String displayOp = getSymbolForOp(basicOp);
        String problemStr = op1.display + " " + displayOp + " " + op2.display;
        double answerValue = 0;
        switch (basicOp) {
            case ADDITION: answerValue = op1.value + op2.value; break;
            case SUBTRACTION: answerValue = op1.value - op2.value; break;
            case MULTIPLICATION: answerValue = op1.value * op2.value; break;
        }
        return new Problem(problemStr, integerFormat.format(answerValue), basicOp);
    }

    private Operand generateOperand(OperationType type) {
        if (random.nextInt(3) == 0) { // 1 in 3 chance of being a simple integer
            int val = random.nextInt(10) + 1;
            return new Operand(String.valueOf(val), val);
        }
        switch (type) {
            case POWER:
                int base = random.nextInt(5) + 2; int exp = random.nextInt(2) + 2;
                return new Operand(base + "^" + exp, Math.pow(base, exp));
            case SQUARE_ROOT:
                int baseSqrt = random.nextInt(9) + 2;
                return new Operand("√" + (baseSqrt * baseSqrt), baseSqrt);
            case COMMON_FRACTIONS:
                return new Operand(new Fraction(random.nextInt(10)+1, random.nextInt(9)+1));
            case DECIMAL_FRACTIONS:
                double d = (double) (random.nextInt(100) + 1) / 10.0;
                return new Operand(decimalFormat.format(d), d);
            default:
                int fallbackVal = random.nextInt(10) + 1;
                return new Operand(String.valueOf(fallbackVal), fallbackVal);
        }
    }

    private OperationType mapBasicToInternalFractionOp(OperationType basicOp){
        switch (basicOp) { case SUBTRACTION: return OperationType.INTERNAL_FRACTION_SUB; case MULTIPLICATION: return OperationType.INTERNAL_FRACTION_MUL; case DIVISION: return OperationType.INTERNAL_FRACTION_DIV; default: return OperationType.INTERNAL_FRACTION_ADD; }
    }
    private OperationType mapBasicToInternalDecimalOp(OperationType basicOp){
        switch (basicOp) { case SUBTRACTION: return OperationType.INTERNAL_DECIMAL_SUB; case MULTIPLICATION: return OperationType.INTERNAL_DECIMAL_MUL; case DIVISION: return OperationType.INTERNAL_DECIMAL_DIV; default: return OperationType.INTERNAL_DECIMAL_ADD; }
    }
    private String getSymbolForOp(OperationType op) {
        switch (op) { case ADDITION: return "+"; case SUBTRACTION: return "-"; case MULTIPLICATION: return "x"; case DIVISION: return "÷"; default: return "?"; }
    }

    private Problem generateIntegerProblem(OperationType opType) {
        int num1, num2; long answerLong; String problemStr;
        int easyMax = 9, mediumMax = 99, hardMax = 999;
        int currentMax;
        switch (settings.getDifficultyLevel()) {
            case MEDIUM: currentMax = mediumMax; break;
            case HARD:   currentMax = hardMax;   break;
            default:     currentMax = easyMax;   break;
        }
        switch (opType) {
            case ADDITION:
                num1 = random.nextInt(currentMax + 1); num2 = random.nextInt(currentMax + 1);
                answerLong = (long)num1 + num2; problemStr = num1 + " + " + num2;
                break;
            case SUBTRACTION:
                num1 = random.nextInt(currentMax + 1); num2 = random.nextInt(num1 + 1);
                answerLong = (long)num1 - num2; problemStr = num1 + " - " + num2;
                break;
            case MULTIPLICATION:
                int multMax = (settings.getDifficultyLevel() == Settings.Difficulty.EASY) ? 10 : (settings.getDifficultyLevel() == Settings.Difficulty.MEDIUM) ? 20 : 30;
                num1 = random.nextInt(multMax + 1); num2 = random.nextInt(multMax + 1);
                answerLong = (long)num1 * num2; problemStr = num1 + " x " + num2;
                break;
            case POWER:
                int baseMax = (settings.getDifficultyLevel() == Settings.Difficulty.EASY) ? 5 : (settings.getDifficultyLevel() == Settings.Difficulty.MEDIUM) ? 10 : 15;
                int expMax = 3;
                num1 = random.nextInt(baseMax - 1) + 2; if (num1 > 10) expMax = 2;
                num2 = random.nextInt(expMax - 1) + 2;
                answerLong = (long) Math.pow(num1, num2); if (answerLong > Integer.MAX_VALUE) answerLong = Integer.MAX_VALUE;
                problemStr = num1 + " ^ " + num2;
                break;
            default:
                int divAnsMax = (settings.getDifficultyLevel() == Settings.Difficulty.EASY) ? 9 : (settings.getDifficultyLevel() == Settings.Difficulty.MEDIUM) ? 20 : 30;
                num2 = random.nextInt(divAnsMax) + 1; int tempAnswer = random.nextInt(divAnsMax + 1);
                num1 = num2 * tempAnswer; answerLong = tempAnswer;
                problemStr = num1 + " ÷ " + num2;
                break;
        }
        return new Problem(problemStr, String.valueOf(answerLong), opType);
    }

    private Problem generateSquareRootProblem() {
        int baseMax = (settings.getDifficultyLevel() == Settings.Difficulty.EASY) ? 10 : (settings.getDifficultyLevel() == Settings.Difficulty.MEDIUM) ? 20 : 35;
        int base = random.nextInt(baseMax - 1) + 2;
        return new Problem("√" + (base * base), String.valueOf(base), OperationType.SQUARE_ROOT);
    }

    private Problem generateCommonFractionProblem(OperationType opType) {
        Fraction f1, f2, answerFraction; String problemStr;
        int numMax = (settings.getDifficultyLevel() == Settings.Difficulty.EASY) ? 10 : (settings.getDifficultyLevel() == Settings.Difficulty.MEDIUM) ? 50 : 100;
        int denMax = numMax;
        if (settings.getDifficultyLevel() == Settings.Difficulty.EASY) {
            int commonDen = random.nextInt(9) + 2;
            f1 = new Fraction(random.nextInt(numMax + 1), commonDen);
            f2 = new Fraction(random.nextInt(numMax + 1), commonDen);
        } else {
            f1 = new Fraction(random.nextInt(numMax + 1), random.nextInt(denMax - 1) + 1);
            f2 = new Fraction(random.nextInt(numMax + 1), random.nextInt(denMax - 1) + 1);
        }
        if (opType == OperationType.INTERNAL_FRACTION_DIV && f2.getNumerator() == 0) {
            f2 = new Fraction(random.nextInt(10)+1, f2.getDenominator());
        }
        if (opType == OperationType.INTERNAL_FRACTION_SUB && f1.toDouble() < f2.toDouble()) {
            Fraction temp = f1; f1 = f2; f2 = temp;
        }
        switch (opType) {
            case INTERNAL_FRACTION_ADD: answerFraction = f1.add(f2); problemStr = f1.toString() + " + " + f2.toString(); break;
            case INTERNAL_FRACTION_SUB: answerFraction = f1.subtract(f2); problemStr = f1.toString() + " - " + f2.toString(); break;
            case INTERNAL_FRACTION_MUL: answerFraction = f1.multiply(f2); problemStr = f1.toString() + " x " + f2.toString(); break;
            default: answerFraction = f1.divide(f2); problemStr = f1.toString() + " ÷ " + f2.toString(); break;
        }
        return new Problem(problemStr, answerFraction, opType);
    }

    private Problem generateDecimalProblem(OperationType opType) {
        double num1, num2, answer; String problemStr;
        int precisionFactor = (settings.getDifficultyLevel() == Settings.Difficulty.EASY) ? 10 : 100;
        int maxVal = (settings.getDifficultyLevel() == Settings.Difficulty.EASY) ? 100 : (settings.getDifficultyLevel() == Settings.Difficulty.MEDIUM) ? 500 : 1000;
        num1 = (double) (random.nextInt(maxVal)) / precisionFactor;
        switch (opType) {
            case INTERNAL_DECIMAL_ADD:
                num2 = (double) (random.nextInt(maxVal)) / precisionFactor;
                answer = num1 + num2; problemStr = decimalFormat.format(num1) + " + " + decimalFormat.format(num2);
                break;
            case INTERNAL_DECIMAL_SUB:
                num2 = (double) (random.nextInt((int)(num1 * precisionFactor) + 1)) / precisionFactor;
                answer = num1 - num2; problemStr = decimalFormat.format(num1) + " - " + decimalFormat.format(num2);
                break;
            case INTERNAL_DECIMAL_MUL:
                num2 = (double) (random.nextInt(maxVal)) / precisionFactor;
                answer = num1 * num2; problemStr = decimalFormat.format(num1) + " x " + decimalFormat.format(num2);
                break;
            default:
                num2 = (double) (random.nextInt(maxVal / 10 - 1) + 1) / precisionFactor;
                double tempAnswer = random.nextInt(10) + 1;
                num1 = num2 * tempAnswer; answer = tempAnswer;
                problemStr = decimalFormat.format(num1) + " ÷ " + decimalFormat.format(num2);
                break;
        }
        return new Problem(problemStr, decimalFormat.format(answer), answer, opType);
    }
}
