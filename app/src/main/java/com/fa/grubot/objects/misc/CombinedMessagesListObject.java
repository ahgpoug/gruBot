package com.fa.grubot.objects.misc;

import android.util.SparseArray;

import com.fa.grubot.objects.chat.ChatMessage;
import com.fa.grubot.objects.users.User;

import java.util.ArrayList;

public class CombinedMessagesListObject {
    private ArrayList<ChatMessage> messages;
    private SparseArray<User> users;

    public CombinedMessagesListObject(ArrayList<ChatMessage> messages, SparseArray<User> users) {
        this.messages = messages;
        this.users = users;
    }

    public ArrayList<ChatMessage> getMessages() {
        return messages;
    }

    public SparseArray<User> getUsers() {
        return users;
    }
}
