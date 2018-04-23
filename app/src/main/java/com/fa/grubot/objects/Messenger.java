package com.fa.grubot.objects;

import android.content.Context;
import android.util.SparseArray;

import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.users.User;

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

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public abstract Observable<Boolean> checkUserAuthObs();
    public abstract Observable<List<Chat>> getChatsListObs();
    public abstract void sendChatMessage();
    public abstract Observable<Object> getChatMessagesListObs(int flag, Chat chat, int totalMessages, final SparseArray<User> inputUsers);
    public abstract User getUser(int userId);
}
