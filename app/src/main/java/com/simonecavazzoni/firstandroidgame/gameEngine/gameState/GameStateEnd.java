package com.simonecavazzoni.firstandroidgame.gameEngine.gameState;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.sax.EndElementListener;

import com.simonecavazzoni.firstandroidgame.R;
import com.simonecavazzoni.firstandroidgame.gameEngine.GameEngine;
import com.simonecavazzoni.firstandroidgame.gameEngine.GameInput;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameProp.GamePropBackground;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameProp.GamePropButton;
import com.simonecavazzoni.firstandroidgame.gameEngine.utils.TupleFloat;
import com.simonecavazzoni.firstandroidgame.utils.UpdateCallback;

public class GameStateEnd extends GameState {

    private static final int BACKGROUND_PERCENT_DIM_Y = 60;

    private GamePropBackground background;
    private GamePropButton replayButton;
    private GamePropButton homeButton;

    private int score;
    private int maxScore;

    private Paint textPaint;

    private boolean isStarting;
    private boolean isFinishing = false;

    public GameStateEnd(Context context, Rect surfaceRect, int score, int maxScore, String backgroundPrimaryColor,  boolean interstitialShowed) {
        super(context, surfaceRect);

        this.score = score;
        this.maxScore = maxScore;

        isStarting = true;

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);

        background = new GamePropBackground(surfaceRect, 0, backgroundPrimaryColor);
        replayButton = new GamePropButton(surfaceRect,
                new TupleFloat(surfaceRect.width()/2, (float)surfaceRect.height()/100*0),
                GamePropButton.SIZE.BIG, GamePropButton.TYPE.REPLAY,
                context.getResources().getDrawable(R.drawable.ic_replay_black));
        homeButton = new GamePropButton(surfaceRect,
                new TupleFloat(),
                GamePropButton.SIZE.MEDIUM, GamePropButton.TYPE.HOME,
                context.getResources().getDrawable(R.drawable.ic_home_black));

        onResize(surfaceRect);

        if (!interstitialShowed) {
            startStartingAnimation();
        } else {
            replayButton.setVisible(false);
        }
    }

    private void startStartingAnimation() {
        background.setPercentDimY(BACKGROUND_PERCENT_DIM_Y, new UpdateCallback<Float>() {
            @Override
            public void onUpdate(Float object) {
                replayButton.onResize(surfaceRect, object);
            }

            @Override
            public void onFinish(Float object) {
                replayButton.onResize(surfaceRect, object);
                isStarting = false;
            }
        });
    }

    @Override
    public void onResize(Rect surfaceRect) {
        super.onResize(surfaceRect);

        background.onResize(surfaceRect);

        replayButton.onResize(surfaceRect, background.dimension.getY());

        float homeButtonPos = homeButton.dimension.getX() + 50;
        homeButton.position.set(homeButtonPos, homeButtonPos);
        homeButton.onResize(surfaceRect);
    }

    @Override
    public void update(float timeScale) {
        super.update(timeScale);

        background.update(timeScale);
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);

        drawBackground(c);
        if (!isStarting && !isFinishing) {
            drawText(c);
        }
        drawButtons(c);
    }

    private void drawBackground(Canvas c) {
        background.draw(c);
    }

    private void drawText(Canvas c) {
        int textSize = Math.abs(c.getHeight()/20);
        textPaint.setTextSize(textSize);
        textPaint.setColor(Color.WHITE);

        int x = (c.getWidth() / 2);
        int y = (int)((c.getHeight()/100*(BACKGROUND_PERCENT_DIM_Y /2)) - ((textPaint.descent() + textPaint.ascent()) / 2)) ;

        c.drawText(mContext.getString(R.string.game_over), x, y - textSize, textPaint);
        c.drawText(mContext.getString(R.string.you_got) + score + " "
                + (score == 1 ? mContext.getString(R.string.point) : mContext.getString(R.string.points)) + "!", x, y + textSize, textPaint);

        y = (int)((c.getHeight()/100*(BACKGROUND_PERCENT_DIM_Y + (100- BACKGROUND_PERCENT_DIM_Y)/2)) - ((textPaint.descent() + textPaint.ascent()) / 2));
        textPaint.setColor(Color.BLACK);

        if (maxScore != -1) {
            c.drawText(mContext.getString(R.string.replay), x, y - textSize, textPaint);

            String maxScoreText = score > maxScore ?
                    mContext.getString(R.string.new_high_score) : mContext.getString(R.string.high_score) + maxScore;
            c.drawText(maxScoreText, x, y + textSize, textPaint);
        } else {
            c.drawText(mContext.getString(R.string.replay), x, y, textPaint);
        }
    }

    private void drawButtons(Canvas c) {
        replayButton.draw(c);

        if (!isStarting && !isFinishing) {
            homeButton.draw(c);
        }
    }

    @Override
    public synchronized boolean onInput(GameInput input) {
        super.onInput(input);

        if (isStarting || isFinishing) {
            return false;
        }

        if (homeButton.onInput(input)) {
            if (input.getType() == GameInput.TAP_END) {
                finishStateWithAnimation(true);
            }
            return true;
        }
        if (replayButton.onInput(input)) {
            if (input.getType() == GameInput.TAP_END) {
                finishStateWithAnimation(false);
            }
            return true;
        }

        return false;
    }

    public void onAdClosed() {
        replayButton.setVisible(true);

        startStartingAnimation();
    }

    private synchronized void finishStateWithAnimation(final boolean toStart) {
        if (!isFinishing) {
            isFinishing = true;
            background.setPercentDimY(toStart ? GameStateStart.BACKGROUND_PERCENT_DIM_Y : 0, new UpdateCallback<Float>() {
                @Override
                public void onUpdate(Float object) {
                    replayButton.onResize(surfaceRect, object);
                }

                @Override
                public void onFinish(Float object) {
                    replayButton.onResize(surfaceRect, object);

                    setNextState(toStart ? GameEngine.STATE.START : GameEngine.STATE.GAME);
                }
            });
        }
    }
}
