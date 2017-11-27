/*
 * Copyright (c) 2017. Created by Philip Joseph Thomas
 */

package com.example.philip.guessfourgame;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Player one fragment.
 */
public class PlayerOneFragment extends Fragment {

    // secret number
    public String secretNumber = Utils.Constants.INIT_SECRET_STRING;
    // list of guess results
    public List<GuessResult> playerOneResults = new ArrayList<>();
    // player one thread
    public PlayerOneHandlerThread playerOneHandlerThread;


    public PlayerOneFragment() {
        // Required empty public constructor
    }

    /**
     * clear fragment views
     */
    public void clearViews() {

        playerOneResults = new ArrayList<>();
        if (getView() != null) {
            TextView secretNumberView = (TextView) getView().findViewById(R.id.secret_number);
            secretNumberView.setText(Utils.Constants.INIT_SECRET_STRING);
            secretNumberView.invalidate();
            ListView guessesList = (ListView) getView().findViewById(R.id.guesses_list);
            guessesList.setAdapter(new GuessesListAdapter(getContext(), playerOneResults));
        }
    }

    /**
     * generate and set random secret number
     */
    public void generateSecretNumber() {
        secretNumber = Utils.generateRandomNumber();
        final String secretStr = "Secret number:\n" + secretNumber;
        if (getView() != null) {
            TextView secretNumberView = (TextView) getView().findViewById(R.id.secret_number);
            secretNumberView.setText(secretStr);
            secretNumberView.invalidate();
        }
    }

    /**
     * on create view
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.player_frame_layout, container, false);
    }

    /**
     * on attach
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        setRetainInstance(true);

    }


    /**
     * on activity created
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (playerOneHandlerThread == null) {
            playerOneHandlerThread = new PlayerOneHandlerThread("Player One", ((GameMainActivity) getActivity()).mainHandler);
        }
        if (getView() != null) {
            TextView player = (TextView) getView().findViewById(R.id.player);
            player.setText(Utils.Constants.PLAYER_ONE);

        }

    }

    /**
     * update fragment view, after configuration changes
     */
    public void updateFragmentView() {
        final String secretStr = "Secret number:\n" + secretNumber;
        if (getView() != null) {
            TextView secretNumberView = (TextView) getView().findViewById(R.id.secret_number);
            secretNumberView.setText(secretStr);
            secretNumberView.invalidate();
            ListView guessesList = (ListView) getView().findViewById(R.id.guesses_list);
            guessesList.setAdapter(new GuessesListAdapter(getContext(), playerOneResults));
        }

    }

    /**
     * initialize new thread
     */
    public void initializeHandlerThread() {
        playerOneHandlerThread = new PlayerOneHandlerThread("Player One", ((GameMainActivity) getActivity()).mainHandler);
    }

    /**
     * Player one handler thread inner class
     */
    class PlayerOneHandlerThread extends HandlerThread {

        //player one handler
        Handler playerOneHandler;
        //player two handler
        private Handler playerTwoHandler;
        //main handler
        private Handler mainHandler;
        // guess result
        private GuessResult gResult;
        // set of all numbers between 0000 and 9999, such that digits are unique
        private Set<String> allNumbers = new HashSet<>();

        // create player one thread
        private PlayerOneHandlerThread(String name, Handler mainHandler) {
            super(name);
            // set main handler
            this.mainHandler = mainHandler;
            // generate all numbers between 0000 and 9999, such that digits are unique
            generateAllNumbers();
        }

        /**
         * generate all numbers between 0000 and 9999,
         * such that digits are unique.
         */
        private void generateAllNumbers() {

            Log.i("### P_1_HndlrThrd:", "generateAllNumbers");

            Set<String> numbersToRemove = new HashSet<>();

            for (int i = 100; i < 1000; i++) {

                allNumbers.add("0" + String.valueOf(i));
            }
            for (int i = 1000; i < 10000; i++) {
                allNumbers.add(String.valueOf(i));
            }
            for (String number : allNumbers) {

                Set<Character> uniqueDigits;
                uniqueDigits = new HashSet<>();
                for (int i = 0; i < 4; i++) {

                    uniqueDigits.add(number.charAt(i));

                }
                if (uniqueDigits.size() != 4) {
                    numbersToRemove.add(number);
                }
            }

            allNumbers.removeAll(numbersToRemove);
        }

        /**
         * Player 1 Strategy:
         * Next guess is generated by pruning the set allNumbers using the previous guess result,
         * such that only numbers with same results as previous guess are retained,
         * while others are removed. The next guess will be a random set of four digits
         * from the pruned set.
         *
         * @param prevGuessResult
         * @param turn
         * @return
         */
        String generateNextGuess(GuessResult prevGuessResult, int turn) {
            Log.i("### P_1_HndlrThrd: ", "generate next guess");

            String nextGuess;
            if (prevGuessResult == null) {
                nextGuess = Utils.generateRandomNumber();
            } else {
                Set<String> numbersToRemove = new HashSet<>();
                String prevGuess = prevGuessResult.getGuess();
                int prevRightPos = prevGuessResult.getRightPos();
                int prevWrongPos = prevGuessResult.getWrongPos();

                for (String possibleNumber : allNumbers) {

                    GuessResult gResult = GuessResult.getGuessResults(turn, prevGuess, possibleNumber);
                    if ((gResult.getRightPos() != prevRightPos) || (gResult.getWrongPos() != prevWrongPos)) {
                        numbersToRemove.add(possibleNumber);
                    }
                }
                allNumbers.removeAll(numbersToRemove);

                nextGuess = allNumbers.iterator().next();
            }
            return nextGuess;
        }

        /**
         * run thread
         */
        @Override
        public void run() {
            Log.i("### P_1_HndlrThrd: ", "run()");
            super.run();
        }

        /**
         * on looper prepared
         */
        @Override
        protected void onLooperPrepared() {
            Log.i("### P_1_HndlrThrd: ", "onLooperPrepared()");
            playerOneHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Log.i("### P_1_HndlrThrd: ", "handleMessage()");
                    PlayerTwoFragment.PlayerTwoHandlerThread playerTwoHandlerThread = ((PlayerTwoFragment) getFragmentManager().findFragmentById(R.id.player2_fragment)).playerTwoHandlerThread;
                    switch (msg.what) {
                        // continue to player one's next turn
                        case Utils.Constants.CONTINUE:

                            int postTurn = (Integer) msg.obj;
                            String nGuess = generateNextGuess(gResult, postTurn);
                            // build message object containing next guess and turn
                            String[] postMsgObjStrArray = {nGuess, String.valueOf(postTurn)};

                            // send next guess to player two
                            if (playerTwoHandlerThread != null && playerTwoHandlerThread.isAlive()) {
                                playerTwoHandler = playerTwoHandlerThread.playerTwoHandler;
                                Message guessMsg = playerTwoHandler.obtainMessage(Utils.Constants.NEXT_GUESS, 0, 0, postMsgObjStrArray);
                                playerTwoHandler.sendMessage(guessMsg);
                            }
                            break;
                        // process player two's next guess for player one's secret number
                        case Utils.Constants.NEXT_GUESS:
                            Log.i("### P_1_HndlrThrd: ", "NEXT_GUESS");
                            String[] getMsgObjStrArray = (String[]) msg.obj;
                            // get next guess from message object
                            String nextGuess = getMsgObjStrArray[0];
                            // get turn from message object
                            int getTurn = Integer.parseInt(getMsgObjStrArray[1]);
                            // get the guess results
                            GuessResult guessResult = GuessResult.getGuessResults(getTurn, secretNumber, nextGuess);

                            // send the guess results to player two
                            if (playerTwoHandlerThread != null && playerTwoHandlerThread.isAlive()) {
                                playerTwoHandler = playerTwoHandlerThread.playerTwoHandler;
                                Message resultMsg = playerTwoHandler.obtainMessage(Utils.Constants.GUESS_RESULTS, 0, 0, guessResult);
                                playerTwoHandler.sendMessage(resultMsg);
                            }
                            break;
                        // get the guess results of player one's guess for player two's secret number
                        case Utils.Constants.GUESS_RESULTS:
                            Log.i("### P_1_HndlrThrd: ", "GUESS_RESULTS");
                            gResult = (GuessResult) msg.obj;
                            // add guess result to the list
                            playerOneResults.add(gResult);

                            // if player one got all right positions, set playerWon as 1
                            if (gResult.getRightPos() == 4) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        ((GameMainActivity) getActivity()).playerWon = 1;

                                    }

                                });

                            }

                            // update the list view with new appended list
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i("### P_1_HndlrThrd: ", "update guesses_list on mainHandler");

                                    ListView guesses_list = (ListView) getActivity().getFragmentManager().findFragmentById(R.id.player1_fragment).getView().findViewById(R.id.guesses_list);
                                    guesses_list.setAdapter(new GuessesListAdapter(getContext(), playerOneResults));
                                    // increment turns count
                                    ((GameMainActivity) getActivity()).turns++;
                                    // call manage game
                                    ((GameMainActivity) getActivity()).manageGame();
                                }
                            });

                            // delay after each turn
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                Log.i("### P_1_HndlrThrd: ", "Thread interrupted");
                            }
                            break;
                        default:
                            Log.i("### P_1_HndlrThrd: ", "default");
                            super.handleMessage(msg);
                    }

                }
            };

        }

    }
}
