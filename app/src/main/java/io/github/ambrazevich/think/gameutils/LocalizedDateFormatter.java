package io.github.ambrazevich.think.gameutils;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public final class LocalizedDateFormatter {
    private LocalizedDateFormatter() {
    }

    public static String format(Context context, long timestamp) {
        return format(timestamp, getResourceLocale(context));
    }

    static String format(long timestamp, Locale locale) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.SHORT, locale);
        return dateFormat.format(new Date(timestamp));
    }

    private static Locale getResourceLocale(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return configuration.getLocales().get(0);
        }
        //noinspection deprecation
        return configuration.locale;
    }
}
