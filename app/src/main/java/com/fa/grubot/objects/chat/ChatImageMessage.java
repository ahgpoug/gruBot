package com.fa.grubot.objects.chat;

import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

public class ChatImageMessage extends ChatMessage {
    private String imgUri;

    public ChatImageMessage(String id, String text, IUser user, Date createdAt, String imgUri) {
        super(id, text, user, createdAt);
        this.imgUri = imgUri;
    }

    public String getImgUri() {
        return imgUri;
    }
}
