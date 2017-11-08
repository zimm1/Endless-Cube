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
import com.simonecavazzoni.firstandroidgame.gameEngine.gameProp.GamePropContinueMessage;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameProp.GamePropObstacle;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameProp.GamePropPlayer;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameProp.GamePropPointsLine;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameProp.GamePropStartArrow;
import com.simonecavazzoni.firstandroidgame.gameEngine.utils.TupleFloat;
import com.simonecavazzoni.firstandroidgame.gameEngine.utils.Utils;
import com.simonecavazzoni.firstandroidgame.utils.UpdateCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameStatePlaying extends GameState {

    public static int COLUMN_NUMBER = 3;
    public static float OBSTACLES_BASE_SPEED = 10;

    private GamePropPlayer player;
    private ArrayList<GamePropObstacle> obstacles;
    private ArrayList<GamePropPointsLine> pointsLines;
    private GamePropContinueMessage continueMessage;
    private ArrayList<GamePropStartArrow> startArrows;

    private TupleFloat startInputPosition;
    private TupleFloat startPlayerPosition;
    private TupleFloat playerInput;

    private boolean isGameStarted = false;
    private boolean isGameGoing = false;
    private boolean isInputStarted = false;

    private float obstaclesSpeed = OBSTACLES_BASE_SPEED;
    public int points = 0;

    private List<Integer> freeColumns = new ArrayList<>();
    private List<Integer> obstaclePositions = new ArrayList<>();
    private List<Integer> obstaclePositionsForced = new ArrayList<>();
    private int spawnOffset = 0;

    private Paint scorePaint;
    private boolean playerDiedOnce = false;
    private boolean showContinueMessage = false;
    private float collisionPosY;

    public GameStatePlaying(Context context, Rect surfaceRect) {
        super(context, surfaceRect);

        scorePaint = new Paint();
        scorePaint.setColor(Color.GRAY);
        scorePaint.setStyle(Paint.Style.FILL);
        scorePaint.setTextSize(100);
        scorePaint.setTextAlign(Paint.Align.RIGHT);

        obstacles = new ArrayList<>();
        pointsLines = new ArrayList<>();
        player = new GamePropPlayer(surfaceRect);
        continueMessage = new GamePropContinueMessage(context, surfaceRect);
        startArrows = new ArrayList<>();

        initializeFreeColumns();
        initializeStartArrows();

        spawnObstacles(0, true);
    }

    @Override
    public void onResize(Rect surfaceRect) {
        super.onResize(surfaceRect);

        continueMessage.onResize(surfaceRect);
    }

    private void initializeFreeColumns() {
        for (int i = 0; i < COLUMN_NUMBER; ++i) {
            freeColumns.add(1);
        }
    }

    private void initializeStartArrows() {
        startArrows.add(new GamePropStartArrow(surfaceRect,
                mContext.getResources().getDrawable(R.drawable.ic_arrow_forward_black), true));
        startArrows.add(new GamePropStartArrow(surfaceRect,
                mContext.getResources().getDrawable(R.drawable.ic_arrow_back_black), false));
    }

    private boolean noFreeColumns() {
        for (int n : freeColumns) {
            if (n > 0) {
                return false;
            }
        }
        return true;
    }

    private void removeFreeColumn(int i) {
        freeColumns.set(i, 0);
    }
    private void addFreeColumn(int i) {
        freeColumns.set(i, freeColumns.get(i) + 1);
    }

    private void fillFreeColumns() {
        for (int i = 0; i < COLUMN_NUMBER; ++i) {
            addFreeColumn(i);
        }
    }

    private void spawnObstacles(float spawnH) {
        spawnObstacles(spawnH, false);
    }
    private void spawnObstacles(float spawnH, boolean forceSpawn) {
        spawnH -= surfaceRect.width()/COLUMN_NUMBER*spawnOffset;

        //Spawn pointsLine
        if (!forceSpawn) {
            pointsLines.add(new GamePropPointsLine(surfaceRect, spawnH + surfaceRect.width() / COLUMN_NUMBER));
        }

        int nObstacles = new Random().nextInt(COLUMN_NUMBER)+1;

        boolean added = false;

        obstaclePositions = Utils.extractNumbers(nObstacles);

        obstaclePositionsForced.clear();
        for (int i = 0; i < COLUMN_NUMBER; ++i) {
            if (freeColumns.get(i) > 4) {
                obstaclePositions.removeAll(Collections.singletonList(i));
                if (spawnObstacle(i, spawnH)) {
                    added = true;
                    obstaclePositionsForced.add(i);
                }
            }
        }

        for (int i = 0; i < COLUMN_NUMBER; i++) {
            if (obstaclePositions.contains(i)) {
                if (spawnObstacle(i, spawnH)) {
                    added = true;
                } else {
                    obstaclePositions.removeAll(Collections.singletonList(i));
                }
            }
        }

        obstaclePositions.addAll(obstaclePositionsForced);

        checkFreeColumn(1, spawnH);
        checkFreeColumn(0, spawnH);
        checkFreeColumn(2, spawnH);

        if (!added) {
            fillFreeColumns();
            if (forceSpawn) {
                spawnObstacles(spawnH);
            } else {
                spawnOffset++;
            }
        } else {
            spawnOffset = 0;
            if (forceSpawn) {
                pointsLines.add(new GamePropPointsLine(surfaceRect, spawnH + surfaceRect.width() / COLUMN_NUMBER));
            }
        }
    }

    private void checkFreeColumn(int i, float spawnH) {
        if (!obstaclePositions.contains(i)) {
            boolean playerCanGoHere = false;

            for (int c = 0; c < COLUMN_NUMBER; ++c) {
                if (freeColumns.get(c) > 0 && (c + 1 == i || c - 1 == i)) {
                    addFreeColumn(i);

                    playerCanGoHere = true;
                    break;
                }
            }

            if (!playerCanGoHere) {
                spawnObstacle(i, spawnH);
            }
        }
    }

    private boolean spawnObstacle(int i, float spawnH) {
        int w = surfaceRect.width();
        float spawnW = w / COLUMN_NUMBER;

        int freeColumnN = freeColumns.get(i);
        removeFreeColumn(i);
        if (!noFreeColumns()) {
            obstacles.add(new GamePropObstacle(surfaceRect, new TupleFloat(spawnW * i, spawnH)));
            return true;
        }
        freeColumns.set(i, freeColumnN+1);
        return false;
    }

    private void startGame() {
        isGameStarted = true;
        isGameGoing = true;
    }

    private void pauseGame() {
        isGameGoing = false;
    }

    @Override
    public void update(float timeScale) {
        super.update(timeScale);

        if (showContinueMessage) {
            continueMessage.update(timeScale);
            if (continueMessage.isFinished()) {
                setNextState(GameEngine.STATE.END);
            }
            if (continueMessage.isContinue()) {
                continueGame();
            }
        }

        player.updateBlink();

        if (!isGameGoing) {
            return;
        }

        for (GamePropPointsLine pointsLine: pointsLines) {
            pointsLine.update(timeScale, obstaclesSpeed);
        }

        Iterator<GamePropObstacle> i = obstacles.iterator();
        while (i.hasNext()) {
            GamePropObstacle obstacle = i.next();

            obstacle.update(timeScale, obstaclesSpeed);
            if (obstacle.isOutOfSurface(surfaceRect)) {
                i.remove();
            }
        }

        if (obstacles.size() == 0) {
            spawnObstacles( -surfaceRect.width() / COLUMN_NUMBER);
        } else {
            float lastObstaclePos = obstacles.get(obstacles.size()-1).position.getY();
            if (lastObstaclePos > -surfaceRect.width()/COLUMN_NUMBER) {
                spawnObstacles(lastObstaclePos - surfaceRect.width() / COLUMN_NUMBER);
            }
        }

        player.update(timeScale, playerInput);
        collisionPosY = player.collidesWithObstaclesAtPosY(obstacles);
        if (collisionPosY != -1) {
            playerDeath();
        }

        if (!pointsLines.isEmpty()) {
            GamePropPointsLine firstPointsLine = pointsLines.get(0);
            if (player.position.getY() <= firstPointsLine.position.getY() + firstPointsLine.dimension.getY()) {
                pointsLines.remove(0);
                points++;
                if (points % 10 == 0) {
                    obstaclesSpeed += 2;
                }
            }
        }
    }

    @Override
    public void draw(Canvas c) {
        super.draw(c);

        try {
            for (GamePropObstacle obstacle : obstacles) {
                obstacle.draw(c);
            }

            for (GamePropPointsLine pointsLine : pointsLines) {
                pointsLine.draw(c);
            }
        } catch (ConcurrentModificationException e) {
            //DO nothing
        }

        player.draw(c);

        drawGameScore(c);

        if (showContinueMessage) {
            continueMessage.draw(c);
        }

        if (!isGameStarted && !showContinueMessage &&!isGameGoing) {
            drawStartArrows(c);
        }
    }

    private void drawGameScore(Canvas c) {
        c.drawText(""+points, c.getWidth()-10, 110, scorePaint);
    }

    private void drawStartArrows(Canvas c) {
        for (GamePropStartArrow a : startArrows) {
            a.draw(c);
        }
    }

    @Override
    public synchronized boolean onInput(GameInput input) {
        super.onInput(input);

        TupleFloat position = new TupleFloat(input.getEvent().getX(), input.getEvent().getY());

        if (showContinueMessage) {
            return continueMessage.onInput(input);
        }

        switch (input.getType()) {
            case GameInput.TAP_START:
                if (!isGameStarted) {
                    startGame();
                }
                if (isGameGoing) {
                    if (!isInputStarted) {
                        startInputPosition = new TupleFloat(position);
                        startPlayerPosition = new TupleFloat(player.position);
                        isInputStarted = true;
                        return true;
                    }
                }
                break;
            case GameInput.TAP_UPDATE:
                if (isInputStarted) {
                    playerInput = new TupleFloat(
                            startPlayerPosition.getX() + (position.getX() - startInputPosition.getX()),
                            startPlayerPosition.getY() + (position.getY() - startInputPosition.getY())
                    );
                    return true;
                }
                break;
            case GameInput.TAP_END:
                if (isInputStarted) {
                    playerInput = null;
                    isInputStarted = false;
                    return true;
                }
                break;
        }

        return false;
    }

    private void playerDeath() {
        pauseGame();

        if (!playerDiedOnce) {
            playerDiedOnce = true;
            blinkPlayer(true);
        } else {
            blinkPlayer(false);
        }
    }

    private void blinkPlayer(final boolean isContinue) {
        player.blink(3, new UpdateCallback<Void>() {
            @Override
            public void onUpdate(Void object) {

            }

            @Override
            public void onFinish(Void object) {
                if (isContinue) {
                    continueMessage.startCountdown(5);
                    showContinueMessage = true;
                } else {
                    setNextState(GameEngine.STATE.END);
                }
            }
        });
    }

    private synchronized void continueGame() {
        if (showContinueMessage) {
            showVideoAd();
        }
        showContinueMessage = false;
    }

    private void showVideoAd() {
        ((MainActivity)mContext).showContinueGameVideoAd();
    }

    public void videoAdFinished() {
        videoAdFinished(false);
    }

    public void videoAdFinished(boolean skipped) {
        if (!skipped) {
            resetGameToContinue();
            isGameStarted = false;
        } else {
            setNextState(GameEngine.STATE.END);
        }
    }

    private void resetGameToContinue() {
        Iterator<GamePropObstacle> obstacleIterator = obstacles.iterator();
        while(obstacleIterator.hasNext()) {
            GamePropObstacle o = obstacleIterator.next();
            if (o.position.getY() > collisionPosY) {
                obstacleIterator.remove();
            } else {
                o.position.setY(o.position.getY() - collisionPosY);
            }
        }

        for (GamePropPointsLine p : pointsLines) {
            p.position.setY(p.position.getY() - collisionPosY);
        }

        player.resetState();
        player.blink(3);
    }
}
