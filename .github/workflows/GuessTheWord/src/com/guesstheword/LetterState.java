package com.guesstheword;

public enum LetterState {
    EMPTY,
    CORRECT,      // Green - right letter, right position
    PRESENT,      // Yellow - right letter, wrong position
    ABSENT        // Red - letter not in word
}