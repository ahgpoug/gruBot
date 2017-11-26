package com.fa.grubot;

import android.app.Application;
import android.graphics.Color;

import com.fa.grubot.helpers.TemporaryDataHelper;
import com.fa.grubot.objects.group.User;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

public class App extends Application {
    public static App INSTANCE;
    private TemporaryDataHelper temporaryDataHelper;

    private boolean isBackstackEnabled = false;
    private boolean areAnimationsEnabled = false;
    private boolean isSlidrEnabled = true;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;
        temporaryDataHelper = new TemporaryDataHelper();
    }

    public TemporaryDataHelper getDataHelper() {
        return temporaryDataHelper;
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

    public User getCurrentUser() {
        return new User(0, "DOMINATOR48RUS", "The First One", "+71903322233", "NOONE CAN STOP ME", "http://www.netlore.ru/upload/files/1307/3_321.jpg");
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
