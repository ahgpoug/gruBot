package com.fa.grubot.objects;

import android.content.Context;

import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.chat.ChatMessage;
import com.fa.grubot.objects.users.User;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public abstract class Messenger {
    protected Context context;
    private User currentUser;

    Messenger(Context context) {
        this.context = context;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isHasUser() {
        return currentUser != null;
    }

    public void setChatUser(User user) {
        this.currentUser = user;
    }

    public abstract Observable<Boolean> checkUserAuthObs();
    public abstract Observable<List<Chat>> getChatsListObs();
    public abstract void sendChatMessage();
    public abstract ArrayList<ChatMessage> getChatMessagesListObs(int start, int end);
    public abstract User getUser(int userId);
}
