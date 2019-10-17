package de.danoeh.antennapod.core.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import de.danoeh.antennapod.core.BuildConfig;
import de.danoeh.antennapod.core.ClientConfig;

/**
 * Manages preferences for accessing Inaudible
 */
public class InaudiblePreferences {

    private InaudiblePreferences(){}

    private static final String TAG = "InaudiblePreferences";

    public static final String PREF_ROOT = "inaudible";
    public static final String PREF_KEY_URL = "url";
    public static final String PREF_KEY_SYNC = "sync";
    public static final String PREF_KEY_SYNC_NOW = "sync_now";
    public static final String PREF_KEY_NOTIFICATIONS = "notifications";


    public static final String DEFAULT_URL = "https://example.com";
    public static final boolean DEFAULT_NOTIFY = true;

    private static String url;
    private static boolean sync;
    private static boolean notifications;

    private static boolean preferencesLoaded = false;

    private static SharedPreferences getPreferences() {
        return ClientConfig.applicationCallbacks.getApplicationInstance().getSharedPreferences(PREF_ROOT, Context.MODE_PRIVATE);
    }

    public static void registerOnSharedPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener) {
        getPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

    private static synchronized void ensurePreferencesLoaded() {
        if (!preferencesLoaded) {
            SharedPreferences prefs = getPreferences();
            url = prefs.getString(PREF_KEY_URL, DEFAULT_URL);
            notifications = prefs.getBoolean(PREF_KEY_NOTIFICATIONS, DEFAULT_NOTIFY);
            sync = prefs.getBoolean(PREF_KEY_SYNC, false);
            preferencesLoaded = true;
        }
    }


    public static boolean syncEnabled() {
        ensurePreferencesLoaded();
        return sync;
    }

    public static void setEnabled(boolean value) {
        sync = value;
        writePreference(PREF_KEY_SYNC, value);
    }

    public static boolean notificationsEnabled() {
        ensurePreferencesLoaded();
        return notifications;
    }

    public static void setNotifications(boolean value) {
        notifications = value;
        writePreference(PREF_KEY_NOTIFICATIONS, value);
    }

    public static String getURL() {
        ensurePreferencesLoaded();
        return url;
    }

    public static void setURL(String value) {
        url = value;
        writePreference(PREF_KEY_URL, url);
        setEnabled(true);
    }

    private static void writePreference(String key, String value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(key, value);
        editor.apply();
    }

    private static void writePreference(String key, boolean value) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

}
