package com.simonecavazzoni.firstandroidgame.gameEngine.gameProp;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.simonecavazzoni.firstandroidgame.gameEngine.GameInput;
import com.simonecavazzoni.firstandroidgame.gameEngine.utils.TupleFloat;

public class GamePropButton extends GameProp {

    private static final String BASE_COLOR = "FFFFFF";
    private static final String HOVER_COLOR = "EEEEEE";

    public enum SIZE {
      SMALL((float)3), MEDIUM((float)4), BIG((float)6);

      private float dimension;

      SIZE(float dimension) {
          this.dimension = dimension;
      }

      public float getDimension() {
          return dimension;
      }
    }

    public enum TYPE {
        PLAY, LEADERBOARD, ACHIEVEMENTS, CONTINUE_COUNTDOWN, CONTINUE_EXIT, HOME, REPLAY
    }

    private TYPE type;
    private SIZE size;
    private Drawable mDrawable;
    private Rect drawableRect;
    private String mText;
    private Paint textPaint;


    private GamePropButton(Rect surfaceRect, TupleFloat position, SIZE size, TYPE type, Drawable drawable, String text) {
        super(surfaceRect);

        this.position = position;
        this.size = size;
        this.type = type;
        this.mDrawable = drawable;
        this.mText = text;

        setBaseColor(BASE_COLOR);
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);

        onResize(surfaceRect, position.getY());
    }

    @SuppressWarnings("WeakerAccess")
    public GamePropButton(Rect surfaceRect, TupleFloat position, SIZE size, TYPE type) {
        this(surfaceRect, position, size, type, null, null);
    }

    public GamePropButton(Rect surfaceRect, TupleFloat position, SIZE size, TYPE type, Drawable drawable) {
        this(surfaceRect, position, size, type, drawable, null);
    }

    @SuppressWarnings("unused")
    public GamePropButton(Rect surfaceRect, TupleFloat position, SIZE size, TYPE type, String text) {
        this(surfaceRect, position, size, type, null, text);
    }

    @Override
    public void onResize(Rect surfaceRect) {
        super.onResize(surfaceRect);

        this.dimension = new TupleFloat(surfaceRect.width()/40*size.getDimension(), surfaceRect.width()/40*size.getDimension());
        this.drawableRect = new Rect(
                (int) (position.getX() - dimension.getX() + dimension.getX() / 10 * 4),
                (int) (position.getY() - dimension.getY() + dimension.getY() / 10 * 4),
                (int) (position.getX() + dimension.getX() - dimension.getX() / 10 * 4),
                (int) (position.getY() + dimension.getY() - dimension.getY() / 10 * 4)
        );
        this.textPaint.setTextSize((dimension.getX() - dimension.getX() / 10 * 4) * 2);
    }

    public void onResize(Rect surfaceRect, float posY) {
        this.position.setY(posY);

        this.onResize(surfaceRect);
    }

    public void onResize(Rect surfaceRect, TupleFloat pos) {
        this.position.setTuple(pos);

        this.onResize(surfaceRect);
    }

    @Override
    public void draw(Canvas c) {
        if (!isVisible()) {
            return;
        }

        setBaseColor(hover ? HOVER_COLOR : BASE_COLOR);

        c.drawCircle(position.getX(), position.getY(), dimension.getX(), paint);

        if (mDrawable != null) {
            mDrawable.setBounds(drawableRect);
            mDrawable.draw(c);
        }

        if (mText != null) {
            c.drawText(mText, position.getX(), position.getY() - ((textPaint.descent() + textPaint.ascent()) / 2), textPaint);
        }
    }

    @Override
    public boolean onInput(GameInput input) {
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

    @Override
    protected boolean collidesWithPoint(TupleFloat point) {
        if (dimension.getX() == 0){
            return false;
        }

        float dx = position.getX() - point.getX();
        float dy = position.getY() - point.getY();

        return dx * dx + dy * dy <= Math.pow(dimension.getX(), 2);
    }

    public TYPE getType() {
        return this.type;
    }

    public void setText(String text) {
        this.mText = text;
    }
}
