package com.simonecavazzoni.firstandroidgame.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.simonecavazzoni.firstandroidgame.R;

public class ForceUpdateChecker {

    private static final String TAG = ForceUpdateChecker.class.getSimpleName();

    private OnUpdateNeededListener onUpdateNeededListener;
    private Context context;

    public interface OnUpdateNeededListener {
        void onUpdateNeeded(String updateUrl);
    }

    public static Builder with(@NonNull Context context) {
        return new Builder(context);
    }

    public ForceUpdateChecker(@NonNull Context context,
                              OnUpdateNeededListener onUpdateNeededListener) {
        this.context = context;
        this.onUpdateNeededListener = onUpdateNeededListener;
    }

    public void check() {
        final FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();

        try {
            String appVersion = getAppVersion(context);

            if (remoteConfig.getBoolean(
                    context.getResources().getString(R.string.force_update_version)+appVersion)
                        && onUpdateNeededListener != null) {

                String updateUrl = remoteConfig.getString(
                        context.getResources().getString(R.string.force_update_store_url));

                onUpdateNeededListener.onUpdateNeeded(updateUrl);
            }

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private String getAppVersion(Context context) throws PackageManager.NameNotFoundException{

        String result = context.getPackageManager()
                .getPackageInfo(context.getPackageName(), 0)
                .versionName;
        result = result.replaceAll("\\.", "");

        return result;
    }

    public static class Builder {

        private Context context;
        private OnUpdateNeededListener onUpdateNeededListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder onUpdateNeeded(OnUpdateNeededListener onUpdateNeededListener) {
            this.onUpdateNeededListener = onUpdateNeededListener;
            return this;
        }

        public ForceUpdateChecker build() {
            return new ForceUpdateChecker(context, onUpdateNeededListener);
        }

        public ForceUpdateChecker check() {
            ForceUpdateChecker forceUpdateChecker = build();
            forceUpdateChecker.check();

            return forceUpdateChecker;
        }
    }
}