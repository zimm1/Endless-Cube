package com.simonecavazzoni.firstandroidgame.gameEngine;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.simonecavazzoni.firstandroidgame.BuildConfig;
import com.simonecavazzoni.firstandroidgame.MainActivity;
import com.simonecavazzoni.firstandroidgame.R;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameState.GameState;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameState.GameStateEnd;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameState.GameStatePlaying;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameState.GameStateStart;
import com.simonecavazzoni.firstandroidgame.gameEngine.utils.ColorUtils;


public class GameEngine {

    public enum STATE {
        START, START_ANIMATED, GAME, END
    }

    private Rect surfaceRect;

    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private STATE currentGameState;
    public GameState gameState;

    private String backgroundPrimaryColor;

    private MainActivity mContext;
    private GoogleApiClient mGoogleApiClient;

    private int maxScore = -1;

    public GameEngine(MainActivity context, GoogleApiClient googleApiClient, Rect surfaceRect) {
        this.mContext = context;
        this.mGoogleApiClient = googleApiClient;

        getMaxScore();

        this.surfaceRect = surfaceRect;
        setNextState(STATE.START_ANIMATED);
    }

    public void onResize(Rect surfaceRect) {
        this.surfaceRect = surfaceRect;
        if (gameState != null) {
            gameState.onResize(surfaceRect);
        }
    }


    public void gameLoop(float timeScale, Canvas c) {
        update(timeScale);
        draw(c);
        checkStateFinished();
    }

    private void update(float timeScale) {
        gameState.update(timeScale);
    }

    private void draw(Canvas c) {
        c.drawColor(GameStateStart.BACKGROUND_COLOR);

        gameState.draw(c);
    }

    private void checkStateFinished() {
        STATE nextState = gameState.getNextState();
        if (nextState != null) {
            setNextState(nextState);
        }
    }

    private void setNextState(STATE nextState) {
        boolean animated = false;

        switch (nextState) {
            case START_ANIMATED:
                animated = true;
                backgroundPrimaryColor = ColorUtils.BACKGROUND_COLORS.random();
            case START:
                currentGameState = STATE.START;
                gameState = new GameStateStart(mContext, surfaceRect, backgroundPrimaryColor, animated);

                break;
            case GAME:
                currentGameState = STATE.GAME;
                gameState = new GameStatePlaying(mContext, surfaceRect);

                break;
            case END:
                int score = ((GameStatePlaying) gameState).points;

                backgroundPrimaryColor = ColorUtils.BACKGROUND_COLORS.random();

                currentGameState = STATE.END;
                gameState = new GameStateEnd(mContext, surfaceRect, score, maxScore, backgroundPrimaryColor, mContext.canShowLevelFinishedInterstitial());
                if (maxScore != -1 && score > maxScore) {
                    maxScore = score;
                }

                logScore(score);
                updateAchievements(score);

                mContext.showLevelFinishedInterstitial();

                break;
        }
    }

    private void logScore(int score) {
        Bundle bundle = new Bundle();
        bundle.putLong("SCORE", score);
        bundle.putLong("LEVEL", 1);
        mContext.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.POST_SCORE, bundle);
    }

    private void updateAchievements(int score) {
        if (mContext != null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Games.Leaderboards.submitScore(mGoogleApiClient, BuildConfig.leaderboard_high_score, score);
            Games.Achievements.unlock(mGoogleApiClient, BuildConfig.achievement_hello_world);
            Games.Achievements.increment(mGoogleApiClient, BuildConfig.achievement_bored, 1);
            Games.Achievements.increment(mGoogleApiClient, BuildConfig.achievement_really_bored, 1);

            String achievementUnlocked;

            switch (score) {
                case 1:
                    achievementUnlocked = BuildConfig.achievement_ok;
                    break;
                case 1337:
                    achievementUnlocked = BuildConfig.achievement_mlg_360_no_scope;
                    break;
                default:
                    achievementUnlocked = null;
            }
            if (achievementUnlocked != null) {
                Games.Achievements.unlock(mGoogleApiClient, achievementUnlocked);

                Bundle bundle = new Bundle();
                bundle.putString("ACHIEVEMENT_ID", achievementUnlocked);
                mContext.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.UNLOCK_ACHIEVEMENT, bundle);
            }
        }
    }

    public synchronized boolean onInput(GameInput input) {
        return gameState.onInput(input);
    }

    private void getMaxScore() {
        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(
                mGoogleApiClient,
                BuildConfig.leaderboard_high_score,
                LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
                    .setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
            @Override
            public void onResult(@NonNull final Leaderboards.LoadPlayerScoreResult scoreResult) {
                if (isScoreResultValid(scoreResult)) {
                    // here you can get the score like this
                    maxScore = (int)scoreResult.getScore().getRawScore();
                }
            }
        });
    }

    private boolean isScoreResultValid(final Leaderboards.LoadPlayerScoreResult scoreResult) {
        return scoreResult != null
                && GamesStatusCodes.STATUS_OK == scoreResult.getStatus().getStatusCode()
                && scoreResult.getScore() != null;
    }
}
