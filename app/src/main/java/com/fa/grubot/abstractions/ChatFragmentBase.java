package com.fa.grubot.abstractions;

import com.fa.grubot.objects.chat.ChatMessage;

import java.util.ArrayList;

public interface ChatFragmentBase extends FragmentBase {
    void setupRecyclerView(ArrayList<ChatMessage> messages);
    void setupLayouts(boolean isNetworkAvailable, boolean isHasData);
    void addNewMessagesToList(ArrayList<ChatMessage> messages, boolean moveToTop);
    void onMessageReceived(ChatMessage chatMessage);
    boolean isAdapterExists();
    boolean isListEmpty();
}
