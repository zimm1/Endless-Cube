package com.simonecavazzoni.firstandroidgame.gameEngine;

import android.view.MotionEvent;


public class GameInput {
    public static final int TAP_START = 1;
    public static final int TAP_UPDATE = 2;
    public static final int TAP_END = 3;
    public static final int TAP_MULTI_START = 4;
    public static final int TAP_MULTI_UPDATE = 5;

    private int type;
    private MotionEvent event;

    public GameInput(int inputType, MotionEvent event) {
        setType(inputType);
        setEvent(event);
    }

    public int getType() {
        return type;
    }
    public MotionEvent getEvent() {
        return event;
    }

    private void setType(int type) {
        this.type = type;
    }
    private void setEvent(MotionEvent event) {
        this.event = event;
    }
}
