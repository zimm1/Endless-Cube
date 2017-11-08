package com.simonecavazzoni.firstandroidgame.gameEngine.gameProp;

import android.graphics.Rect;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameState.GameStatePlaying;
import com.simonecavazzoni.firstandroidgame.gameEngine.utils.TupleFloat;

public class GamePropObstacle extends GameProp {

    public GamePropObstacle(Rect surfaceRect, TupleFloat position) {
        super(surfaceRect);

        int w = surfaceRect.width();

        this.position.setTuple(position);
        dimension.set((float)w/ GameStatePlaying.COLUMN_NUMBER, (float)w/GameStatePlaying.COLUMN_NUMBER);
        speed.set((float)0, GameStatePlaying.OBSTACLES_BASE_SPEED);
    }

    public void update(float timeScale, float obstacleSpeed) {
        speed.setY(obstacleSpeed);
        super.update(timeScale);
    }

    public boolean isOutOfSurface(Rect surfaceRect) {
        return position.getY() > surfaceRect.height();
    }
}
