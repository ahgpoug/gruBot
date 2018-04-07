package com.fa.grubot.profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import com.fa.grubot.adapters.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fa.grubot.MainActivity;
import com.fa.grubot.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;

/**
 * Created by ni.petrov on 04/04/2018.
 */

public class ProfileFragmentV2 extends Fragment {

    @BindView(R.id.tab_layout_profile)
    TabLayout tabLayout;

    @BindView(R.id.viewpager_profile)
    ViewPager viewPager;

    private PagerAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile_v2, container, false);
        hideMainToolBar();
        ButterKnife.bind(this, v);
        tabLayout.addTab(tabLayout.newTab().setText("VK"));
        tabLayout.addTab(tabLayout.newTab().setText("Telegram"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mAdapter = new com.fa.grubot.adapters.PagerAdapter(getChildFragmentManager(), tabLayout.getTabCount());

        viewPager.setAdapter(mAdapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return v;
    }

    private void hideMainToolBar() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);

        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    public static ProfileFragmentV2 getInstanse(){
        return new ProfileFragmentV2();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

}
