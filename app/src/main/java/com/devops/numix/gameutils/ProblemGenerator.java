package com.devops.numix.gameutils;

import com.devops.numix.data.OperationType;
import com.devops.numix.data.Problem;
import com.devops.numix.data.Settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.text.DecimalFormat;


public class ProblemGenerator {
    private Random random;
    private Settings settings;
    private DecimalFormat decimalFormat; // For potential future decimal problems


    public ProblemGenerator(Settings settings) {
        this.settings = settings;
        this.random = new Random();
        this.decimalFormat = new DecimalFormat("#.##"); // Format to max 2 decimal places
    }

    public Problem generateProblem() {
        List<OperationType> availableOps = new ArrayList<>();
        Set<OperationType> enabledOpsDirect = settings.getEnabledOperations(); // The set from settings

        // Populate availableOps based on what's directly enabled in settings
        if (enabledOpsDirect.contains(OperationType.ADDITION)) availableOps.add(OperationType.ADDITION);
        if (enabledOpsDirect.contains(OperationType.SUBTRACTION)) availableOps.add(OperationType.SUBTRACTION);
        if (enabledOpsDirect.contains(OperationType.MULTIPLICATION)) availableOps.add(OperationType.MULTIPLICATION);
        if (enabledOpsDirect.contains(OperationType.DIVISION)) availableOps.add(OperationType.DIVISION);
        if (enabledOpsDirect.contains(OperationType.POWER)) availableOps.add(OperationType.POWER);

        // If the FRACTIONS category switch is on, add specific fraction operations to the pool
        if (enabledOpsDirect.contains(OperationType.FRACTIONS)) {
            availableOps.add(OperationType.FRACTION_ADD);
            availableOps.add(OperationType.FRACTION_SUBTRACT);
            availableOps.add(OperationType.FRACTION_MULTIPLY);
            availableOps.add(OperationType.FRACTION_DIVIDE);
        }


        if (availableOps.isEmpty()) {
            // This case should ideally be prevented by SettingsActivity validation
            // Fallback to simple addition if somehow no operations are selected
            return generateIntegerProblem(OperationType.ADDITION);
        }

        OperationType selectedOp = availableOps.get(random.nextInt(availableOps.size()));

        switch (selectedOp) {
            case ADDITION:
            case SUBTRACTION:
            case MULTIPLICATION:
            case DIVISION:
            case POWER:
                return generateIntegerProblem(selectedOp);
            case FRACTION_ADD:
            case FRACTION_SUBTRACT:
            case FRACTION_MULTIPLY:
            case FRACTION_DIVIDE:
                return generateFractionProblem(selectedOp);
            default:
                // Should not be reached if availableOps is populated correctly
                return generateIntegerProblem(OperationType.ADDITION); // Fallback
        }
    }

    private Problem generateIntegerProblem(OperationType opType) {
        int num1, num2;
        long answerLong; // Use long for intermediate power calc to avoid overflow before casting
        String problemStr;

        int easyMax = 9;
        int mediumMax = 99;
        int hardMax = 999;
        int currentMax;

        switch (settings.getDifficultyLevel()) {
            case MEDIUM: currentMax = mediumMax; break;
            case HARD: currentMax = hardMax; break;
            case EASY:
            default: currentMax = easyMax; break;
        }

        switch (opType) {
            case ADDITION:
                num1 = random.nextInt(currentMax + 1);
                num2 = random.nextInt(currentMax + 1);
                answerLong = (long)num1 + num2;
                problemStr = num1 + " + " + num2;
                break;
            case SUBTRACTION:
                num1 = random.nextInt(currentMax + 1);
                num2 = random.nextInt(num1 + 1); // Ensures num1 >= num2, so result is non-negative
                answerLong = (long)num1 - num2;
                problemStr = num1 + " - " + num2;
                break;
            case MULTIPLICATION:
                int multMax;
                if (settings.getDifficultyLevel() == Settings.Difficulty.EASY) multMax = 10; // e.g., up to 9x9 or 10x_
                else if (settings.getDifficultyLevel() == Settings.Difficulty.MEDIUM) multMax = 20; // e.g., up to 20x20 approx
                else multMax = 30; // e.g. up to 30x30 approx (results up to 900)

                num1 = random.nextInt(multMax + 1);
                num2 = random.nextInt(multMax + 1);
                answerLong = (long)num1 * num2;
                problemStr = num1 + " " + OperationType.MULTIPLICATION.getSymbol() + " " + num2;
                break;
            case DIVISION:
                int divAnsMax; // Max for the answer (quotient)
                int divNum2Max; // Max for the divisor
                if (settings.getDifficultyLevel() == Settings.Difficulty.EASY) {divAnsMax = 9; divNum2Max = 9;}
                else if (settings.getDifficultyLevel() == Settings.Difficulty.MEDIUM) {divAnsMax = 20; divNum2Max = 20;}
                else {divAnsMax = 30; divNum2Max = 30;}

                num2 = random.nextInt(divNum2Max) + 1; // Divisor (num2) cannot be 0, range 1 to divNum2Max
                int tempAnswer = random.nextInt(divAnsMax + 1); // Answer
                num1 = num2 * tempAnswer; // Dividend, ensures whole number result
                answerLong = tempAnswer;
                problemStr = num1 + " " + OperationType.DIVISION.getSymbol() + " " + num2;
                break;
            case POWER:
                int baseMax, expMax;
                if (settings.getDifficultyLevel() == Settings.Difficulty.EASY) { baseMax = 5; expMax = 3;} // e.g. 5^2=25, 2^3=8
                else if (settings.getDifficultyLevel() == Settings.Difficulty.MEDIUM) { baseMax = 10; expMax = 3;} // e.g. 10^2=100, 7^3=343, 3^3=27
                else { baseMax = 15; expMax = 3; } // e.g. 15^2=225, 5^3=125, 9^3=729

                num1 = random.nextInt(baseMax -1) + 2; // Base (2 to baseMax)

                // Ensure exponent keeps result within reasonable integer limits (approx < 1000 for hard)
                if (num1 > 10 && expMax > 2) expMax = 2; // For bases > 10, limit exp to 2
                if (num1 > 5 && num1 <=10 && expMax > 3) expMax = 3; // For bases 6-10, limit exp to 3
                if (num1 > 30) expMax = 1; // if base somehow gets very large, exp = 1 or 2

                num2 = random.nextInt(expMax) + ( (num1 > 5 && expMax > 2 && expMax !=1 ) ? 1 : 2); // Exponent (usually 2 to expMax, or 1 if base is large)
                if (num1 > 7 && num2 > 2 && num1 <=30) num2 = 2; // Cap exponent for larger bases
                if (num1 > 30 && num2 > 1) num2 = 1; // Make exponent 1 for very large bases
                if (num2 == 0 && expMax > 0) num2 = 1; // Avoid exponent 0 unless it's the only option
                if (expMax == 1) num2 = 1;


                answerLong = (long) Math.pow(num1, num2);
                if (answerLong > 2000 && num2 > 1) { // If result too big, try reducing exponent
                    num2 = (num2 > 1) ? num2 -1 : 1;
                    if (num2 == 0 && expMax > 0) num2 = 1;
                    answerLong = (long) Math.pow(num1, num2);
                }
                if (answerLong > 2000 && num1 > 10) { // If still too big, reduce base
                    num1 = random.nextInt(10-2)+2;
                    answerLong = (long) Math.pow(num1, num2);
                }


                problemStr = num1 + " ^ " + num2;
                break;
            default: // Should not happen
                num1 = 1; num2 = 1; answerLong = 2; problemStr = "1 + 1";
        }
        // Cast to int for Problem constructor if it fits, otherwise handle large numbers (not required by prompt)
        int finalAnswerInt = (int) answerLong; // Assuming results fit in int as per "100-999 for hard"
        return new Problem(problemStr, String.valueOf(finalAnswerInt), finalAnswerInt, opType);
    }

    private Problem generateFractionProblem(OperationType opType) {
        Fraction f1, f2, answerFraction;
        String problemStr;

        int numEasyMax = 10; int denEasyRange = 10; // Denominators from 2 to 10
        int numMediumMax = 50; int denMediumRange = 50;
        int numHardMax = 100; int denHardRange = 100;

        switch (settings.getDifficultyLevel()) {
            case EASY:
                int commonDenominator = random.nextInt(denEasyRange - 1) + 2; // Denominator from 2 to 10
                int num1_easy = random.nextInt(numEasyMax + 1); // Numerator 0-10
                int num2_easy;
                do {
                    num2_easy = random.nextInt(numEasyMax + 1);
                } while (num1_easy == num2_easy && numEasyMax > 0); // Ensure different numerators if possible
                f1 = new Fraction(num1_easy, commonDenominator);
                f2 = new Fraction(num2_easy, commonDenominator);
                break;
            case MEDIUM:
                f1 = new Fraction(random.nextInt(numMediumMax + 1), random.nextInt(denMediumRange -1) + 1); // Denom 1 to 50
                f2 = new Fraction(random.nextInt(numMediumMax + 1), random.nextInt(denMediumRange -1) + 1);
                break;
            case HARD:
            default:
                f1 = new Fraction(random.nextInt(numHardMax + 1), random.nextInt(denHardRange -1) + 1); // Denom 1 to 100
                f2 = new Fraction(random.nextInt(numHardMax + 1), random.nextInt(denHardRange -1) + 1);
                break;
        }

        // Ensure non-zero divisor for fraction division and valid operands for subtraction
        switch (opType) {
            case FRACTION_DIVIDE:
                while (f2.getNumerator() == 0) { // Regenerate f2 if its numerator is zero
                    if (settings.getDifficultyLevel() == Settings.Difficulty.EASY) {
                        f2 = new Fraction(random.nextInt(numEasyMax) + 1, f1.getDenominator()); // Keep common denom, num 1-10
                    } else if (settings.getDifficultyLevel() == Settings.Difficulty.MEDIUM) {
                        f2 = new Fraction(random.nextInt(numMediumMax) + 1, random.nextInt(denMediumRange -1) + 1);
                    } else {
                        f2 = new Fraction(random.nextInt(numHardMax) + 1, random.nextInt(denHardRange -1) + 1);
                    }
                }
                break;
            case FRACTION_SUBTRACT:
                // Ensure f1 >= f2 to avoid negative results for fractions, as per "Fractional or negative results are not allowed"
                if (f1.toDouble() < f2.toDouble()) {
                    Fraction temp = f1;
                    f1 = f2;
                    f2 = temp;
                }
                // If after swap, f1 == f2, and it's easy mode, try to make them different if possible
                if (f1.equals(f2) && settings.getDifficultyLevel() == Settings.Difficulty.EASY) {
                    int commonDen = f1.getDenominator();
                    int n1 = f1.getNumerator();
                    int n2;
                    if (n1 > 0) n2 = random.nextInt(n1); // n2 < n1
                    else if (commonDen > 1) { // if n1 is 0, make f1 slightly larger
                        f1 = new Fraction(1, commonDen); // e.g. 1/5 - 0/5
                        n2 = 0;
                    } else { // if n1 is 0 and commonDen is 1 (0/1 - 0/1)
                        f1 = new Fraction(random.nextInt(numEasyMax-1)+1, commonDen); // 1/X - 0/X
                        n2 = 0;
                    }
                    f2 = new Fraction(n2, commonDen);
                    if (f1.toDouble() < f2.toDouble()) { Fraction temp = f1; f1 = f2; f2 = temp; } // re-ensure
                }
                break;
        }


        switch (opType) {
            case FRACTION_ADD:
                answerFraction = f1.add(f2);
                problemStr = f1.toString() + " + " + f2.toString();
                break;
            case FRACTION_SUBTRACT:
                answerFraction = f1.subtract(f2); // f1 is already >= f2
                problemStr = f1.toString() + " - " + f2.toString();
                break;
            case FRACTION_MULTIPLY:
                answerFraction = f1.multiply(f2);
                problemStr = f1.toString() + " " + opType.getSymbol() + " " + f2.toString();
                break;
            case FRACTION_DIVIDE:
                answerFraction = f1.divide(f2); // f2 numerator is not zero
                problemStr = f1.toString() + " " + opType.getSymbol() + " " + f2.toString();
                break;
            default: // Should not happen
                answerFraction = new Fraction(1,2);
                problemStr = "1/2 + 0/1";
        }
        // The answerFraction is already simplified by its constructor.
        // "If correct answer in common fractions is greater than 1, it must be reduced."
        // This means simplified (e.g., 4/2 to 2, or 6/4 to 3/2). Fraction.toString() handles 4/2 -> "2".
        return new Problem(problemStr, answerFraction, opType);
    }
}
