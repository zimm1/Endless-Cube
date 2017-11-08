package com.simonecavazzoni.firstandroidgame.gameEngine.gameProp;

import android.graphics.Rect;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameState.GameStatePlaying;
import com.simonecavazzoni.firstandroidgame.gameEngine.utils.ColorUtils;
import com.simonecavazzoni.firstandroidgame.gameEngine.utils.TupleFloat;
import com.simonecavazzoni.firstandroidgame.utils.UpdateCallback;
import java.util.ArrayList;


public class GamePropPlayer extends GameProp {

    private final static int COLLISION_OFFSET_PERCENT = 20;

    private boolean isBlinking;
    private int blinkingTimes;
    private int blinkingAlpha;
    private boolean blinkingGoingDown;
    private String baseColorWithoutAlpha;
    private UpdateCallback<Void> blinkCallback;

    public GamePropPlayer(Rect surfaceRect) {
        super(surfaceRect);

        resetState();

        baseColorWithoutAlpha = baseColor;
    }

    public void resetState() {
        int w = surfaceRect.width();
        int h = surfaceRect.height();

        float dim = w/GameStatePlaying.COLUMN_NUMBER/3;

        dimension.set(dim, dim);
        position.set(w/2-dimension.getY()/2, h-dimension.getY()*3);
        speed.set((float)0, (float)0);
    }

    public void update(float timeScale, TupleFloat playerInput) {
        super.update(timeScale);

        if (playerInput != null) {
            position.setTuple(playerInput);
            constraintPosition();
        }
    }

    private void constraintPosition() {
        if (position.getX() < 0) {
            position.setX((float)0);
        }
        if (position.getX()+dimension.getX() > surfaceRect.width()) {
            position.setX(surfaceRect.width()-dimension.getX());
        }

        if (position.getY() < 0) {
            position.setY((float)0);
        }
        if (position.getY()+dimension.getY() > surfaceRect.height()) {
            position.setY(surfaceRect.height()-dimension.getY());
        }
    }

    public float collidesWithObstaclesAtPosY(ArrayList<GamePropObstacle> obstacles) {
        for (GamePropObstacle o : obstacles) {
            if (    this.position.getX() < o.position.getX() + o.dimension.getX() &&
                    this.position.getX() + this.dimension.getX() > o.position.getX() &&
                    this.position.getY() + this.dimension.getX()/100* COLLISION_OFFSET_PERCENT < o.position.getY() + o.dimension.getY() &&
                    this.position.getY() + this.dimension.getY() - this.dimension.getX()/100* COLLISION_OFFSET_PERCENT > o.position.getY()) {
                return o.position.getY();
            }
        }

        return -1;
    }

    public void blink(@SuppressWarnings("SameParameterValue") int times) {
        blink(times, null);
    }

    public void blink(int times, UpdateCallback<Void> callback) {
        blinkingTimes = times;
        blinkingAlpha = 255;
        blinkingGoingDown = true;

        blinkCallback = callback;

        isBlinking = true;
    }

    public void updateBlink() {
        if (!isBlinking) {
            return;
        }

        if (blinkingGoingDown) {
            blinkingAlpha -= 32;
            if (blinkingAlpha < 0) {
                blinkingAlpha = 0;
            }
            if (blinkingAlpha == 0) {
                blinkingGoingDown = false;
            }
        } else {
            blinkingAlpha += 32;
            if (blinkingAlpha > 255) {
                blinkingAlpha = 255;
            }
            if (blinkingAlpha == 255) {
                blinkingGoingDown = true;
                blinkingTimes--;
            }
        }

        if (blinkingTimes == 0) {
            isBlinking = false;
            baseColor = baseColorWithoutAlpha;
            if (blinkCallback != null) {
                blinkCallback.onFinish(null);
                blinkCallback = null;
            }
        }

        baseColor = ColorUtils.getColorStringFromAlpha(baseColorWithoutAlpha, blinkingAlpha);

        this.paint.setColor(ColorUtils.getColorFromString(baseColor));
    }

    @Override
    public void setBaseColor(String colorString) {
        super.setBaseColor(colorString);

        baseColorWithoutAlpha = baseColor;
    }
}
