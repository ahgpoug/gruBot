package com.fa.grubot.abstractions;

import com.fa.grubot.objects.dashboard.Action;
import com.google.firebase.firestore.DocumentChange;

import java.util.ArrayList;

public interface ActionsFragmentBase {
    void setupRecyclerView(ArrayList<Action> entries);
    void setupLayouts(boolean isNetworkAvailable, boolean isHasData);
    void setupRetryButton();
    void setupToolbar();
    void showRequiredViews();
    void handleListUpdate(DocumentChange.Type type, int newIndex, int oldIndex, Action action);
    void showArchiveSnackbar(Action action);
    boolean isAdapterExists();
    boolean isListEmpty();
}
