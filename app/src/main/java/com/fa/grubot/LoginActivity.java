package com.fa.grubot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.fa.grubot.fragments.InitialLoginFragment;

import icepick.Icepick;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setContentView(R.layout.activity_login);

        if (savedInstanceState == null) {
            Fragment initialLoginFragment = InitialLoginFragment.newInstance();

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.content, initialLoginFragment);
            transaction.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    protected void onDestroy() {
        if (App.INSTANCE.hasTelegramClient()) {
            App.INSTANCE.closeTelegramClient();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (App.INSTANCE.hasTelegramClient()) {
            App.INSTANCE.closeTelegramClient();
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (App.INSTANCE.hasTelegramClient()) {
            App.INSTANCE.closeTelegramClient();
        }
        super.onStop();
    }
}
