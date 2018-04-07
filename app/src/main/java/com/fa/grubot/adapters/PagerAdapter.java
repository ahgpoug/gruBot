package com.fa.grubot.adapters;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fa.grubot.profile.ProfileItemFragment;
import com.fa.grubot.util.Consts;

/**
 * Created by ni.petrov on 03/04/2018.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int mNumOfTabs) {
        super(fm);
        this.mNumOfTabs = mNumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ProfileItemFragment tab1 = new ProfileItemFragment();
                Bundle args1 = new Bundle();
                args1.putString("type", Consts.VK);
                tab1.setArguments(args1);
                return tab1;
            case 1:
                ProfileItemFragment tab2 = new ProfileItemFragment();
                Bundle args2 = new Bundle();
                args2.putString("type", Consts.Telegram);
                tab2.setArguments(args2);
                return tab2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
