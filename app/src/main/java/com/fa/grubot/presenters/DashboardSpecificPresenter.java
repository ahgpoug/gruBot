package com.fa.grubot.presenters;


import android.content.Context;
import android.view.View;

import com.fa.grubot.R;
import com.fa.grubot.abstractions.DashboardSpecificFragmentBase;
import com.fa.grubot.fragments.DashboardSpecificFragment;
import com.fa.grubot.models.DashboardSpecificModel;
import com.fa.grubot.objects.dashboard.DashboardEntry;

import java.util.ArrayList;

public class DashboardSpecificPresenter {
    private DashboardSpecificFragmentBase fragment;
    private DashboardSpecificModel model;
    private ArrayList<DashboardEntry> entries = new ArrayList<>();

    public DashboardSpecificPresenter(DashboardSpecificFragmentBase fragment){
        this.fragment = fragment;
        this.model = new DashboardSpecificModel();
    }

    public void notifyViewCreated(int layout, View v){
        switch (layout) {
            case R.layout.fragment_dashboard_specific:
                fragment.setupToolbar();
                fragment.setupRecyclerView(entries);
                fragment.setupSwipeRefreshLayout(layout);
                break;
            case R.layout.fragment_no_internet_connection:
                fragment.setupRetryButton();
                break;
            case R.layout.fragment_no_data:
                fragment.setupSwipeRefreshLayout(layout);
                break;
        }
    }

    public void updateView(int layout, Context context, int type){
        entries = model.loadEntries(type);
        if (model.isNetworkAvailable(context)) {
            if (layout == R.layout.fragment_dashboard_specific && entries.size() > 0)
                updateDashboardRecyclerView(entries);
            else
                fragment.reloadFragment();
        } else
            fragment.reloadFragment();
    }

    private void updateDashboardRecyclerView(ArrayList<DashboardEntry> entries){
        fragment.setupRecyclerView(entries);
    }

    public void notifyFragmentStarted(Context context, int type){
        boolean isNetworkAvailable = model.isNetworkAvailable(context);
        boolean isHasData = false;
        if (isNetworkAvailable) {
            entries = model.loadEntries(type);
        }

        if (entries.size() > 0)
            isHasData = true;

        fragment.setupLayouts(isNetworkAvailable, isHasData);
    }

    public void onRetryBtnClick(){
        fragment.reloadFragment();
    }

    public void destroy(){
        fragment = null;
        model = null;
    }
}
