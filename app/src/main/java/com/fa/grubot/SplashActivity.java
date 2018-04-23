package com.fa.grubot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.fa.grubot.objects.TelegramMessenger;
import com.fa.grubot.objects.VkMessenger;
import com.fa.grubot.objects.misc.AuthObject;
import com.fa.grubot.util.Globals;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.fa.grubot.App.INSTANCE;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadPreferences();
        if (Globals.InternetMethods.isNetworkAvailable(this)) {
            checkAuth();
        } else {
            Toast.makeText(this, "Нет подключения к сети", Toast.LENGTH_LONG).show();
            this.finishAffinity();
        }
    }

    private void loadPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        INSTANCE.setAnimationsEnabled(prefs.getBoolean("animationsSwitch", false));
        INSTANCE.setBackstackEnabled(prefs.getBoolean("backstackSwitch", false));
        INSTANCE.setSlidrEnabled(prefs.getBoolean("slidrSwitch", true));
    }

    @Override
    protected void onDestroy() {
        App.INSTANCE.closeTelegramClient();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        App.INSTANCE.closeTelegramClient();
        super.onPause();
    }

    @Override
    protected void onStop() {
        App.INSTANCE.closeTelegramClient();
        super.onStop();
    }

    private void checkAuth() {
        Observable<Boolean> telegramAuthObs = App.INSTANCE.telegramMessenger.checkUserAuthObs().timeout(3, TimeUnit.SECONDS).onErrorResumeNext(Observable.empty()).defaultIfEmpty(false);
        Observable<Boolean> vkAuthObs = App.INSTANCE.vkMessenger.checkUserAuthObs().timeout(3, TimeUnit.SECONDS).onErrorResumeNext(Observable.empty()).defaultIfEmpty(false);

        Observable
                .zip(telegramAuthObs, vkAuthObs, (v, t) -> new AuthObject(t, v))
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(authObject -> {
                    if (authObject.isHasTelegramAuth() || authObject.isHasVkAuth())
                        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                    else
                        startActivity(new Intent(this, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                });
    }
}
