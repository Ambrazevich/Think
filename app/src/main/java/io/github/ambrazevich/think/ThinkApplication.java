package io.github.ambrazevich.think;

import android.app.Application;

import io.github.ambrazevich.think.gameutils.AppLanguageManager;

public class ThinkApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppLanguageManager.migrateLegacyLocaleIfNeeded(this);
    }
}
