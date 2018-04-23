package com.fa.grubot.models;

import com.fa.grubot.App;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.events.telegram.TelegramMessageEvent;
import com.fa.grubot.objects.events.telegram.TelegramUpdateUserNameEvent;
import com.fa.grubot.objects.events.telegram.TelegramUpdateUserPhotoEvent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class ChatsListModel {

    public Observable<List<Chat>> sendChatsListRequest() {
        return App.INSTANCE.telegramMessenger.getChatsListObs();
    }

    public Observable<List<Chat>> sendVkChatListRequest() {
        return App.INSTANCE.vkMessenger.getChatsListObs();
    }

    public ArrayList<Chat> onNewMessage(ArrayList<Chat> chats, TelegramMessageEvent event) {
        Chat chatToChange = null;
        int index = -1;
        int idToFind;

        if (event.getToId() == App.INSTANCE.getCurrentUser().getTelegramUser().getId())
            idToFind = event.getFromId();
        else
            idToFind = event.getToId();

        for (Chat chat : chats) {
            if (Integer.valueOf(chat.getId()) == idToFind) {
                chatToChange = chat;
                index = chats.indexOf(chat);
                break;
            }
        }

        if (chatToChange != null) {
            chatToChange.setLastMessage(event.getMessage());
            chatToChange.setLastMessageDate(event.getDate());
            chatToChange.setLastMessageFrom(event.getNameFrom());
            chats.remove(index);
            chats.add(0, chatToChange);
        }

        return chats;
    }

    public ArrayList<Chat> onUserPhotoUpdate(ArrayList<Chat> chats, TelegramUpdateUserPhotoEvent event) {
        Chat chatToChange = null;
        int index = -1;
        int idToFind = event.getUserId();

        for (Chat chat : chats) {
            if (Integer.valueOf(chat.getId()) == idToFind) {
                chatToChange = chat;
                index = chats.indexOf(chat);
                break;
            }
        }

        if (chatToChange != null) {
            chatToChange.setImgUri(event.getImgUri());
            chats.remove(index);
            chats.add(index, chatToChange);
        }

        return chats;
    }

    public ArrayList<Chat> onUserNameUpdate(ArrayList<Chat> chats, TelegramUpdateUserNameEvent event) {
        Chat chatToChange = null;
        int index = -1;
        int idToFind = event.getUserId();

        for (Chat chat : chats) {
            if (Integer.valueOf(chat.getId()) == idToFind) {
                chatToChange = chat;
                index = chats.indexOf(chat);
                break;
            }
        }

        String newName = (event.getFirstName() + " " + event.getLastName()).replace("null", "").trim();
        if (newName.isEmpty())
            newName = event.getUserName();

        if (chatToChange != null) {
            chatToChange.setName(newName);
            chats.remove(index);
            chats.add(index, chatToChange);
        }

        return chats;
    }
}
