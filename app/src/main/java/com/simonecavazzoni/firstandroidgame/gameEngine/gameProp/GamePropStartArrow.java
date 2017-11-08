package com.simonecavazzoni.firstandroidgame.gameEngine.gameProp;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameState.GameStatePlaying;


public class GamePropStartArrow extends GameProp {

    private Drawable mDrawable;
    private boolean left;

    private Rect drawableRect;

    public GamePropStartArrow(Rect surfaceRect, Drawable drawable, boolean left) {
        super(surfaceRect);

        this.mDrawable = drawable;
        this.left = left;

        onResize(surfaceRect);
    }

    @Override
    public void draw(Canvas c) {
        if (!isVisible()) {
            return;
        }

        if (mDrawable != null) {
            mDrawable.setBounds(drawableRect);
            mDrawable.draw(c);
        }
    }

    @Override
    public void onResize(Rect surfaceRect) {
        super.onResize(surfaceRect);

        int w = surfaceRect.width();
        int h = surfaceRect.height();

        this.dimension.set((float)w/GameStatePlaying.COLUMN_NUMBER,(float)w/GameStatePlaying.COLUMN_NUMBER);
        this.position.set(left ? (float)w/12 : (float)w-w/12-dimension.getX(), (float)h-w/GameStatePlaying.COLUMN_NUMBER-w/GameStatePlaying.COLUMN_NUMBER/3);
        this.drawableRect = new Rect(
                Math.round(position.getX()),
                Math.round(position.getY()),
                Math.round(position.getX() + dimension.getX()),
                Math.round(position.getY() + dimension.getY())
        );
    }
}
