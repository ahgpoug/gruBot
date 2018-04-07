package com.fa.grubot.abstractions;

import com.fa.grubot.objects.chat.Chat;

import java.util.ArrayList;

public interface ChatsListRequestResponse {
    void onChatsListResult(ArrayList<Chat> chats, boolean moveToTop);
    void onFloodException();
}
