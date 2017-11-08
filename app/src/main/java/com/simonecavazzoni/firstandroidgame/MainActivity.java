package com.simonecavazzoni.firstandroidgame;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.simonecavazzoni.firstandroidgame.gameEngine.GameEngine;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameState.GameStateEnd;
import com.simonecavazzoni.firstandroidgame.gameEngine.gameState.GameStatePlaying;
import com.simonecavazzoni.firstandroidgame.utils.AdMobCallback;
import com.simonecavazzoni.firstandroidgame.utils.ForceUpdateChecker;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        AdMobCallback,
        RewardedVideoAdListener,
        ForceUpdateChecker.OnUpdateNeededListener {

    private static final int UI_ANIMATION_DELAY = 600;
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_RESOLUTION = 9002;
    private static final int FIREBASE_REMOTE_CONFIG_CACHE_EXPIRATION = 60;

    private MySurfaceView mContentView;

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            if (!mContentView.isGameLoopGoing) {
                mHideHandler.removeCallbacks(startRunnable);
                mHideHandler.postDelayed(startRunnable, UI_ANIMATION_DELAY);
            }
        }
    };
    private final Runnable startRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mContentView.isGameLoopGoing) {
                mContentView.startGameLoop();
            }
        }
    };

    private GoogleApiClient mGoogleApiClient;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;
    boolean mExplicitSignOut = false;
    boolean mInSignInFlow = false;
    private FirebaseAuth mAuth;

    public FirebaseAnalytics firebaseAnalytics;
    private InterstitialAd mLevelFinishedInterstitialAd;
    private AdView bannerView;
    private RewardedVideoAd mContinueGameVideoAd;
    private int levelFinishedAdCounter = 0;

    private FirebaseRemoteConfig firebaseRemoteConfig;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initializeContentView();
        initializeFirebase();
        initializeAdMob();
        initializeGameEngine();
    }

    private void initializeContentView() {
        setContentView(R.layout.activity_main);
        mContentView = findViewById(R.id.fullscreen_content);
    }

    private void initializeGameEngine() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mContentView.gameEngine = new GameEngine(
                this,
                mGoogleApiClient,
                new Rect(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        );
    }

    private void initializeFirebase() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuth = FirebaseAuth.getInstance();

        //noinspection SpellCheckingInspection
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_SIGN_IN_REQUEST_ID_TOKEN)
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        firebaseRemoteConfig.fetch(FIREBASE_REMOTE_CONFIG_CACHE_EXPIRATION)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            firebaseRemoteConfig.activateFetched();
                            ForceUpdateChecker
                                    .with(MainActivity.this)
                                    .onUpdateNeeded(MainActivity.this)
                                    .check();
                        }
                    }
                });

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
    }

    private void initializeAdMob() {
        MobileAds.initialize(this, BuildConfig.admob_app_id);
        mLevelFinishedInterstitialAd = new InterstitialAd(this);
        mLevelFinishedInterstitialAd.setAdUnitId(BuildConfig.admob_interstitial_level_finished);
        mLevelFinishedInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mLevelFinishedInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice(BuildConfig.AD_MOB_TEST_DEVICE).build());
                onInterstitialAdClosed();
            }

        });

        mLevelFinishedInterstitialAd.loadAd(new AdRequest.Builder().addTestDevice(BuildConfig.AD_MOB_TEST_DEVICE).build());

        bannerView = findViewById(R.id.bannerView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(BuildConfig.AD_MOB_TEST_DEVICE).build();
        bannerView.loadAd(adRequest);

        mContinueGameVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mContinueGameVideoAd.setImmersiveMode(true);
        mContinueGameVideoAd.setRewardedVideoAdListener(this);
        mContinueGameVideoAd.loadAd(
                BuildConfig.DEBUG ?
                        BuildConfig.admob_video_continue_game_test :
                        BuildConfig.admob_video_continue_game,
                new AdRequest.Builder().build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mInSignInFlow && !mExplicitSignOut) {
            // auto sign in
            mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }
    }


    @Override
    protected void onResume() {
        mContinueGameVideoAd.resume(this);

        super.onResume();

        mContentView.getHolder().addCallback(mContentView);

        delayedHide();

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, null);
    }

    @Override
    protected void onPause() {
        mContinueGameVideoAd.pause(this);

        super.onPause();

        mContentView.stopGameLoop();

        mContentView.getHolder().removeCallback(mContentView);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        mContinueGameVideoAd.destroy(this);

        super.onDestroy();
    }

    private void delayedHide() {
            mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, UI_ANIMATION_DELAY);
        hideActionBar();
    }

    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @Override
    public void onBackPressed() {
        //Do nothing
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;

            silentSignIn();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RC_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                unableToSignIn();
            }
        }
    }

    private void silentSignIn() {
        OptionalPendingResult<GoogleSignInResult> pendingResult =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult.isDone()) {
            // There's immediate result available.
            updateButtonsAndStatusFromSignInResult(pendingResult.get(), true);
        } else {
            // There's no immediate result ready, displays some progress indicator and waits for the
            // async callback.
            pendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult result) {
                    updateButtonsAndStatusFromSignInResult(result, true);
                }
            });
        }
    }

    private void normalSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            updateButtonsAndStatusFromSignInResult(result, false);
        }

        if (requestCode == RC_RESOLUTION) {
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
            } else {
                unableToSignIn();
            }
        }
    }

    private void updateButtonsAndStatusFromSignInResult(GoogleSignInResult signInResult, boolean silentSignIn) {
        if (signInResult.isSuccess()) {
            delayedHide();
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = signInResult.getSignInAccount();
            firebaseAuthWithGoogle(account);

            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null);
        } else {
            if (silentSignIn) {
                normalSignIn();
            } else {
                unableToSignIn();
            }
        }
    }

    private void unableToSignIn() {
        delayedHide();
        Toast.makeText(MainActivity.this, getResources().getString(R.string.sign_in_failure),
                Toast.LENGTH_SHORT).show();
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            if(user!=null){
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(acct.getDisplayName())
                                        .setPhotoUri(acct.getPhotoUrl())
                                        .build();
                                user.updateProfile(profileUpdates);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.sign_in_failure),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public boolean canShowLevelFinishedInterstitial() {
        return (firebaseRemoteConfig.getBoolean(
                getResources().getString(R.string.show_level_finished_interstitial_ad))
                && ++levelFinishedAdCounter >= firebaseRemoteConfig.getLong(
                getResources().getString(R.string.n_games_before_show_level_finished_interstitial_ad)));
    }

    @Override
    public void showLevelFinishedInterstitial() {
        if (canShowLevelFinishedInterstitial()) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (mLevelFinishedInterstitialAd.isLoaded()) {
                        mLevelFinishedInterstitialAd.show();
                        levelFinishedAdCounter = 0;
                    } else {
                        onInterstitialAdClosed();
                    }

                    bannerView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void onInterstitialAdClosed() {
        if (mContentView.gameEngine != null
                && mContentView.gameEngine.gameState != null
                && mContentView.gameEngine.gameState instanceof GameStateEnd) {
            ((GameStateEnd)mContentView.gameEngine.gameState).onAdClosed();
        }
    }

    public void showContinueGameVideoAd() {
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (firebaseRemoteConfig.getBoolean(
                        getResources().getString(R.string.show_continue_game_video_ad))
                        && mContinueGameVideoAd.isLoaded()) {
                    mContinueGameVideoAd.show();
                } else {
                    onRewarded(null);
                }
            }
        });
    }

    public void showLeaderboard() {
        if (mGoogleApiClient.hasConnectedApi(Games.API)) {
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                    BuildConfig.leaderboard_high_score), 1234);
        }
    }

    public void showAchievements() {
        if (mGoogleApiClient.hasConnectedApi(Games.API)) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                    1235);
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        try {
            ((GameStatePlaying) mContentView.gameEngine.gameState).videoAdFinished(true);
        } catch (Exception e) {
            //Do nothing
        }
        mContinueGameVideoAd.loadAd(
                BuildConfig.DEBUG ?
                        BuildConfig.admob_video_continue_game_test :
                        BuildConfig.admob_video_continue_game,
                new AdRequest.Builder().build());
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        try {
            ((GameStatePlaying) mContentView.gameEngine.gameState).videoAdFinished();
        } catch (Exception e) {
            //Do nothing
        }
        mContinueGameVideoAd.loadAd(
                BuildConfig.DEBUG ?
                        BuildConfig.admob_video_continue_game_test :
                        BuildConfig.admob_video_continue_game,
                new AdRequest.Builder().build());
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }

    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.new_version_available)
                .setMessage(R.string.please_update_message)
                .setPositiveButton(R.string.update,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton(R.string.no_thanks,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).create();
        dialog.show();
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
