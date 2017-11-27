/*
 * Copyright (c) 2017. Created by Philip Joseph Thomas
 */

package com.example.philip.guessfourgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Common utilities class
 */
class Utils {

    /**
     * generates four digit random number, with all digits unique
     *
     * @return
     */
    static String generateRandomNumber() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        String secretNumber = "";
        for (int i = 0; i < 4; i++) {
            secretNumber += numbers.get(i).toString();
        }
        return secretNumber;
    }

    /**
     * constants class
     */
    static class Constants {
        static final int CONTINUE = 1;
        static final int NEXT_GUESS = 2;
        static final int GUESS_RESULTS = 3;
        static final String INIT_SECRET_STRING = "_ _ _ _";
        static final String PLAYER_WON = "PLAYER_WON";
        static final String TURNS = "TURNS";
        static final String PREV_TURN = "PREV_TURN";
        static final String PLAYER_ONE_WON = "Player 1 won!";
        static final String PLAYER_TWO_WON = "Player 2 won!";
        static final String NO_WINNER = "Game Over! No winner!";
        static final String PLAYER_ONE = "PLAYER 1";
        static final String PLAYER_TWO = "PLAYER 2";
    }
}
