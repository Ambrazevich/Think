package io.github.ambrazevich.think.gameutils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FractionTest {

    @Test
    public void simplifiesAndNormalizesSign() {
        assertEquals(new Fraction(1, 2), new Fraction(4, 8));
        assertEquals(new Fraction(-1, 2), new Fraction(1, -2));
        assertEquals("0", new Fraction(0, 7).toString());
    }

    @Test
    public void parsesEquivalentAnswers() {
        assertEquals(new Fraction(3, 2), Fraction.parseFraction("6/4"));
        assertEquals(new Fraction(5, 1), Fraction.parseFraction("5"));
    }
}
