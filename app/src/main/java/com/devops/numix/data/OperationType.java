package com.devops.numix.data;

public enum OperationType {
    // Operations selected by the user in Settings
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION,
    POWER,
    SQUARE_ROOT,
    COMMON_FRACTIONS,
    DECIMAL_FRACTIONS,

    // Internal types used by the generator to create specific problems
    INTERNAL_FRACTION_ADD,
    INTERNAL_FRACTION_SUB,
    INTERNAL_FRACTION_MUL,
    INTERNAL_FRACTION_DIV,

    INTERNAL_DECIMAL_ADD,
    INTERNAL_DECIMAL_SUB,
    INTERNAL_DECIMAL_MUL,
    INTERNAL_DECIMAL_DIV
}
