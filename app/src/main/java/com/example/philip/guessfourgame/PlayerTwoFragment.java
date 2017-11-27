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
import java.util.Collections;
import java.util.List;

/**
 * Player two fragmnet
 */
public class PlayerTwoFragment extends Fragment {

    // secret number
    public String secretNumber = Utils.Constants.INIT_SECRET_STRING;
    // player two guess results list
    public List<GuessResult> playerTwoResults = new ArrayList<>();
    // player two thread
    public PlayerTwoHandlerThread playerTwoHandlerThread;


    public PlayerTwoFragment() {
        // Required empty public constructor
    }

    /**
     * clear fragment views
     */
    public void clearViews() {
        playerTwoResults = new ArrayList<>();
        if (getView() != null) {
            TextView secretNumberView = (TextView) getView().findViewById(R.id.secret_number);
            secretNumberView.setText(Utils.Constants.INIT_SECRET_STRING);
            secretNumberView.invalidate();
            ListView guessesList = (ListView) getView().findViewById(R.id.guesses_list);
            guessesList.setAdapter(new GuessesListAdapter(getContext(), playerTwoResults));
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
        if (playerTwoHandlerThread == null) {
            playerTwoHandlerThread = new PlayerTwoHandlerThread("Player Two", ((GameMainActivity) getActivity()).mainHandler);
        }
        if (getView() != null) {
            TextView player = (TextView) getView().findViewById(R.id.player);
            player.setText(Utils.Constants.PLAYER_TWO);
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
            guessesList.setAdapter(new GuessesListAdapter(getContext(), playerTwoResults));
        }

    }

    /**
     * initialize new thread
     */
    public void initializeHandlerThread() {
        playerTwoHandlerThread = new PlayerTwoHandlerThread("Player Two", ((GameMainActivity) getActivity()).mainHandler);
    }


    /**
     * Player one handler thread inner class
     */
    class PlayerTwoHandlerThread extends HandlerThread {

        //player two handler
        Handler playerTwoHandler;
        //player one handler
        private Handler playerOneHandler;
        // main handler
        private Handler mainHandler;
        // guess result
        private GuessResult gResult;
        // 4 x 10 table to store possible digits for each position
        private List<List<Integer>> table = new ArrayList<>();

        // create player two thread
        PlayerTwoHandlerThread(String name, Handler mainHandler) {
            super(name);
            // set main handler
            this.mainHandler = mainHandler;
            // initialize table
            initializeTable();

        }

        /**
         * initialize table with digits 0-9 for all 4 positions
         */
        private void initializeTable() {

            for (int j = 0; j < 4; j++) {
                List<Integer> posList = new ArrayList<>();

                for (int i = 0; i < 10; i++) {
                    posList.add(i);
                }
                table.add(posList);
            }

        }

        /**
         * delete all four digits of previous guess,
         * in all four positions of the table
         *
         * @param prevGuess
         */
        private void deleteAllDigits(String prevGuess) {
            int posDigit;
            for (int i = 0; i < 4; i++) {
                posDigit = Character.getNumericValue(prevGuess.charAt(i));
                for (List<Integer> digitsList : table) {
                    int indexToRemove = digitsList.indexOf(posDigit);
                    if (indexToRemove != -1) {
                        digitsList.remove(indexToRemove);
                    }
                }
            }

        }

        /**
         * delete digits in previous guess from table in respective positions
         *
         * @param prevGuess
         */
        private void deleteRespDigits(String prevGuess) {
            int posDigit;
            for (int i = 0; i < 4; i++) {
                posDigit = Character.getNumericValue(prevGuess.charAt(i));
                int indexToRemove = table.get(i).indexOf(posDigit);
                if (indexToRemove != -1) {
                    table.get(i).remove(indexToRemove);
                }
            }
        }

        /**
         * delete all other digits except for digits present in previous guess,
         * in all positions of the table
         *
         * @param prevGuess
         */
        private void deleteOtherDigits(String prevGuess) {
            int posDigit;
            List<Integer> exclusionList = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                exclusionList.add(i);
            }
            for (int i = 0; i < 4; i++) {
                posDigit = Character.getNumericValue(prevGuess.charAt(i));
                int indexToRemove = exclusionList.indexOf(posDigit);
                if (indexToRemove != -1) {
                    exclusionList.remove(exclusionList.indexOf(posDigit));
                }
            }

            for (List<Integer> digitsList : table) {
                digitsList.removeAll(exclusionList);
            }
        }

        /**
         * Player 2 Strategy:
         * Next guess is generated by using the previous guess results.
         * If prev guess had 0 right positions,
         *  then, if prev guess had 0 wrong positions
         *      then delete all digits in the guess from table
         *  otherwise, if prev guess had < 4 wrong positions
         *      then delete digits at resp. position from table
         *  otherwise, if prev guess had 4 wrong positions
         *      then delete all other digits, except digits in the guess,
         *      in all positions of the table
         *
         *  pick a random digit from each position of the table
         *
         * @param prevGuessResult
         * @return
         */
        String generateNextGuess(GuessResult prevGuessResult) {
            StringBuilder nextGuess = new StringBuilder();
            if (prevGuessResult == null) {
                nextGuess.append(Utils.generateRandomNumber());
            } else {

                String prevGuess = prevGuessResult.getGuess();

                int prevRightPos = prevGuessResult.getRightPos();
                int prevWrongPos = prevGuessResult.getWrongPos();
                if (prevRightPos == 0) {
                    if (prevWrongPos == 0) {
                        deleteAllDigits(prevGuess);
                    } else if (prevWrongPos < 4) {
                        deleteRespDigits(prevGuess);
                    } else if (prevWrongPos == 4) {
                        deleteOtherDigits(prevGuess);
                    }
                }

                for (List<Integer> digitsList : table) {
                    Collections.shuffle(digitsList);
                    boolean digitAppended = false;
                    for (Integer digit : digitsList) {
                        if (!(nextGuess.toString().contains(String.valueOf(digit)))) {
                            digitAppended = true;
                            nextGuess.append(digit);
                            break;
                        }
                    }
                    if (!digitAppended) {

                        for (int i = 0; i < 10; i++) {
                            if (!nextGuess.toString().contains(String.valueOf(i))) {
                                nextGuess.append(i);
                                break;
                            }
                        }
                    }
                }
            }

            return nextGuess.toString();
        }

        /**
         * run thread
         */
        @Override
        public void run() {
            Log.i("### P_2_HndlrThrd: ", "run()");
            super.run();
        }

        /**
         * on looper prepared
         */
        @Override
        protected void onLooperPrepared() {
            Log.i("### P_2_HndlrThrd: ", "onLooperPrepared()");
            playerTwoHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Log.i("### P_2_HndlrThrd: ", "handleMessage()");
                    PlayerOneFragment.PlayerOneHandlerThread playerOneHandlerThread = ((PlayerOneFragment) getFragmentManager().findFragmentById(R.id.player1_fragment)).playerOneHandlerThread;
                    switch (msg.what) {
                        // continue to player two's next turn
                        case Utils.Constants.CONTINUE:

                            int postTurn = (Integer) msg.obj;
                            String nGuess = generateNextGuess(gResult);
                            // build message object containing next guess and turn
                            String[] postMsgObjStrArray = {nGuess, String.valueOf(postTurn)};
                            // send next guess to player one
                            if (playerOneHandlerThread != null && playerOneHandlerThread.isAlive()) {
                                playerOneHandler = playerOneHandlerThread.playerOneHandler;
                                Message guessMsg = playerOneHandler.obtainMessage(Utils.Constants.NEXT_GUESS, 0, 0, postMsgObjStrArray);
                                playerOneHandler.sendMessage(guessMsg);
                            }
                            break;
                        // process player one's next guess for player two's secret number
                        case Utils.Constants.NEXT_GUESS:
                            Log.i("### P_2_HndlrThrd: ", "NEXT_GUESS");
                            String[] getMsgObjStrArray = (String[]) msg.obj;
                            // get next guess from message object
                            String nextGuess = getMsgObjStrArray[0];
                            // get turn from message object
                            int getTurn = Integer.parseInt(getMsgObjStrArray[1]);
                            // get the guess results
                            GuessResult guessResult = GuessResult.getGuessResults(getTurn, secretNumber, nextGuess);
                            Log.i("### P_2_HndlrThrd: ", "guessResult.getRightPos() = " + guessResult.getRightPos());

                            // send the guess results to player one
                            if (playerOneHandlerThread != null && playerOneHandlerThread.isAlive()) {
                                playerOneHandler = playerOneHandlerThread.playerOneHandler;
                                Message resultMsg = playerOneHandler.obtainMessage(Utils.Constants.GUESS_RESULTS, 0, 0, guessResult);
                                playerOneHandler.sendMessage(resultMsg);
                            }

                            break;
                        // get the guess results of player two's guess for player one's secret number
                        case Utils.Constants.GUESS_RESULTS:
                            Log.i("### P_2_HndlrThrd: ", "GUESS_RESULTS");
                            Log.i("### P_2_HndlrThrd: ", "GUESS_RESULTS msg.obj = " + msg.obj);
                            gResult = (GuessResult) msg.obj;
                            // add guess result to the list
                            playerTwoResults.add(gResult);

                            // if player two got all right positions, set playerWon as 2
                            if (gResult.getRightPos() == 4) {
                                mainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        ((GameMainActivity) getActivity()).playerWon = 2;

                                    }

                                });
                            }

                            // update the list view with new appended list
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i("### P_2_HndlrThrd: ", "update guesses_list on mainHandler");
                                    ListView guesses_list = (ListView) getActivity().getFragmentManager().findFragmentById(R.id.player2_fragment).getView().findViewById(R.id.guesses_list);
                                    guesses_list.setAdapter(new GuessesListAdapter(getContext(), playerTwoResults));
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
                                Log.i("### P_2_HndlrThrd: ", "Thread interrupted");
                            }
                            break;
                        default:
                            Log.i("### P_2_HndlrThrd: ", "default");
                            super.handleMessage(msg);
                    }

                }

            };
        }

    }

}
