package com.simonecavazzoni.firstandroidgame;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.simonecavazzoni.firstandroidgame.gameEngine.GameEngine;
import com.simonecavazzoni.firstandroidgame.gameEngine.GameInput;
import java.util.Timer;
import java.util.TimerTask;


public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    public static final int FPS = 60;

    public GameEngine gameEngine;
    private Timer gameLoopTimer;
    public boolean isGameLoopGoing = false;

    private TimerTask gameLoopTask;

    private int targetMillis = 1000/FPS;
    private int lastTime;
    private int targetTime;


    public MySurfaceView(Context context) {
        super(context);
        initializeSurface();
    }
    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeSurface();
    }
    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeSurface();
    }

    private void initializeSurface () {
    }

    private TimerTask getGameLoopTask() {
        return new TimerTask() {
            @Override
            public void run() {
                int current = (int)System.currentTimeMillis();
                if (current < targetTime) return;
                float timeScale = 1;

                lastTime = current;
                targetTime = (current+ targetMillis)-(current-targetTime);

                // Game logic

                if (isGameLoopGoing && gameEngine != null) {
                    Canvas c = getHolder().lockCanvas();
                    if (c != null) {
                        gameEngine.gameLoop(timeScale, c);
                    }
                    try {
                        getHolder().unlockCanvasAndPost(c);
                    } catch (Exception e) {
                        //Log.e("Error", "IllegalState", e);
                    }
                }
            }
        };
    }

    public synchronized void startGameLoop() {
        if (!isGameLoopGoing) {
            lastTime = (int) System.currentTimeMillis();
            targetTime = lastTime + targetMillis;

            gameLoopTimer = new Timer("loopTimer", true);

            if (gameLoopTask != null) {
                gameLoopTask.cancel();
            }
            gameLoopTask = getGameLoopTask();
            gameLoopTimer.schedule(gameLoopTask, 0, 1);

            isGameLoopGoing = true;
        }
    }

    public synchronized void stopGameLoop() {
        if (isGameLoopGoing) {
            gameLoopTimer.cancel();
            gameLoopTimer.purge();
            gameLoopTimer = null;
            gameLoopTask.cancel();
            gameLoopTask = null;

            isGameLoopGoing = false;
        }
    }

    private void onResize(Rect surfaceRect) {
        if (gameEngine != null) {
            gameEngine.onResize(surfaceRect);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {}

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        onResize(surfaceHolder.getSurfaceFrame());
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public synchronized boolean onTouchEvent(MotionEvent event) {
        int type;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                type = GameInput.TAP_START;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                type = GameInput.TAP_MULTI_START;
                break;
            case MotionEvent.ACTION_MOVE:
                type = event.getPointerId(0) == 0 ?
                        GameInput.TAP_UPDATE : GameInput.TAP_MULTI_UPDATE;
                break;
            case MotionEvent.ACTION_UP:
                type = GameInput.TAP_END;
                break;
            default:
                return false;
        }

        return gameEngine.onInput(new GameInput(type, event));
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return false;
    }
}
