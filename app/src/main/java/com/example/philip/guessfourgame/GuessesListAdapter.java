/*
 * Copyright (c) 2017. Created by Philip Joseph Thomas
 */

package com.example.philip.guessfourgame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * List adapter for guess results list view
 */
class GuessesListAdapter extends BaseAdapter {


    // application context
    private Context mCtxt;
    // the list of guesses and hints
    private List<GuessResult> mGuessResults;


    // create new list adapter and set results
    GuessesListAdapter(Context c, List<GuessResult> results) {
        mCtxt = c;
        mGuessResults = new ArrayList<>();
        for (int i = results.size() - 1; i >= 0; i--) {
            mGuessResults.add(results.get(i));
        }

    }


    @Override
    public int getCount() {
        return mGuessResults.size();
    }

    @Override
    public Object getItem(int position) {
        return mGuessResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem;

        // recycle existing views
        if (convertView == null) {

            // get the layout inflater
            LayoutInflater inflater = LayoutInflater.from(mCtxt);
            // inflate the list item view
            listItem = inflater.inflate(R.layout.guesses_list_item, parent, false);
        } else {
            listItem = convertView;
        }
        // get the reference to the name text view
        TextView guessedNumber = (TextView) listItem.findViewById(R.id.guessed_number);
        // get the reference to the address text view
        TextView hints = (TextView) listItem.findViewById(R.id.hints);
        //set the text with dealer name at respective position
        String guessedItem = "Turn: "
                + mGuessResults.get(position).getTurn()
                + "\nGuessed number: "
                + mGuessResults.get(position).getGuess();
        guessedNumber.setText(guessedItem);
        String hintsItem = "Correct positions: "
                + mGuessResults.get(position).getRightPos()
                + "\nWrong positions: "
                + mGuessResults.get(position).getWrongPos();
        hints.setText(hintsItem);
        return listItem;
    }
}
