package com.fa.grubot;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.fa.grubot.fragments.BranchesFragment;
import com.r0adkll.slidr.Slidr;

/**
 * Created by ni.petrov on 18/11/2017.
 */


public class BranchesActivity extends AppCompatActivity {
    private int groupId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        Bundle bundle = new Bundle();
        bundle.putInt("groupId", groupId);

        BranchesFragment fragment = new BranchesFragment();
        fragment.setArguments(bundle);
        fragmentTransaction.replace(R.id.content_branches, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }
}
