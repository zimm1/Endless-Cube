package com.simonecavazzoni.firstandroidgame.gameEngine.gameState;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import com.simonecavazzoni.firstandroidgame.MainActivity;
import com.simonecavazzoni.firstandroidgame.R;
import com.simonecavazzoni.firstandroidgame.gameEngine.GameEngine;
import com.simonecavazzoni.firstandroidgame.gameEngine.GameInput;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameProp.GamePropBackground;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameProp.GamePropButton;
import com.simonecavazzoni.firstandroidgame.gameEngine.utils.TupleFloat;
import com.simonecavazzoni.firstandroidgame.utils.UpdateCallback;

import java.util.ArrayList;


public class GameStateStart extends GameState {

    public static final int BACKGROUND_COLOR = Color.parseColor("#E0E0E0");

    static final int BACKGROUND_PERCENT_DIM_Y = 80;

    private GamePropBackground background;
    private ArrayList<GamePropButton> buttons = new ArrayList<>();

    private boolean isStarting;
    private boolean isFinishing = false;

    @SuppressWarnings("unused")
    public GameStateStart(Context context, Rect surfaceRect, String backgroundPrimaryColor) {
        this(context, surfaceRect, backgroundPrimaryColor,false);
    }

    public GameStateStart(Context context, Rect surfaceRect, String backgroundPrimaryColor, boolean animated) {
        super(context, surfaceRect);

        isStarting = animated;

        int backgroundPercentDimY = animated ? 0 : BACKGROUND_PERCENT_DIM_Y;

        background = new GamePropBackground(surfaceRect, backgroundPercentDimY, backgroundPrimaryColor);

        buttons.add(new GamePropButton(
                surfaceRect,
                new TupleFloat((float)surfaceRect.width()/40*7, background.dimension.getY()),
                GamePropButton.SIZE.SMALL, GamePropButton.TYPE.ACHIEVEMENTS,
                context.getResources().getDrawable(R.drawable.ic_playlist_add_check_black)));
        buttons.add(new GamePropButton(
                surfaceRect,
                new TupleFloat((float)surfaceRect.width()/2, background.dimension.getY()),
                GamePropButton.SIZE.BIG, GamePropButton.TYPE.PLAY,
                context.getResources().getDrawable(R.drawable.ic_play_arrow_black)));
        buttons.add(new GamePropButton(
                surfaceRect,
                new TupleFloat((float)surfaceRect.width()-surfaceRect.width()/40*7, background.dimension.getY()),
                GamePropButton.SIZE.SMALL, GamePropButton.TYPE.LEADERBOARD,
                context.getResources().getDrawable(R.drawable.ic_format_list_numbered_black)));

        onResize(surfaceRect);

        if (animated) {
            startStartingAnimation();
        }
    }

    private void startStartingAnimation() {
        background.setPercentDimY(BACKGROUND_PERCENT_DIM_Y, new UpdateCallback<Float>() {
            @Override
            public void onUpdate(Float object) {
                for (GamePropButton b : buttons) {
                    b.onResize(surfaceRect, object);
                }
            }

            @Override
            public void onFinish(Float object) {
                for (GamePropButton b : buttons) {
                    b.onResize(surfaceRect, object);
                }
                isStarting = false;
            }
        });
    }

    @Override
    public void onResize(Rect surfaceRect) {
        super.onResize(surfaceRect);

        background.onResize(surfaceRect);

        for (GamePropButton b : buttons) {
            b.onResize(surfaceRect, background.dimension.getY());
        }
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
        drawButtons(c);
    }

    private void drawBackground(Canvas c) {
        Paint paint = new Paint();

        paint.setColor(BACKGROUND_COLOR);
        c.drawRect(0, 0, c.getWidth(), c.getHeight(), paint);

        background.draw(c);

        if (!isFinishing && !isStarting) {
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(100);
            paint.setTextAlign(Paint.Align.CENTER);

            int x = (surfaceRect.width() / 2);
            int y = (int) ((surfaceRect.height() / 10 * 4) - ((paint.descent() + paint.ascent()) / 2));

            c.drawText("Endless Cube", x, y, paint);
        }
    }

    private void drawButtons(Canvas c) {
        for (GamePropButton b : buttons) {
            b.draw(c);
        }
    }

    @Override
    public synchronized boolean onInput(GameInput input) {
        super.onInput(input);

        if (isFinishing || isStarting) {
            return false;
        }


        for (GamePropButton b: buttons) {
            if (b.onInput(input)) {
                if (input.getType() == GameInput.TAP_END) {
                    buttonPressed(b);
                }
                return true;
            }
        }

        return false;
    }

    private void buttonPressed(GamePropButton b) {
        switch (b.getType()) {
            case PLAY:
                finishStateWithAnimation();
                break;
            case LEADERBOARD:
                ((MainActivity)mContext).showLeaderboard();
                break;
            case ACHIEVEMENTS:
                ((MainActivity)mContext).showAchievements();
                break;
        }
    }

    private synchronized void finishStateWithAnimation() {
        if (!isFinishing) {
            isFinishing = true;
            background.setPercentDimY(0, new UpdateCallback<Float>() {
                @Override
                public void onUpdate(Float object) {
                    for (GamePropButton b : buttons) {
                        b.onResize(surfaceRect, object);
                    }
                }

                @Override
                public void onFinish(Float object) {
                    for (GamePropButton b : buttons) {
                        b.onResize(surfaceRect, object);
                    }
                    setNextState(GameEngine.STATE.GAME);
                }
            });
        }
    }
}
