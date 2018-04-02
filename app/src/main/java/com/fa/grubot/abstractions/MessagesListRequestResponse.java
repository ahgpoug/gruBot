package com.fa.grubot.abstractions;

import com.fa.grubot.objects.chat.ChatMessage;
import com.fa.grubot.objects.misc.CombinedMessagesListObject;

import java.util.ArrayList;

public interface MessagesListRequestResponse {
    void onMessagesListResult(CombinedMessagesListObject combinedMessagesListObject, int flag, boolean moveToTop);
}
