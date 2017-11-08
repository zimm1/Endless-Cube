package com.simonecavazzoni.firstandroidgame.gameEngine.gameState;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.simonecavazzoni.firstandroidgame.gameEngine.GameEngine;
import com.simonecavazzoni.firstandroidgame.gameEngine.GameInput;


public abstract class GameState {

    private static final float TOUCH_MARGIN = 200;

    @SuppressWarnings("unused")
    static boolean gameInputIsInsideMargin(GameInput input, Rect surfaceRect) {
        return input.getEvent().getX() >= TOUCH_MARGIN
                && input.getEvent().getX() <= surfaceRect.width()-TOUCH_MARGIN
                && input.getEvent().getY() >= TOUCH_MARGIN
                && input.getEvent().getY() <= surfaceRect.height()-TOUCH_MARGIN;
    }

    protected Rect surfaceRect;
    private GameEngine.STATE nextState;
    @SuppressWarnings("WeakerAccess")
    protected Context mContext;

    @SuppressWarnings("WeakerAccess")
    public GameState(Context context, Rect surfaceRect) {
        this.surfaceRect = surfaceRect;

        this.mContext = context;

        nextState = null;
    }

    public void draw(Canvas c) {}

    public void update(float timeScale) {}

    public synchronized boolean onInput(GameInput input) {
        return false;
    }

    public void onResize(Rect surfaceRect) {
        this.surfaceRect = surfaceRect;
    }

    public GameEngine.STATE getNextState() {
        return this.nextState;
    }

    @SuppressWarnings("WeakerAccess")
    protected void setNextState(GameEngine.STATE nextState) {
        this.nextState = nextState;
    }
}
