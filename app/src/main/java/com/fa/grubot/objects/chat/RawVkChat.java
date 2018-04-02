package com.fa.grubot.objects.chat;

import java.util.Map;

/**
 * Created by ni.petrov on 01/04/2018.
 */

public class RawVkChat extends Chat {
    private final boolean isGroupDialog;
    private final boolean isLastMessageInput;

    public RawVkChat(String id, String name, Map<String, Boolean> users, String imgUri,
                     String lastMessage, String type, long lastMessageDate,
                     String lastMessageFrom, boolean isGroupDialog, String isLastMessageInput) {
        super(id, name, users, imgUri, lastMessage, type, lastMessageDate, lastMessageFrom);
        this.isGroupDialog = isGroupDialog;
        this.isLastMessageInput = isLastMessageInput.equals("1") ? true : false;
    }

    public boolean isGroupDialog() {
        return isGroupDialog;
    }

    public boolean isLastMessageInput(){
        return isLastMessageInput;
    }
}
