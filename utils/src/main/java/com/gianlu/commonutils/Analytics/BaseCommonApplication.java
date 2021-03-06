package com.gianlu.commonutils.Analytics;

import android.app.Application;

import com.gianlu.commonutils.CommonPK;
import com.gianlu.commonutils.CommonUtils;
import com.gianlu.commonutils.Logging;
import com.gianlu.commonutils.Preferences.Prefs;
import com.gianlu.commonutils.Preferences.PrefsStorageModule;
import com.yarolegovich.mp.io.MaterialPreferences;

import androidx.annotation.CallSuper;

public abstract class BaseCommonApplication extends Application implements Thread.UncaughtExceptionHandler {

    @SuppressWarnings("deprecation")
    private void deprecatedBackwardCompatibility() {
        if (Prefs.has(CommonPK.TRACKING_DISABLE)) {
            boolean old = Prefs.getBoolean(CommonPK.TRACKING_DISABLE, false);
            Prefs.putBoolean(CommonPK.TRACKING_ENABLED, !old);
            Prefs.putBoolean(CommonPK.CRASH_REPORT_ENABLED, !old);
            Prefs.remove(CommonPK.TRACKING_DISABLE);
        }
    }

    /**
     * Never called in debug.
     */
    @Override
    public final void uncaughtException(Thread thread, Throwable throwable) {
        Logging.log(throwable);

        if (uncaughtNotDebug(thread, throwable))
            UncaughtExceptionActivity.startActivity(this, throwable);
    }

    protected boolean uncaughtNotDebug(Thread thread, Throwable throwable) {
        return true;
    }

    protected abstract boolean isDebug();

    @Override
    @CallSuper
    public void onCreate() {
        super.onCreate();

        Prefs.init(this);

        CommonUtils.setDebug(isDebug());
        Logging.init(this);
        Logging.clearLogs(this);
        if (!isDebug()) Thread.setDefaultUncaughtExceptionHandler(this);

        deprecatedBackwardCompatibility();

        MaterialPreferences.instance().setStorageModule(new PrefsStorageModule.Factory());
    }
}
