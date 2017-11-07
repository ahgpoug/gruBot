package com.fa.grubot.abstractions;

import com.fa.grubot.objects.dashboard.DashboardEntry;

import java.util.ArrayList;

public interface DashboardSpecificFragmentBase {
    void setupRecyclerView(ArrayList<DashboardEntry> entries);
    void setupSwipeRefreshLayout(int layout);
    void setupToolbar();
    void setupLayouts(boolean isNetworkAvailable, boolean isHasData);
    void setupRetryButton();
    void reloadFragment();
}