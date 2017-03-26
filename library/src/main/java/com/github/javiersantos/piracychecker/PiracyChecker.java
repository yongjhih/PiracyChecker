package com.github.javiersantos.piracychecker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;


import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.github.javiersantos.piracychecker.activities.LicenseActivity;
import com.github.javiersantos.piracychecker.enums.Display;
import com.github.javiersantos.piracychecker.enums.InstallerID;
import com.github.javiersantos.piracychecker.enums.PiracyCheckerCallback;
import com.github.javiersantos.piracychecker.enums.PiracyCheckerError;
import com.github.javiersantos.piracychecker.enums.PirateApp;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("HardwareIds")
public class PiracyChecker {

    protected static final String LIBRARY_PREFERENCES_NAME = "license_check";

    // Library configuration/customizations
    protected Context context;
    protected String unlicensedDialogTitle;
    protected String unlicensedDialogDescription;
    protected Display display;
    protected int colorPrimary;
    protected int colorPrimaryDark;
    protected boolean enableLVL;
    protected boolean enableSigningCertificate;
    protected boolean enableInstallerId;
    protected boolean enableUnauthorizedAppsCheck;
    protected boolean enableStoresCheck;
    protected boolean enableEmulatorCheck;
    protected boolean enableDebugCheck;
    protected boolean saveToSharedPreferences;
    protected boolean blockUnauthorized;
    protected SharedPreferences preferences;
    protected String preferenceSaveResult;
    protected String preferenceBlockUnauthorized;
    protected String licenseBase64;
    protected String signature;
    protected List<InstallerID> installerIDs;
    protected PiracyCheckerCallback callback;

    // LVL
    protected LicenseChecker libraryLVLChecker;


    public PiracyChecker(Context context) {
        this(context, context.getString(R.string.app_unlicensed), context.getString(R.string.app_unlicensed_description));
    }

    public PiracyChecker(Context context, String title, String description) {
        this.context = context;
        this.unlicensedDialogTitle = title;
        this.unlicensedDialogDescription = description;
        this.display = Display.DIALOG;
        this.installerIDs = new ArrayList<>();
        this.colorPrimary = ContextCompat.getColor(context, R.color.colorPrimary);
        this.colorPrimaryDark = ContextCompat.getColor(context, R.color.colorPrimaryDark);
    }

    public PiracyChecker(Context context, @StringRes int title, @StringRes int description) {
        this(context, context.getString(title), context.getString(description));
    }

    public PiracyChecker enableGooglePlayLicensing(String licenseKeyBase64) {
        this.enableLVL = true;
        this.licenseBase64 = licenseKeyBase64;
        return this;
    }

    public PiracyChecker enableSigningCertificate(String signature) {
        this.enableSigningCertificate = true;
        this.signature = signature;
        return this;
    }

    public PiracyChecker enableInstallerId(InstallerID installerID) {
        this.enableInstallerId = true;
        this.installerIDs.add(installerID);
        return this;
    }

    public PiracyChecker enableUnauthorizedAppsCheck() {
        this.enableUnauthorizedAppsCheck = true;
        return this;
    }

    @Deprecated
    public PiracyChecker blockIfUnauthorizedAppDetected(SharedPreferences preferences,
                                                           @NonNull String preferenceName) {
        this.blockIfUnauthorizedAppUninstalled(preferences, preferenceName);
        return this;
    }

    public PiracyChecker blockIfUnauthorizedAppUninstalled(SharedPreferences preferences,
                                                           @NonNull String preferenceName) {
        this.blockUnauthorized = true;
        this.preferenceBlockUnauthorized = preferenceName;
        saveToSharedPreferences(preferences);
        return this;
    }

    @Deprecated
    public PiracyChecker blockIfUnauthorizedAppDetected(String preferencesName,
                                                           @NonNull String preferenceName) {
        this.blockIfUnauthorizedAppUninstalled(preferences, preferenceName);
        return this;
    }

    public PiracyChecker blockIfUnauthorizedAppUninstalled(String preferencesName,
                                                           @NonNull String preferenceName) {
        this.blockUnauthorized = true;
        this.preferenceBlockUnauthorized = preferenceName;
        saveToSharedPreferences(preferencesName);
        return this;
    }

    public PiracyChecker enableStoresCheck() {
        this.enableStoresCheck = true;
        return this;
    }

    public PiracyChecker enableDebugCheck() {
        this.enableDebugCheck = true;
        return this;
    }

    public PiracyChecker enableEmulatorCheck() {
        this.enableEmulatorCheck = true;
        return this;
    }

    public PiracyChecker saveResultToSharedPreferences(SharedPreferences preferences,
                                                       @NonNull String preferenceName) {
        this.saveToSharedPreferences = true;
        this.preferenceSaveResult = preferenceName;
        saveToSharedPreferences(preferences);
        return this;
    }

    public PiracyChecker saveResultToSharedPreferences(String preferencesName,
                                                       @NonNull String preferenceName) {
        this.saveToSharedPreferences = true;
        this.preferenceSaveResult = preferenceName;
        saveToSharedPreferences(preferencesName);
        return this;
    }

    private void saveToSharedPreferences(SharedPreferences preferences) {
        if (preferences != null) {
            this.preferences = preferences;
        } else {
            try {
                this.preferences = ((Activity) context).getPreferences(Context.MODE_PRIVATE);
            } catch (Exception e) {
                this.preferences = context.getSharedPreferences(LIBRARY_PREFERENCES_NAME,
                        Context.MODE_PRIVATE);
            }
        }
    }

    private void saveToSharedPreferences(String preferencesName) {
        if (preferencesName != null) {
            this.preferences = context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE);
        } else {
            try {
                this.preferences = ((Activity) context).getPreferences(Context.MODE_PRIVATE);
            } catch (Exception e) {
                this.preferences = context.getSharedPreferences(LIBRARY_PREFERENCES_NAME,
                        Context.MODE_PRIVATE);
            }
        }
    }

    public PiracyChecker display(Display display) {
        this.display = display;
        return this;
    }

    public PiracyChecker withActivityColor(@ColorRes int colorPrimary, @ColorRes int colorPrimaryDark) {
        this.colorPrimary = colorPrimary;
        this.colorPrimaryDark = colorPrimaryDark;
        return this;
    }

    public PiracyChecker callback(PiracyCheckerCallback callback) {
        this.callback = callback;
        return this;
    }

    public void destroy() {
        if (libraryLVLChecker != null) {
            libraryLVLChecker.onDestroy();
        }
    }

    public void start() {
        if (callback != null) {
            verify(callback);
        } else {
            this.callback = new PiracyCheckerCallback() {
                @Override
                public void allow() {
                }

                @Override
                public void dontAllow(@NonNull PiracyCheckerError error, @Nullable PirateApp app) {
                    if (context instanceof Activity && ((Activity) context).isFinishing()) {
                        return;
                    }

                    String dialogContent = unlicensedDialogDescription;
                    if (app != null)
                        dialogContent = context.getString(R.string.unauthorized_app_found, app.getName());
                    else if (error.equals(PiracyCheckerError.BLOCK_PIRATE_APP))
                        dialogContent = context.getString(R.string.unauthorized_app_blocked);

                    if (display == Display.DIALOG)
                        LibraryUtils.buildUnlicensedDialog(context, unlicensedDialogTitle, dialogContent).show();
                    else {
                        Intent intent = new Intent(context, LicenseActivity.class)
                                .putExtra("content", dialogContent)
                                .putExtra("colorPrimary", colorPrimary)
                                .putExtra("colorPrimaryDark", colorPrimaryDark);
                        context.startActivity(intent);
                        ((Activity)context).finish();
                    }
                }
            };
            verify(callback);
        }
    }

    protected void verify(final PiracyCheckerCallback verifyCallback) {
        // Library will check first the non-LVL methods since LVL is asynchronous and could take
        // some seconds to give a result
        if (!verifySigningCertificate()) {
            verifyCallback.dontAllow(PiracyCheckerError.SIGNATURE_NOT_VALID, null);
        } else if (!verifyInstallerId()) {
            verifyCallback.dontAllow(PiracyCheckerError.INVALID_INSTALLER_ID, null);
        } else if (!verifyUnauthorizedApp()) {
            verifyCallback.dontAllow(PiracyCheckerError.BLOCK_PIRATE_APP, null);
        } else {
            if (enableLVL) {
                String deviceId = Settings.Secure.getString(context.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                libraryLVLChecker = new LicenseChecker(context, new
                        ServerManagedPolicy(context, new AESObfuscator(LibraryUtils.SALT, context
                        .getPackageName(), deviceId)), licenseBase64);
                libraryLVLChecker.checkAccess(new LicenseCheckerCallback() {
                    @Override
                    public void allow(int reason) {
                        doExtraVerification(verifyCallback, true);
                    }

                    @Override
                    public void dontAllow(int reason) {
                        doExtraVerification(verifyCallback, reason == Policy.RETRY);
                    }

                    @Override
                    public void applicationError(int errorCode) {
                        verifyCallback.onError(PiracyCheckerError.getCheckerErrorFromCode
                                (errorCode));
                    }
                });
            } else {
                doExtraVerification(verifyCallback, true);
            }
        }
    }

    protected boolean verifySigningCertificate() {
        if (enableSigningCertificate) {
            if (LibraryUtils.verifySigningCertificate(context, signature))
                return true;
        } else
            return true;
        return false;
    }

    protected boolean verifyInstallerId() {
        if (enableInstallerId) {
            if (LibraryUtils.verifyInstallerId(context, installerIDs))
                return true;
        } else
            return true;
        return false;
    }

    protected boolean verifyUnauthorizedApp() {
        if (blockUnauthorized) {
            if (!preferences.getBoolean(preferenceBlockUnauthorized, false))
                return true;
        } else
            return true;
        return false;
    }

    protected void doExtraVerification(final PiracyCheckerCallback verifyCallback, boolean
            possibleSuccess) {
        PirateApp app = LibraryUtils.getPirateApp(context, enableUnauthorizedAppsCheck, enableStoresCheck);
        if (possibleSuccess) {
            if (enableDebugCheck && LibraryUtils.isDebug(context)) {
                if (preferences != null && saveToSharedPreferences)
                    preferences.edit().putBoolean(preferenceSaveResult, false).apply();
                verifyCallback.dontAllow(PiracyCheckerError.USING_DEBUG_APP, null);
            } else if (enableEmulatorCheck && LibraryUtils.isInEmulator()) {
                if (preferences != null && saveToSharedPreferences)
                    preferences.edit().putBoolean(preferenceSaveResult, false).apply();
                verifyCallback.dontAllow(PiracyCheckerError.USING_APP_IN_EMULATOR, null);
            } else if (app != null) {
                if (preferences != null && saveToSharedPreferences)
                    preferences.edit().putBoolean(preferenceSaveResult, false).apply();
                if (preferences != null && blockUnauthorized && app.isUnauthorized())
                    preferences.edit().putBoolean(preferenceBlockUnauthorized, true).apply();
                verifyCallback.dontAllow(app.isUnauthorized() ? PiracyCheckerError.PIRATE_APP_INSTALLED :
                        PiracyCheckerError.THIRD_PARTY_STORE_INSTALLED, app);
            } else {
                if (preferences != null && saveToSharedPreferences)
                    preferences.edit().putBoolean(preferenceSaveResult, true).apply();
                verifyCallback.allow();
            }
        } else {
            if (app != null) {
                if (preferences != null && saveToSharedPreferences)
                    preferences.edit().putBoolean(preferenceSaveResult, false).apply();
                if (preferences != null && blockUnauthorized && app.isUnauthorized())
                    preferences.edit().putBoolean(preferenceBlockUnauthorized, true).apply();
                verifyCallback.dontAllow(app.isUnauthorized() ? PiracyCheckerError.PIRATE_APP_INSTALLED :
                        PiracyCheckerError.THIRD_PARTY_STORE_INSTALLED, app);
            } else {
                if (preferences != null && saveToSharedPreferences)
                    preferences.edit().putBoolean(preferenceSaveResult, false).apply();
                verifyCallback.dontAllow(PiracyCheckerError.NOT_LICENSED, null);
            }
        }
    }

}
