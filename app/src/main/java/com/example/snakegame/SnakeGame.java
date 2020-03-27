package com.example.snakegame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceView;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;

import java.util.Random;

public class SnakeGame extends SurfaceView implements Runnable {

    private Thread thread = null;
    private Context context;

    public enum Direction {UP, RIGHT, DOWN, LEFT}

    // Start in the right direction
    private Direction direction = Direction.RIGHT;

    // screen size in pixels
    private int screenX;
    private int screenY;

    private int snakeLength;
    private int foodX;
    private int foodY;

    //size of snake segment in pixels
    private int blockSize;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int numBlocksHigh;

    private long nextFrameTime;
    private final long FPS = 10;
    private final long MILLIS_PER_SECOND = 1000;

    private int score;

    // The location in the grid of all the segments
    private int[] snakeXs;
    private int[] snakeYs;

    private volatile boolean isPlaying;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Paint paint;


    public SnakeGame(Context context, Point size) {
        super(context);

        screenX = size.x;
        screenY = size.y;

        blockSize = screenX / NUM_BLOCKS_WIDE;
        numBlocksHigh = screenY / blockSize;

        surfaceHolder = getHolder();
        paint = new Paint();

        snakeXs = new int[200];
        snakeYs = new int[200];

        // Start the game
        newGame();


    }


    @Override
    public void run() {

        while (isPlaying) {


            if (updateRequired()) {
                update();
                draw();
            }

        }
    }

    public void pause() {
        isPlaying = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }

    public void resume() {
        isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }


    public void newGame() {

        snakeLength = 1;
        snakeXs[0] = NUM_BLOCKS_WIDE / 2;
        snakeYs[0] = numBlocksHigh / 2;


        spawnFood();


        score = 0;

        // Setup nextFrameTime so an update is triggered
        nextFrameTime = System.currentTimeMillis();
    }

    public void spawnFood() {
        Random random = new Random();
        foodX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1; //random value between 0 + 1 and NUM_BLOCKS_WIDE
        foodY = random.nextInt(numBlocksHigh - 1) + 1;
    }

    private void eatFood() {
        snakeLength++;
        spawnFood();
        score = score + 1;
    }

    private void moveSnake() {
        // Move the body
        for (int i = snakeLength; i > 0; i--) {
            snakeXs[i] = snakeXs[i - 1];
            snakeYs[i] = snakeYs[i - 1];

        }

        // Move the head in the appropriate direction
        switch (direction) {
            case UP:
                snakeYs[0]--;
                break;

            case RIGHT:
                snakeXs[0]++;
                break;

            case DOWN:
                snakeYs[0]++;
                break;

            case LEFT:
                snakeXs[0]--;
                break;
        }
    }


    private boolean detectDeath() {

        boolean dead = false;

        // Hit the screen edge
        if (snakeXs[0] == -1) dead = true;
        if (snakeXs[0] >= NUM_BLOCKS_WIDE) dead = true;
        if (snakeYs[0] == -1) dead = true;
        if (snakeYs[0] == numBlocksHigh) dead = true;

        // Eaten itself?
        for (int i = snakeLength - 1; i > 0; i--) {
            if ((i > 4) && (snakeXs[0] == snakeXs[i]) && (snakeYs[0] == snakeYs[i])) {
                dead = true;
            }
        }

        return dead;
    }

    public void update() {
        // Did the head of the snake eat food?
        if (snakeXs[0] == foodX && snakeYs[0] == foodY) {
            eatFood();
        }

        moveSnake();

        if (detectDeath()) {
            //start again
            newGame();
        }
    }


    public void draw() {
        // Get a lock on the canvas
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();


            canvas.drawColor(Color.argb(255, 126, 228, 100));

            // Set the color of the paint to draw the snake white
            paint.setColor(Color.argb(255, 0, 0, 0));

            // Scale the HUD text
            paint.setTextSize(90);
            canvas.drawText("Score:" + score, 10, 70, paint);

            // Draw the snake one block at a time
            for (int i = 0; i < snakeLength; i++) {
                canvas.drawRect(snakeXs[i] * blockSize,
                        (snakeYs[i] * blockSize),
                        (snakeXs[i] * blockSize) + blockSize,
                        (snakeYs[i] * blockSize) + blockSize,
                        paint);
            }

            // Set the color of the paint to draw Food red
            paint.setColor(Color.argb(255, 255, 0, 0));

            // Draw Food
            canvas.drawRect(foodX * blockSize,
                    (foodY * blockSize),
                    (foodX * blockSize) + blockSize,
                    (foodY * blockSize) + blockSize,
                    paint);

            // Unlock the canvas and reveal the graphics for this frame
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    public boolean updateRequired() {


        if (nextFrameTime <= System.currentTimeMillis()) {

            nextFrameTime = System.currentTimeMillis() + MILLIS_PER_SECOND / FPS;

            // Return true so that the update and draw
            // functions are executed
            return true;
        }

        return false;
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (motionEvent.getX() >= screenX / 2) {
                    switch (direction) {
                        case UP:
                            direction = Direction.RIGHT;
                            break;
                        case RIGHT:
                            direction = Direction.DOWN;
                            break;
                        case DOWN:
                            direction = Direction.LEFT;
                            break;
                        case LEFT:
                            direction = Direction.UP;
                            break;
                    }
                } else {
                    switch (direction) {
                        case UP:
                            direction = Direction.LEFT;
                            break;
                        case LEFT:
                            direction = Direction.DOWN;
                            break;
                        case DOWN:
                            direction = Direction.RIGHT;
                            break;
                        case RIGHT:
                            direction = Direction.UP;
                            break;
                    }
                }
        }
        return true;
    }


}
