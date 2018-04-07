package com.fa.grubot.abstractions;

import android.util.SparseArray;

import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.misc.CombinedMessagesListObject;
import com.fa.grubot.objects.users.User;

public interface MessagesListRequestResponse {
    void onMessagesListResult(CombinedMessagesListObject combinedMessagesListObject, int flag, boolean moveToTop);
    void retryLoad(Chat chat, int flag, int totalMessages, SparseArray<User> users);
}
