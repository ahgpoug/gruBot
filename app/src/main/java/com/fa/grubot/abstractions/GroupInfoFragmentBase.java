package com.fa.grubot.abstractions;

import com.fa.grubot.objects.dashboard.Action;
import com.fa.grubot.objects.users.User;
import com.google.firebase.firestore.DocumentChange;

import java.util.ArrayList;

public interface GroupInfoFragmentBase extends FragmentBase {
    void setupFab();
    void setupButtonClickListeners();
    void setupActionsRecyclerView(String dataType);
    void handleDataUpdate(String dataType, DocumentChange.Type type, int newIndex, int oldIndex, Action action);
    void addParticipants(ArrayList<User> users);
    void setParticipantsCount(int count);
    void setupLayouts(boolean isNetworkAvailable);
    void hideGroupActions(boolean isInList);
    boolean isOneOfTheAdaptersExists();
}
