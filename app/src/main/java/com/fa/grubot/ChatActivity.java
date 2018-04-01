package com.fa.grubot;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.fa.grubot.fragments.ChatFragment;
import com.fa.grubot.objects.chat.Chat;
import com.r0adkll.slidr.Slidr;

import icepick.Icepick;

public class ChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Icepick.restoreInstanceState(this, savedInstanceState);

        if (App.INSTANCE.isSlidrEnabled())
            Slidr.attach(this, App.INSTANCE.getSlidrConfig());

        Chat chat = (Chat) getIntent().getSerializableExtra("chat");

        if (savedInstanceState == null) {
            Fragment chatFragment = ChatFragment.newInstance(chat);

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.replace(R.id.content, chatFragment);
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
        App.INSTANCE.closeTelegramClient();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        App.INSTANCE.closeTelegramClient();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }
}