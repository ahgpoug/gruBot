package com.fa.grubot.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.fa.grubot.fragments.ActionsFragment;

public class ActionsPagerAdapter extends FragmentPagerAdapter {

    private int type;

    private ActionsFragment currentActions;
    private ActionsFragment archiveActions;

    public ActionsPagerAdapter(FragmentManager fragmentManager, int type) {
        super(fragmentManager);
        this.type = type;
        setFragments();
    }

    private void setFragments() {
        ActionsFragment actionsFragment = new ActionsFragment();
        Bundle args = new Bundle();
        if (type == ActionsFragment.TYPE_ANNOUNCEMENTS)
            args.putInt("type", ActionsFragment.TYPE_ANNOUNCEMENTS);
        else
            args.putInt("type", ActionsFragment.TYPE_VOTES);
        actionsFragment.setArguments(args);
        currentActions = actionsFragment;

        ActionsFragment actionsArchiveFragment = new ActionsFragment();
        Bundle args1 = new Bundle();
        if (type == ActionsFragment.TYPE_ANNOUNCEMENTS)
            args1.putInt("type", ActionsFragment.TYPE_ANNOUNCEMENTS_ARCHIVE);
        else
            args1.putInt("type", ActionsFragment.TYPE_VOTES_ARCHIVE);
        actionsArchiveFragment.setArguments(args1);
        archiveActions = actionsArchiveFragment;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0)
            return currentActions;
        else
            return archiveActions;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Текущие";
            case 1:
                return "Архив";
        }
        return null;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }
}
