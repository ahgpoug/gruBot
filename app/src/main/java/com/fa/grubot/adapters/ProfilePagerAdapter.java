package com.fa.grubot.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.fa.grubot.App;
import com.fa.grubot.fragments.ProfileItemFragment;
import com.fa.grubot.util.Consts;

/**
 * Created by ni.petrov on 03/04/2018.
 */

public class ProfilePagerAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;

    public ProfilePagerAdapter(FragmentManager fm, int mNumOfTabs) {
        super(fm);
        this.mNumOfTabs = mNumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return ProfileItemFragment.newInstance(App.INSTANCE.getCurrentUser().getVkUser().getId(), Consts.VK, null, Consts.PROFILE_MODE_DUAL);
            case 1:
                return ProfileItemFragment.newInstance(App.INSTANCE.getCurrentUser().getTelegramUser().getId(), Consts.Telegram, App.INSTANCE.getCurrentUser().getTelegramChatUser(), Consts.PROFILE_MODE_DUAL);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
