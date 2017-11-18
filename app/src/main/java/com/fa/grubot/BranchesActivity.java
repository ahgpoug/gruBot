package com.fa.grubot;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.fa.grubot.fragments.BranchesFragment;
import com.fa.grubot.fragments.ChatFragment;
import com.r0adkll.slidr.Slidr;

/**
 * Created by ni.petrov on 18/11/2017.
 */

public class BranchesActivity extends AppCompatActivity{
    private int groupId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.activity_branches);

        init();
    }


    private void init(){
        if (App.INSTANCE.isSlidrEnabled())
            Slidr.attach(this, App.INSTANCE.getSlidrConfig());


        Intent intent = getIntent();
        groupId = intent.getIntExtra("groupId", 0);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        BranchesFragment fragment = new BranchesFragment();
        fragmentTransaction.replace(R.id.content, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }
}
