package com.simonecavazzoni.firstandroidgame.gameEngine.gameProp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import com.simonecavazzoni.firstandroidgame.R;
import com.simonecavazzoni.firstandroidgame.gameEngine.GameInput;
import com.simonecavazzoni.firstandroidgame.gameEngine.utils.TupleFloat;
import java.util.Timer;
import java.util.TimerTask;


public class GamePropContinueMessage extends GameProp {

    private static final String BASE_COLOR = "000000";
    private static final String HOVER_COLOR = "757575";

    private GamePropButton countdownButton;
    private GamePropButton exitButton;
    private Paint textPaint;
    private int countdown;
    private boolean isFinished;
    private boolean isContinue;

    public GamePropContinueMessage(Context context, Rect surfaceRect) {
        super(surfaceRect);

        countdownButton = new GamePropButton(
                surfaceRect,
                new TupleFloat((float)surfaceRect.width()/2, position.getY()+dimension.getY()),
                GamePropButton.SIZE.SMALL, GamePropButton.TYPE.CONTINUE_COUNTDOWN);
        exitButton = new GamePropButton(
                surfaceRect,
                new TupleFloat((float)position.getX()+dimension.getX(), position.getY()),
                GamePropButton.SIZE.SMALL, GamePropButton.TYPE.CONTINUE_EXIT,
                context.getResources().getDrawable(R.drawable.ic_close_black));

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(dimension.getY()/3);
        textPaint.setTextAlign(Paint.Align.CENTER);

        onResize(surfaceRect);
    }

    @Override
    public void onResize(Rect surfaceRect) {
        super.onResize(surfaceRect);

        this.dimension.set((float)surfaceRect.width()/4*3, (float)surfaceRect.width()/8*3);
        this.position.set(
                (float)surfaceRect.width()/2-dimension.getX()/2,
                (float)surfaceRect.height()/5*2-dimension.getY()/2
        );
        countdownButton.onResize(surfaceRect, new TupleFloat((float)surfaceRect.width()/2, position.getY()+dimension.getY()));
        exitButton.onResize(surfaceRect, new TupleFloat((float)position.getX()+dimension.getX(), position.getY()));

        textPaint.setTextSize(dimension.getY()/3);
    }

    @Override
    public void update(float timeScale) {
        super.update(timeScale);

        countdownButton.update(timeScale);
        exitButton.update(timeScale);
    }

    @Override
    public void draw(Canvas c) {
        setBaseColor(hover ? HOVER_COLOR : BASE_COLOR);

        super.draw(c);

        if (!isVisible()) {
            return;
        }

        countdownButton.draw(c);
        exitButton.draw(c);

        c.drawText("Continue?", c.getWidth()/2, position.getY()+10 + Math.abs(textPaint.ascent()-textPaint.descent()), textPaint);
    }

    @Override
    public boolean onInput(GameInput input) {
        if (isFinished || isContinue) {
            return false;
        }

        if (input.getType() == GameInput.TAP_END) {
            if (exitButton.onInput(input)) {
                isFinished = true;
                return true;
            }
            if (countdownButton.onInput(input) || onSelfInput(input)) {
                isContinue = true;
                return true;
            }

            return false;
        }

        return exitButton.onInput(input) || countdownButton.onInput(input) || onSelfInput(input);
    }

    private boolean onSelfInput(GameInput input) {
        switch (input.getType()) {
            case GameInput.TAP_START:
                if (collidesWithInput(input)) {
                    inputStarted = true;
                    hover = true;
                    return true;
                }
                break;
            case GameInput.TAP_UPDATE:
                if (inputStarted) {
                    hover = collidesWithInput(input);
                    return true;
                }
                break;
            case GameInput.TAP_END:
                if (inputStarted) {
                    hover = false;
                    inputStarted = false;
                    return collidesWithInput(input);
                }
                break;
        }

        return false;
    }

    private boolean collidesWithInput(GameInput input) {
        return collidesWithPoint(
                new TupleFloat(input.getEvent().getX(),
                        input.getEvent().getY()));
    }

    public void startCountdown(@SuppressWarnings("SameParameterValue") int n) {
        this.countdown = n+1;

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                countdown--;

                if (countdown >= 0) {
                    countdownButton.setText("" + countdown);
                } else {
                    isFinished = true;
                    timer.cancel();
                    timer.purge();

                }
            }
        }, 0, 1000);
    }

    public boolean isFinished() {
        return this.isFinished;
    }

    public boolean isContinue() {
        return this.isContinue;
    }
}
