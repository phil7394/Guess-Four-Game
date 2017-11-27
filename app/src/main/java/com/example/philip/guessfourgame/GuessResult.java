/*
 * Copyright (c) 2017. Created by Philip Joseph Thomas
 */

package com.example.philip.guessfourgame;

/**
 * Guess result POJO
 */
class GuessResult {

    // player turn
    private int turn;
    // guessed number
    private String guess;
    // right positions
    private int rightPos;
    // wrong positions
    private int wrongPos;

    private GuessResult(int turn, String guess, int rightPos, int wrongPos) {
        this.turn = turn;
        this.guess = guess;
        this.rightPos = rightPos;
        this.wrongPos = wrongPos;
    }

    /**
     * get the guess results for a guess
     *
     * @param turn
     * @param secretNumber
     * @param nextGuess
     * @return
     */
    static GuessResult getGuessResults(int turn, String secretNumber, String nextGuess) {

        int rPos = 0;
        int wPos = 0;
        int[] numbers = new int[10];
        for (int i = 0; i < secretNumber.length(); i++) {
            int s = Character.getNumericValue(secretNumber.charAt(i));
            int g = Character.getNumericValue(nextGuess.charAt(i));
            if (s == g) rPos++;
            else {
                if (numbers[s] < 0) wPos++;
                if (numbers[g] > 0) wPos++;
                numbers[s]++;
                numbers[g]--;
            }
        }
        return new GuessResult(turn, nextGuess, rPos, wPos);

    }

    int getTurn() {
        return turn;
    }


    String getGuess() {
        return guess;
    }


    int getRightPos() {
        return rightPos;
    }


    int getWrongPos() {
        return wrongPos;
    }

}
