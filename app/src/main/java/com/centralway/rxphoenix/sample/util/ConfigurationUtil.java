package com.centralway.rxphoenix.sample.util;

import android.content.res.Configuration;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

public class ConfigurationUtil {

    /**
     * Changes system locale to the one provided. Requires {@link android.Manifest.permission#CHANGE_CONFIGURATION}
     * to be executed successfully.
     *
     * @param newLocale to be set as system default.
     */
    public static void changeSystemLocale(Locale newLocale) {
        try {
            Class amnClass = Class.forName("android.app.ActivityManagerNative");
            Method methodGetDefault = amnClass.getMethod("getDefault");
            methodGetDefault.setAccessible(true);
            Object amn = methodGetDefault.invoke(amnClass);

            // config = amn.getConfiguration();
            Method methodGetConfiguration = amnClass.getMethod("getConfiguration");
            methodGetConfiguration.setAccessible(true);
            Configuration config = (Configuration) methodGetConfiguration.invoke(amn);
            Class configClass = config.getClass();
            Field f = configClass.getField("userSetLocale");
            f.setBoolean(config, true);

            // set the locale to the new value
            config.locale = newLocale;

            // amn.updateConfiguration(config);
            Method methodUpdateConfiguration = amnClass.getMethod("updateConfiguration", Configuration.class);
            methodUpdateConfiguration.setAccessible(true);
            methodUpdateConfiguration.invoke(amn, config);
        } catch (Exception e) {
            Log.d("ConfigurationUtil", "Exception while trying to set system lang.", e);
        }
    }
}
