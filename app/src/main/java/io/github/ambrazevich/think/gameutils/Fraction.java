package io.github.ambrazevich.think.gameutils;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Objects;

public class Fraction implements Serializable {
    private static final long serialVersionUID = 1L;
    private int numerator;
    private int denominator;

    public Fraction(int numerator, int denominator) {
        if (denominator == 0) {
            throw new IllegalArgumentException("Denominator cannot be zero.");
        }
        this.numerator = numerator;
        this.denominator = denominator;
        simplify();
    }

    public int getNumerator() {
        return numerator;
    }

    public int getDenominator() {
        return denominator;
    }

    private void simplify() {
        if (denominator < 0) { // Ensure denominator is positive
            numerator = -numerator;
            denominator = -denominator;
        }
        if (numerator == 0) {
            denominator = 1; // Canonical form for zero
            return;
        }
        int commonDivisor = gcd(Math.abs(numerator), Math.abs(denominator));
        numerator /= commonDivisor;
        denominator /= commonDivisor;

        // Reduce mixed numbers if numerator > denominator (e.g., 7/2 becomes 3 1/2 for display, but stored as 7/2)
        // The problem states "If correct answer in common fractions is greater than 1, it must be reduced."
        // This typically means simplifying (e.g. 2/4 to 1/2) which is already done.
        // If it means converting improper to mixed, that's a display concern, not storage.
        // The current toString() handles 5/1 as "5".
        // For "reduced" meaning simplified, we are good.
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    public Fraction add(Fraction other) {
        int newNumerator = this.numerator * other.denominator + other.numerator * this.denominator;
        int newDenominator = this.denominator * other.denominator;
        return new Fraction(newNumerator, newDenominator);
    }

    public Fraction subtract(Fraction other) {
        int newNumerator = this.numerator * other.denominator - other.numerator * this.denominator;
        int newDenominator = this.denominator * other.denominator;
        return new Fraction(newNumerator, newDenominator);
    }

    public Fraction multiply(Fraction other) {
        int newNumerator = this.numerator * other.numerator;
        int newDenominator = this.denominator * other.denominator;
        return new Fraction(newNumerator, newDenominator);
    }

    public Fraction divide(Fraction other) {
        if (other.numerator == 0) {
            throw new ArithmeticException("Cannot divide by zero fraction.");
        }
        int newNumerator = this.numerator * other.denominator;
        int newDenominator = this.denominator * other.numerator;
        return new Fraction(newNumerator, newDenominator);
    }

    // Power for fractions (integer exponent)
    public Fraction power(int exponent) {
        if (exponent == 0) {
            return new Fraction(1, 1);
        }
        if (exponent < 0) {
            if (numerator == 0) throw new ArithmeticException("Cannot raise zero fraction to negative power.");
            return new Fraction((int) Math.pow(denominator, -exponent), (int) Math.pow(numerator, -exponent));
        }
        return new Fraction((int) Math.pow(numerator, exponent), (int) Math.pow(denominator, exponent));
    }


    public double toDouble() {
        return (double) numerator / denominator;
    }

    @NonNull
    @Override
    public String toString() {
        if (denominator == 1) {
            return String.valueOf(numerator); // 5/1 becomes 5
        }
        // For improper fractions like 7/2, it will be displayed as "7/2".
        // If "reduced" meant mixed numbers (e.g., 3 1/2), this would need to change.
        // Based on typical math game phrasing, "reduced" usually means simplified (e.g., 2/4 to 1/2).
        return numerator + "/" + denominator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Fraction fraction = (Fraction) o;
        // Simplified fractions should be equal if their num/den match
        return numerator == fraction.numerator && denominator == fraction.denominator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator);
    }

    // Static method to parse a string like "n/d" or "n" into a Fraction
    public static Fraction parseFraction(String s) {
        if (s == null || s.trim().isEmpty()) {
            throw new NumberFormatException("Input string is null or empty.");
        }
        s = s.trim();
        if (s.contains("/")) {
            String[] parts = s.split("/");
            if (parts.length == 2) {
                try {
                    int num = Integer.parseInt(parts[0].trim());
                    int den = Integer.parseInt(parts[1].trim());
                    return new Fraction(num, den);
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Invalid fraction format: " + s);
                }
            } else {
                throw new NumberFormatException("Invalid fraction format: " + s);
            }
        } else {
            try {
                int num = Integer.parseInt(s);
                return new Fraction(num, 1); // Treat as whole number
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Invalid number format: " + s);
            }
        }
    }
}
