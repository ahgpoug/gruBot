package com.fa.grubot.abstractions;

import com.fa.grubot.objects.dashboard.Action;

import java.util.ArrayList;

public interface ActionsFragmentBase {
    void setupRecyclerView(ArrayList<Action> entries);
    void setupSwipeRefreshLayout();
    void setupLayouts(boolean isNetworkAvailable, boolean isHasData);
    void setupRetryButton();
    void showRequiredViews();
    void showLoadingView();
}
