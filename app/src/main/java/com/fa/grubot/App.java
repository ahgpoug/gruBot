package com.fa.grubot;

import android.app.Application;
import android.graphics.Color;
import android.util.Log;

import com.fa.grubot.objects.users.CurrentUser;
import com.fa.grubot.util.TmApiStorage;
import com.github.badoualy.telegram.api.Kotlogram;
import com.github.badoualy.telegram.api.TelegramApp;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.api.UpdateCallback;
import com.github.badoualy.telegram.mtproto.model.DataCenter;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

import java.io.File;

public class App extends Application {
    public static App INSTANCE;

    private boolean isBackstackEnabled = false;
    private boolean areAnimationsEnabled = false;
    private boolean isSlidrEnabled = true;

    private TelegramClient telegramClient;
    private CurrentUser currentUser;

    private static final int API_ID = 0;
    private static final String API_HASH = "";
  
    private static final String APP_VERSION = "1.0";
    private static final String MODEL = "Dev";
    private static final String SYSTEM_VERSION = "Dev";
    private static final String LANG_CODE = "en";

    private File authKeyFile;
    private File nearestDcFile;
    private File vkAccessTokenFile;

    private TelegramApp application;

    private VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                Log.d("TokenTracker", "token has been destroyed");
            } else {
                Log.d("TokenTracker", "token just right");
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        currentUser = new CurrentUser(null, null);

        authKeyFile = new File(this.getApplicationContext().getFilesDir(), "auth.key");
        nearestDcFile = new File(this.getApplicationContext().getFilesDir(), "dc.save");
        vkAccessTokenFile = new File(this.getApplicationContext().getFilesDir(), "vk.token");

        application = new TelegramApp(API_ID, API_HASH, MODEL, SYSTEM_VERSION, APP_VERSION, LANG_CODE);
        INSTANCE = this;

        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(getApplicationContext());
    }

    public TelegramClient getNewTelegramClient(UpdateCallback callback) {
        if (telegramClient != null && !telegramClient.isClosed())
            closeTelegramClient();

        TmApiStorage apiStorage = new TmApiStorage(authKeyFile, nearestDcFile);
        if (callback != null)
            telegramClient = Kotlogram.getDefaultClient(application, apiStorage, new DataCenter("149.154.167.50", 443), callback);
        else
            telegramClient = Kotlogram.getDefaultClient(application, apiStorage, new DataCenter("149.154.167.50", 443));
        return telegramClient;
    }

    public TelegramClient getNewDownloaderClient() {
        if (telegramClient != null && !telegramClient.isClosed())
            return telegramClient.getDownloaderClient();
        else
            return getNewTelegramClient(null).getDownloaderClient();
    }

    public void closeTelegramClient() {
        if (telegramClient != null)
            telegramClient.close(false);
    }

    public String getVkTokenFilePath(){
        return vkAccessTokenFile.getPath();
    }

    public void setCurrentUser(CurrentUser user) {
        this.currentUser = user;
    }

    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    public boolean isBackstackEnabled() {
        return isBackstackEnabled;
    }

    public boolean areAnimationsEnabled() {
        return areAnimationsEnabled;
    }

    public boolean isSlidrEnabled() {
        return isSlidrEnabled;
    }

    public void setBackstackEnabled(boolean backstackEnabled) {
        isBackstackEnabled = backstackEnabled;
    }

    public void setAnimationsEnabled(boolean areAnimationsEnabled) {
        this.areAnimationsEnabled = areAnimationsEnabled;
    }

    public void setSlidrEnabled(boolean slidrEnabled) {
        isSlidrEnabled = slidrEnabled;
    }

    public SlidrConfig getSlidrConfig() {
        return new SlidrConfig.Builder()
                .position(SlidrPosition.LEFT)
                .sensitivity(0.7f)
                .scrimColor(Color.BLACK)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0f)
                .velocityThreshold(2400)
                .distanceThreshold(0.5f)
                .edge(false)
                .build();
    }
}
