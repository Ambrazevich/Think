package com.devops.numix.data;

public enum OperationType {
    ADDITION("+"),
    SUBTRACTION("-"),
    MULTIPLICATION("x"),
    DIVISION("÷"), // For display, actual calculation might use /
    POWER("^"),
    FRACTIONS("FRACTIONS_CATEGORY"), // Represents the general switch for all fraction operations
    FRACTION_ADD("+f"), // Using different symbols internally to distinguish if needed
    FRACTION_SUBTRACT("-f"),
    FRACTION_MULTIPLY("xf"),
    FRACTION_DIVIDE("÷f");


    private final String symbol;

    OperationType(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        // For display in problem string, might want to use the standard symbols
        if (this == FRACTION_ADD) return "+";
        if (this == FRACTION_SUBTRACT) return "-";
        if (this == FRACTION_MULTIPLY) return "x";
        if (this == FRACTION_DIVIDE) return "÷";
        return symbol;
    }
}
