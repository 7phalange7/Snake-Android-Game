package com.example.snakegame;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;

import androidx.annotation.Nullable;

public class GameActivity extends Activity {


    SnakeGame snakeGame;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // to get pixel dimensions of mobile screen
        Display display = getWindowManager().getDefaultDisplay();

        //get dimensions in a Point object
        Point size = new Point();
        display.getSize(size);


        snakeGame = new SnakeGame(this,size);


        setContentView(snakeGame);
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeGame.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeGame.resume();
    }
}
