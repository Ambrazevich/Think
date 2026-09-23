package io.github.ambrazevich.think.gameutils;

import io.github.ambrazevich.think.data.OperationType;
import io.github.ambrazevich.think.data.Problem;
import io.github.ambrazevich.think.data.Settings;

import org.junit.Test;

import java.math.BigDecimal;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

public class ProblemGeneratorTest {

    @Test
    public void decimalProblemsKeepExactAnswers() {
        Settings settings = new Settings();
        settings.setDifficultyLevel(Settings.Difficulty.HARD);
        settings.setEnabledOperations(EnumSet.of(OperationType.DECIMAL_FRACTIONS));
        ProblemGenerator generator = new ProblemGenerator(settings);
        EnumSet<OperationType> generatedTypes = EnumSet.noneOf(OperationType.class);

        for (int i = 0; i < 500; i++) {
            Problem problem = generator.generateProblem();
            generatedTypes.add(problem.getOperationType());
            assertTrue(expectedDecimalAnswer(problem)
                    .compareTo(new BigDecimal(problem.getCorrectAnswerString())) == 0);
        }

        assertTrue(generatedTypes.contains(OperationType.INTERNAL_DECIMAL_ADD));
        assertTrue(generatedTypes.contains(OperationType.INTERNAL_DECIMAL_SUB));
        assertTrue(generatedTypes.contains(OperationType.INTERNAL_DECIMAL_MUL));
        assertTrue(generatedTypes.contains(OperationType.INTERNAL_DECIMAL_DIV));
    }

    @Test
    public void compoundDivisionKeepsComplexOperandVisible() {
        Settings settings = new Settings();
        settings.setEnabledOperations(EnumSet.of(OperationType.DIVISION, OperationType.POWER));

        Problem problem = new ProblemGenerator(settings).generateProblem();

        assertTrue(problem.getDisplayEquation().contains("^"));
        assertTrue(problem.getDisplayEquation().contains("÷"));
    }

    private BigDecimal expectedDecimalAnswer(Problem problem) {
        String equation = problem.getDisplayEquation();
        if (equation.contains(" + ")) {
            BigDecimal[] operands = operands(equation, " \\+ ");
            return operands[0].add(operands[1]);
        }
        if (equation.contains(" - ")) {
            BigDecimal[] operands = operands(equation, " - ");
            return operands[0].subtract(operands[1]);
        }
        if (equation.contains(" x ")) {
            BigDecimal[] operands = operands(equation, " x ");
            return operands[0].multiply(operands[1]);
        }
        BigDecimal[] operands = operands(equation, " ÷ ");
        return operands[0].divide(operands[1]);
    }

    private BigDecimal[] operands(String equation, String separator) {
        String[] parts = equation.split(separator);
        return new BigDecimal[]{new BigDecimal(parts[0]), new BigDecimal(parts[1])};
    }
}
