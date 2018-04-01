package com.fa.grubot.presenters;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;

import com.fa.grubot.App;
import com.fa.grubot.abstractions.ChatFragmentBase;
import com.fa.grubot.abstractions.ChatMessageSendRequestResponse;
import com.fa.grubot.abstractions.MessagesListRequestResponse;
import com.fa.grubot.callbacks.TelegramEventCallback;
import com.fa.grubot.helpers.TelegramHelper;
import com.fa.grubot.models.ChatModel;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.chat.ChatMessage;
import com.fa.grubot.objects.events.telegram.TelegramMessageEvent;
import com.fa.grubot.objects.events.telegram.TelegramUpdateUserNameEvent;
import com.fa.grubot.objects.events.telegram.TelegramUpdateUserPhotoEvent;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.util.FragmentState;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.tl.api.messages.TLAbsMessages;

import java.sql.Date;
import java.util.ArrayList;

public class ChatPresenter implements MessagesListRequestResponse, ChatMessageSendRequestResponse {

    private ChatFragmentBase fragment;
    private ChatModel model;
    private Context context;

    private TelegramEventCallback.TelegramEventListener telegramEventListener;

    private String chatId;
    private Chat chat;
    private ChatPresenter presenter = this;
    private TelegramClient client;

    private ArrayList<ChatMessage> messages = new ArrayList<>();

    public ChatPresenter(ChatFragmentBase fragment, Context context) {
        this.fragment = fragment;
        this.context = context;
        model = new ChatModel();
    }

    public void notifyFragmentStarted(Chat chat) {
        this.chat = chat;
        this.chatId = chat.getId();

        if (App.INSTANCE.getCurrentUser().hasTelegramUser())
            model.sendTelegramMessagesRequest(context, presenter, chatId);

        //if (App.INSTANCE.getCurrentUser().hasVkUser())
        //    model.sendVkChatListRequest(this);
    }

    public void sendMessage(String message) {
        model.sendMessage(context, chat, presenter, message);
    }

    @Override
    public void onMessageSent(ChatMessage chatMessage) {
        messages.add(chatMessage);
        fragment.onMessageReceived(chatMessage);
    }

    @Override
    public void onMessagesListResult(ArrayList<ChatMessage> messages, boolean moveToTop) {
        this.messages = messages;

        if (fragment != null) {
            if (messages.isEmpty()) {
                fragment.setupLayouts(true, false);
                notifyViewCreated(FragmentState.STATE_NO_DATA);
                this.messages = messages;
            } else if (!fragment.isAdapterExists() || fragment.isListEmpty()) {
                this.messages = messages;
                fragment.setupLayouts(true, true);
                notifyViewCreated(FragmentState.STATE_CONTENT);
            } else if (fragment.isAdapterExists()) {
                fragment.updateMessagesList(messages, moveToTop);
            }

            if (client == null || client.isClosed())
                setUpdateCallback();
        }
    }

    private void onMessageReceived(ChatMessage chatMessage) {
        if (!messageAlreadyAdded(chatMessage)) {
            messages.add(chatMessage);
            fragment.onMessageReceived(chatMessage);
        }
    }

    private void notifyViewCreated(int state) {
        fragment.showRequiredViews();
        fragment.setupToolbar();

        switch (state) {
            case FragmentState.STATE_CONTENT:
                fragment.setupRecyclerView(messages);
                break;
            case FragmentState.STATE_NO_INTERNET_CONNECTION:
                fragment.setupRetryButton();
                break;
            case FragmentState.STATE_NO_DATA:
                fragment.setupRecyclerView(messages);
                break;
        }
    }

    public void setUpdateCallback() {
        AsyncTask.execute(() -> {
            telegramEventListener = new TelegramEventCallback.TelegramEventListener() {
                @Override
                public void onMessage(TelegramMessageEvent telegramMessageEvent) {
                    Log.d("debug", "Received a message with id: " + telegramMessageEvent.getMessageId() + " from id: " + telegramMessageEvent.getFromId() + " to id: " + telegramMessageEvent.getToId() + " current chat id is: " + chatId);
                    if (telegramMessageEvent.getToId() == Integer.valueOf(chatId)) {
                        int messageId = telegramMessageEvent.getMessageId();
                        String messageText = telegramMessageEvent.getMessage();
                        Date createdAt = new Date(telegramMessageEvent.getDate());
                        int fromId = telegramMessageEvent.getFromId();

                        SparseArray<User> users;
                        User user = null;
                        try {
                            if (fromId == App.INSTANCE.getCurrentUser().getTelegramUser().getId())
                                user = App.INSTANCE.getCurrentUser().getTelegramChatUser();
                            else {
                                TLAbsMessages tlAbsMessages = client.messagesGetHistory(chat.getInputPeer(), 0, 0, 0, 40, 0, 1);
                                users = TelegramHelper.Chats.getChatUsers(client, tlAbsMessages, context);

                                try {
                                    Log.d("debug", "Trying to get user with id: " + fromId);
                                    user = users.get(fromId);
                                } catch (Exception e) {
                                    Log.d("debug", "Is not a user, trying to get chat with id: " + fromId);
                                    user = TelegramHelper.Chats.getChatAsUser(client.getDownloaderClient(), fromId, context);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (user != null) {
                            ChatMessage chatMessage = new ChatMessage(String.valueOf(messageId), messageText, user, createdAt);
                            ((AppCompatActivity) context).runOnUiThread(() -> presenter.onMessageReceived(chatMessage));
                        }
                    }
                }

                @Override
                public void onUserNameUpdate(TelegramUpdateUserNameEvent telegramUpdateUserNameEvent) {
                }

                @Override
                public void onUserPhotoUpdate(TelegramUpdateUserPhotoEvent telegramUpdateUserPhotoEvent) {
                }
            };
            client = App.INSTANCE.getNewTelegramClient(new TelegramEventCallback(telegramEventListener, context));
        });
    }

    public void onRetryBtnClick() {
        model.sendTelegramMessagesRequest(context, presenter, chatId);
    }

    private boolean messageAlreadyAdded(ChatMessage chatMessage) {
        for (ChatMessage message : messages) {
            if (message.getId().equals(chatMessage.getId()))
                return true;
        }

        return false;
    }

    public void destroy() {
        if (client != null)
            client.close(false);
        fragment = null;
        model = null;
    }
}
