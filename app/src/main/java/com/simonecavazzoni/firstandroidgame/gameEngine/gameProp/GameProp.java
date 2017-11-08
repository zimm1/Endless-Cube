package com.simonecavazzoni.firstandroidgame.gameEngine.gameProp;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.simonecavazzoni.firstandroidgame.gameEngine.GameInput;
import com.simonecavazzoni.firstandroidgame.gameEngine.utils.ColorUtils;
import com.simonecavazzoni.firstandroidgame.gameEngine.utils.TupleFloat;


public abstract class GameProp {

    private static final String BASE_COLOR = "000000";

    Paint paint;
    public TupleFloat position;
    public TupleFloat dimension;
    @SuppressWarnings("WeakerAccess")
    public TupleFloat speed;

    protected Rect surfaceRect;
    @SuppressWarnings("WeakerAccess")
    protected String baseColor;

    private boolean isVisible;

    protected boolean inputStarted;
    protected boolean hover;

    GameProp(Rect surfaceRect) {
        position = new TupleFloat();
        dimension = new TupleFloat();
        speed = new TupleFloat();
        paint = new Paint();

        this.surfaceRect = surfaceRect;

        isVisible = true;
        inputStarted = false;
        hover = false;

        setBaseColor(BASE_COLOR);
    }

    public void update(float timeScale) {
        updatePosition(timeScale);
        this.paint.setColor(ColorUtils.getColorFromString(baseColor));
    }

    private void updatePosition(float timeScale) {
        position.setX(position.getX() + speed.getX()*timeScale);
        position.setY(position.getY() + speed.getY()*timeScale);
    }

    public void draw(Canvas c) {
        if (!isVisible) {
            return;
        }

        c.drawRect(
                position.getX(),
                position.getY(),
                position.getX() + dimension.getX(),
                position.getY() + dimension.getY(),
                paint
        );
    }

    public boolean onInput(GameInput input) {
        return false;
    }

    public void onResize(Rect surfaceRect) {
        this.surfaceRect = surfaceRect;
    }

    public void setBaseColor(String colorString) {
        this.baseColor = colorString;
        this.paint.setColor(ColorUtils.getColorFromString(baseColor));
    }

    protected boolean collidesWithPoint(TupleFloat point) {
        return point.getX() >= position.getX()
                && point.getX() <= position.getX()+dimension.getX()
                && point.getY() >= position.getY()
                && point.getY() <= position.getY()+dimension.getY();
    }

    @SuppressWarnings("WeakerAccess")
    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }
}
