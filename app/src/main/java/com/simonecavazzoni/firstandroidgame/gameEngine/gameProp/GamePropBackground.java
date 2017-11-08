package com.simonecavazzoni.firstandroidgame.gameEngine.gameProp;

import android.graphics.Rect;

import com.simonecavazzoni.firstandroidgame.utils.UpdateCallback;

public class GamePropBackground extends GameProp {

    private static final float ANIMATION_SPEED_Y = 70;

    private int percentDimY;
    private boolean isAnimationGoing = false;
    private int percentDimYTo;
    private float animationSpeed;
    private UpdateCallback<Float> animationCallback;

    public GamePropBackground(Rect surfaceRect, int percentDimY, String colorString) {
        super(surfaceRect);

        this.dimension.setX((float)surfaceRect.width());
        this.dimension.setY((float)surfaceRect.height()/100*percentDimY);
        setPercentDimY(percentDimY);

        setBaseColor(colorString);
    }

    @Override
    public void update(float timeScale) {
        super.update(timeScale);

        if (isAnimationGoing) {
            updateAnimation();
        }
    }

    private void updateAnimation() {
        this.dimension.setY(this.dimension.getY()+animationSpeed);

        float targetHeight = surfaceRect.height()/100* percentDimYTo;

        this.animationCallback.onUpdate(dimension.getY());

        if ((animationSpeed > 0 && dimension.getY() >= targetHeight)
                || (animationSpeed < 0 && dimension.getY() <= targetHeight)) {
            this.speed.setY((float)0);
            this.dimension.setY(targetHeight);
            this.isAnimationGoing = false;
            this.percentDimY = percentDimYTo;
            this.percentDimYTo = 0;
            this.animationSpeed = 0;

            this.animationCallback.onFinish(dimension.getY());
            this.animationCallback = null;
        }
    }

    private void setDimYFromPercent() {
        this.dimension.setY((float)surfaceRect.height()/100* percentDimY);
    }

    @SuppressWarnings("WeakerAccess")
    public void setPercentDimY(int percentDimY) {
        this.percentDimY = percentDimY;
        setDimYFromPercent();
    }

    public void setPercentDimY(int percentDimY, UpdateCallback<Float> callback) {
        startAnimationToPercent(percentDimY, callback);
    }

    private void startAnimationToPercent(int percentDimYTo, UpdateCallback<Float> callback) {
        this.percentDimYTo = percentDimYTo;
        animationSpeed = percentDimYTo > percentDimY ? ANIMATION_SPEED_Y : -ANIMATION_SPEED_Y;
        this.animationCallback = callback;

        isAnimationGoing = true;
    }

    @Override
    public void onResize(Rect surfaceRect) {
        super.onResize(surfaceRect);

        this.dimension.setX((float)surfaceRect.width());

        if (!isAnimationGoing) {
            setDimYFromPercent();
        }
    }
}
