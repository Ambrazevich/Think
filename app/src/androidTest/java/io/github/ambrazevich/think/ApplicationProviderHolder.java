package io.github.ambrazevich.think;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

final class ApplicationProviderHolder {
    private ApplicationProviderHolder() {
    }

    static Context context() {
        return ApplicationProvider.getApplicationContext();
    }
}
