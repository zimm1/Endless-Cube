package com.simonecavazzoni.firstandroidgame.gameEngine.gameProp;

import android.graphics.Rect;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameState.GameStatePlaying;


public class GamePropPointsLine extends GameProp {

    private static float LINE_HEIGHT = 10;

    public GamePropPointsLine(Rect surfaceRect, float posY) {
        super(surfaceRect);

        position.set((float)0, posY - LINE_HEIGHT/2);
        speed.set((float)0, GameStatePlaying.OBSTACLES_BASE_SPEED);

        //noinspection SpellCheckingInspection
        setBaseColor("cccccc");

        onResize(surfaceRect);
    }

    @Override
    public void onResize(Rect surfaceRect) {
        super.onResize(surfaceRect);

        dimension.set((float)surfaceRect.width(), LINE_HEIGHT);
    }

    public void update(float timeScale, float obstacleSpeed) {
        speed.setY(obstacleSpeed);
        super.update(timeScale);
    }
}
