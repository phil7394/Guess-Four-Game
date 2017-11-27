/*
 * Copyright (c) 2017. Created by Philip Joseph Thomas
 */

package com.example.philip.guessfourgame;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * The Main activity for the Guess Four Game
 */
public class GameMainActivity extends AppCompatActivity {

    // field for win status (0 = no winner, 1 = player 1 won, 2 = player 2 won)
    public int playerWon = 0;
    // field for turns status
    public int turns = 1;
    // field to store previous turn
    public int prevTurn = -1;
    // the handler for UI thread
    public Handler mainHandler = new Handler();
    // player one thread
    private PlayerOneFragment.PlayerOneHandlerThread playerOneHandlerThread;
    // player two thread
    private PlayerTwoFragment.PlayerTwoHandlerThread playerTwoHandlerThread;
    // player one handler
    private Handler playerOneHandler;
    //player two handler
    private Handler playerTwoHandler;
    //player one fragment
    private PlayerOneFragment playerOneFragment;
    //player two fragment
    private PlayerTwoFragment playerTwoFragment;


    /**
     * activity onCreate
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // restore states
        if (savedInstanceState != null) {
            playerWon = savedInstanceState.getInt(Utils.Constants.PLAYER_WON);
            turns = savedInstanceState.getInt(Utils.Constants.TURNS);
            prevTurn = savedInstanceState.getInt(Utils.Constants.PREV_TURN);
        }
        setContentView(R.layout.activity_game_main);
        // start button
        Button startBtn = (Button) findViewById(R.id.start_button);

        //get player one fragment
        playerOneFragment = (PlayerOneFragment) getFragmentManager().findFragmentById(R.id.player1_fragment);
        //get player two fragment
        playerTwoFragment = (PlayerTwoFragment) getFragmentManager().findFragmentById(R.id.player2_fragment);

        // update fragment views, when configuration changes
        playerOneFragment.updateFragmentView();
        playerTwoFragment.updateFragmentView();

        // set start button listener
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("### GameMainActivity: ", "START button clicked");
                // clear main handler message queue
                mainHandler.removeCallbacksAndMessages(null);
                // clear and quit any running player loopers
                stopLoopers();
                // kill any  running player threads
                stopThreads();

                // clear fragment views
                playerOneFragment.clearViews();
                playerTwoFragment.clearViews();

                // generate random secret number for each player
                playerOneFragment.generateSecretNumber();
                playerTwoFragment.generateSecretNumber();

                // get new player threads
                playerOneFragment.initializeHandlerThread();
                playerTwoFragment.initializeHandlerThread();
                playerOneHandlerThread = playerOneFragment.playerOneHandlerThread;
                playerTwoHandlerThread = playerTwoFragment.playerTwoHandlerThread;

                //start player threads
                playerOneHandlerThread.start();
                playerTwoHandlerThread.start();

                // set states if game was restarted
                playerWon = 0;
                turns = 1;
                prevTurn = -1;

                // get player one handler
                playerOneHandler = playerOneHandlerThread.playerOneHandler;
                // if player one handler was not ready, wait till it is ready
                while (playerOneHandler == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    playerOneHandler = playerOneHandlerThread.playerOneHandler;
                }

                // get player two handler
                playerTwoHandler = playerTwoHandlerThread.playerTwoHandler;
                // if player one handler was not ready, wait till it is ready
                while (playerTwoHandler == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    playerTwoHandler = playerTwoHandlerThread.playerTwoHandler;
                }

                // let player one wait 2s
                playerOneHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });
                // let player two wait 2s
                playerTwoHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });
                // manage the game
                manageGame();

            }
        });

    }

    /**
     * on save instance state
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(Utils.Constants.PLAYER_WON, playerWon);
        Log.i("### onSaveState:", "Turns = " + turns);
        outState.putInt(Utils.Constants.TURNS, turns);
        outState.putInt(Utils.Constants.PREV_TURN, prevTurn);

        super.onSaveInstanceState(outState);

    }

    /**
     * Manages the game by using playerWon, turns and prevTurn.
     */
    public void manageGame() {

        Log.i("### manageGame():", "Turn = " + turns);
        // if no player has won yet and turns < 40
        if (playerWon == 0 && turns < 40) {
            // if both players played their turns
            if (turns == prevTurn + 2) {
                // get turn value for each player for total turns
                int playerTurn = turns - ((turns - 1) / 2);

                if (playerOneHandler == null) {
                    playerOneHandlerThread = playerOneFragment.playerOneHandlerThread;
                    playerOneHandler = playerOneHandlerThread.playerOneHandler;
                }
                if (playerTwoHandler == null) {
                    playerTwoHandlerThread = playerTwoFragment.playerTwoHandlerThread;
                    playerTwoHandler = playerTwoHandlerThread.playerTwoHandler;
                }
                // send CONTINUE message to play next turn
                Message msgOne = playerOneHandler.obtainMessage(Utils.Constants.CONTINUE, 0, 0, playerTurn);
                playerOneHandler.sendMessage(msgOne);
                Message msgTwo = playerTwoHandler.obtainMessage(Utils.Constants.CONTINUE, 0, 0, playerTurn);
                playerTwoHandler.sendMessage(msgTwo);

                // store previous turn
                prevTurn = turns;
            }
        } else {
            if (playerWon == 1) {
                // player one won
                Toast.makeText(GameMainActivity.this, Utils.Constants.PLAYER_ONE_WON, Toast.LENGTH_LONG).show();

            } else if (playerWon == 2) {
                //player two won
                Toast.makeText(GameMainActivity.this, Utils.Constants.PLAYER_TWO_WON, Toast.LENGTH_LONG).show();

            } else if (turns >= 40) {
                // turns limit reached, no winner
                Toast.makeText(GameMainActivity.this, Utils.Constants.NO_WINNER, Toast.LENGTH_LONG).show();

            }
            // kill threads
            stopThreads();
            // clear and stop loopers
            stopLoopers();

        }

    }

    /**
     * kill any running player threads
     */
    private void stopThreads() {
        if (playerOneHandlerThread != null && playerOneHandlerThread.isAlive()) {
            Log.i("### GameMainActivity:", "interrupting playerOneHandlerThread");
            playerOneHandlerThread.interrupt();
        }
        if (playerTwoHandlerThread != null && playerTwoHandlerThread.isAlive()) {
            Log.i("### GameMainActivity:", "interrupting playerTwoHandlerThread");
            playerTwoHandlerThread.interrupt();
        }

    }

    /**
     * clear message queues and quit loopers
     */
    private void stopLoopers() {

        if ((playerOneHandlerThread != null) && (playerOneHandlerThread.isAlive())) {
            Log.i("### GameMainActivity:", "cleared playerOneHandler queue");
            playerOneHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (playerOneHandler != null) {
                        playerOneHandler.removeCallbacksAndMessages(null);
                        playerOneHandler.getLooper().quit();
                    }

                }
            });
        }
        if ((playerTwoHandlerThread != null) && (playerTwoHandlerThread.isAlive())) {
            Log.i("### GameMainActivity:", "cleared playerTwoHandler queue");
            playerTwoHandler.post(new Runnable() {
                @Override
                public void run() {

                    playerTwoHandler.removeCallbacksAndMessages(null);
                    playerTwoHandler.getLooper().quit();

                }
            });
        }

    }
}
