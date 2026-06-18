package com.niit.memory.util;

import android.content.Context;
import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;
import io.reactivex.rxjava3.core.Single;

public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "memory_session";
    private static SessionManager instance;
    private final RxDataStore<Preferences> dataStore;

    private static final Preferences.Key<String> KEY_TOKEN = PreferencesKeys.stringKey("token");
    private static final Preferences.Key<String> KEY_USER_ID = PreferencesKeys.stringKey("userId");
    private static final Preferences.Key<String> KEY_USERNAME = PreferencesKeys.stringKey("username");
    private static final Preferences.Key<String> KEY_NICKNAME = PreferencesKeys.stringKey("nickname");
    private static final Preferences.Key<String> KEY_AVATAR_URL = PreferencesKeys.stringKey("avatarUrl");
    private static final Preferences.Key<String> KEY_HIS_AVATAR_URL = PreferencesKeys.stringKey("hisAvatarUrl");
    private static final Preferences.Key<String> KEY_HER_AVATAR_URL = PreferencesKeys.stringKey("herAvatarUrl");

    // In-memory cache for sync access (volatile for cross-thread visibility)
    private volatile String token;
    private volatile String userId;
    private volatile String username;
    private volatile String nickname;
    private volatile String avatarUrl;
    private volatile String hisAvatarUrl;
    private volatile String herAvatarUrl;

    private SessionManager(Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context, PREF_NAME).build();
        loadFromDisk();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    private void loadFromDisk() {
        try {
            Preferences prefs = dataStore.data().blockingFirst();
            token = prefs.get(KEY_TOKEN);
            userId = prefs.get(KEY_USER_ID);
            username = prefs.get(KEY_USERNAME);
            nickname = prefs.get(KEY_NICKNAME);
            avatarUrl = prefs.get(KEY_AVATAR_URL);
            hisAvatarUrl = prefs.get(KEY_HIS_AVATAR_URL);
            herAvatarUrl = prefs.get(KEY_HER_AVATAR_URL);
        } catch (Exception e) {
            android.util.Log.e("SessionManager", "Failed to load session from DataStore", e);
        }
    }

    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getNickname() { return nickname; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getHisAvatarUrl() { return hisAvatarUrl; }
    public String getHerAvatarUrl() { return herAvatarUrl; }
    public boolean isLoggedIn() { return token != null && !token.isEmpty(); }

    public void saveLogin(String token, String userId, String username, String nickname, String avatarUrl) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        dataStore.updateDataAsync(prefs -> {
            MutablePreferences mp = prefs.toMutablePreferences();
            mp.set(KEY_TOKEN, token != null ? token : "");
            mp.set(KEY_USER_ID, userId != null ? userId : "");
            mp.set(KEY_USERNAME, username != null ? username : "");
            mp.set(KEY_NICKNAME, nickname != null ? nickname : "");
            mp.set(KEY_AVATAR_URL, avatarUrl != null ? avatarUrl : "");
            return Single.just(mp);
        }).subscribe(prefs -> {}, error -> android.util.Log.e(TAG, "Failed to persist session", error));
    }

    public void saveAvatarUrl(String url) {
        this.avatarUrl = url;
        dataStore.updateDataAsync(prefs -> {
            MutablePreferences mp = prefs.toMutablePreferences();
            mp.set(KEY_AVATAR_URL, url != null ? url : "");
            return Single.just(mp);
        }).subscribe(prefs -> {}, error -> android.util.Log.e(TAG, "Failed to persist avatar", error));
    }

    public void saveHisAvatarUrl(String url) {
        this.hisAvatarUrl = url;
        dataStore.updateDataAsync(prefs -> {
            MutablePreferences mp = prefs.toMutablePreferences();
            mp.set(KEY_HIS_AVATAR_URL, url != null ? url : "");
            return Single.just(mp);
        }).subscribe(prefs -> {}, error -> android.util.Log.e(TAG, "Failed to persist his avatar", error));
    }

    public void saveHerAvatarUrl(String url) {
        this.herAvatarUrl = url;
        dataStore.updateDataAsync(prefs -> {
            MutablePreferences mp = prefs.toMutablePreferences();
            mp.set(KEY_HER_AVATAR_URL, url != null ? url : "");
            return Single.just(mp);
        }).subscribe(prefs -> {}, error -> android.util.Log.e(TAG, "Failed to persist her avatar", error));
    }

    public void logout() {
        this.token = null;
        this.userId = null;
        this.username = null;
        this.nickname = null;
        this.avatarUrl = null;
        dataStore.updateDataAsync(prefs -> {
            MutablePreferences mp = prefs.toMutablePreferences();
            mp.set(KEY_TOKEN, "");
            mp.set(KEY_USER_ID, "");
            mp.set(KEY_USERNAME, "");
            mp.set(KEY_NICKNAME, "");
            mp.set(KEY_AVATAR_URL, "");
            return Single.just(mp);
        }).subscribe(prefs -> {}, error -> android.util.Log.e(TAG, "Failed to persist logout", error));
    }
}
