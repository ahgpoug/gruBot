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
import com.fa.grubot.objects.misc.CombinedMessagesListObject;
import com.fa.grubot.objects.pojos.PollingSererInfo;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.util.Consts;
import com.fa.grubot.util.Globals;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.tl.api.messages.TLAbsMessages;
import com.google.gson.Gson;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPoll;

import org.json.JSONException;

import java.sql.Date;
import java.util.ArrayList;

import rx.Single;

import static com.fa.grubot.util.Consts.STATE_NO_INTERNET_CONNECTION;

public class ChatPresenter implements MessagesListRequestResponse, ChatMessageSendRequestResponse {

    private ChatFragmentBase fragment;
    private ChatModel model;
    private Context context;

    private TelegramEventCallback.TelegramEventListener telegramEventListener;

    private Chat chat;
    private ChatPresenter presenter = this;
    private TelegramClient client;

    private ArrayList<ChatMessage> messages = new ArrayList<>();
    private SparseArray<User> users = new SparseArray<>();

    public ChatPresenter(ChatFragmentBase fragment, Context context) {
        this.fragment = fragment;
        this.context = context;
        model = new ChatModel();
    }

    public void notifyFragmentStarted(Chat chat) {
        this.chat = chat;

        if (Globals.InternetMethods.isNetworkAvailable(context)) {
            if (chat.getType().equals(Consts.Telegram)) {
                model.sendTelegramMessagesRequest(context, presenter, chat, Consts.FLAG_LOAD_FIRST_MESSAGES, 0, users);
            } else if (chat.getType().equals(Consts.VK)) {
                model.sendVkMessagesRequest(context, presenter, chat, Consts.FLAG_LOAD_FIRST_MESSAGES, 0, users);
            }
        } else {
            notifyViewCreated(STATE_NO_INTERNET_CONNECTION);
        }
    }

    public void sendMessage(String message) {
        if (chat.getType().equals(Consts.Telegram)) {
            model.sendMessage(context, chat, presenter, message);
        } else if (chat.getType().equals(Consts.VK)) {
            model.sendVkMessage(context, chat, presenter, message);
        }

    }

    @Override
    public void onMessageSent(ChatMessage chatMessage) {
        messages.add(chatMessage);
        fragment.onMessageReceived(chatMessage);
    }

    @Override
    public void onMessagesListResult(CombinedMessagesListObject combinedMessagesListObject, int flag, boolean moveToTop) {
        this.messages = combinedMessagesListObject.getMessages();
        this.users = combinedMessagesListObject.getUsers();

        if (fragment != null) {
            switch (flag) {
                case Consts.FLAG_LOAD_FIRST_MESSAGES:
                    if (messages.isEmpty()) {
                        fragment.setupLayouts(true, false);
                        notifyViewCreated(Consts.STATE_NO_DATA);
                    } else if (!fragment.isAdapterExists() || fragment.isListEmpty()) {
                        fragment.setupLayouts(true, true);
                        notifyViewCreated(Consts.STATE_CONTENT);
                    }

                    if (client == null || client.isClosed())
                        setUpdateCallback();
                    break;
                case Consts.FLAG_LOAD_NEW_MESSAGES:
                    fragment.addNewMessagesToList(messages, false);
                    break;
            }
        }
    }

    private void onMessageReceived(ChatMessage chatMessage) {
        if (!messageAlreadyAdded(chatMessage)) {
            messages.add(chatMessage);
            fragment.onMessageReceived(chatMessage);
        }
    }

    public void loadMoreMessages(int totalCount) {
        model.sendTelegramMessagesRequest(context, presenter, chat, Consts.FLAG_LOAD_NEW_MESSAGES, totalCount, users);
    }

    private void notifyViewCreated(int state) {
        fragment.showRequiredViews();
        fragment.setupToolbar();

        switch (state) {
            case Consts.STATE_CONTENT:
                fragment.setupRecyclerView(messages, chat.getType());
                break;
            case STATE_NO_INTERNET_CONNECTION:
                fragment.setupRetryButton();
                break;
            case Consts.STATE_NO_DATA:
                fragment.setupRecyclerView(messages, chat.getType());
                break;
        }
    }

    public void setUpdateCallback() {
        AsyncTask.execute(() -> {
            telegramEventListener = new TelegramEventCallback.TelegramEventListener() {
                @Override
                public void onMessage(TelegramMessageEvent telegramMessageEvent) {
                    //Log.d("debug", "Received a message with id: " + telegramMessageEvent.getMessageId() + " from id: " + telegramMessageEvent.getFromId() + " to id: " + telegramMessageEvent.getToId() + " current chat id is: " + chatId);
                    if (telegramMessageEvent.getToId() == Integer.valueOf(chat.getId())) {
                        int messageId = telegramMessageEvent.getMessageId();
                        String messageText = telegramMessageEvent.getMessage();
                        Date createdAt = new Date(telegramMessageEvent.getDate());
                        int fromId = telegramMessageEvent.getFromId();

                        User user = users.get(fromId);
                        if (user == null) {
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
            if (Globals.InternetMethods.isNetworkAvailable(context))
                client = App.INSTANCE.getNewTelegramClient(new TelegramEventCallback(telegramEventListener, context));
            else
                notifyViewCreated(STATE_NO_INTERNET_CONNECTION);
        });
    }

    public void setupPollingVk() {
        VKRequest request = new VKRequest("messages.getLongPollServer",
                VKParameters.from("need_pts", 1));
        Single.create(ss -> {
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    Gson gson = new Gson();
                    try {
                        PollingSererInfo psi = gson.fromJson(response.json.getJSONObject("response").toString(), PollingSererInfo.class);
                        ss.onSuccess(psi);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(VKError error) {
                    super.onError(error);
                }
            });
        }).map(obj -> (PollingSererInfo) obj)
                .subscribe(psi -> {
                    VKRequest requesPolling = new VKRequest("messages.getLongPollHistory",
                            VKParameters.from("ts", psi.getTs()));
                    requesPolling.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);

                        }

                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                        }
                    });
                });

    }

    public void onRetryBtnClick() {
        if (Globals.InternetMethods.isNetworkAvailable(context))
            if (chat.getType().equals(Consts.Telegram)) {
                model.sendTelegramMessagesRequest(context, presenter, chat, Consts.FLAG_LOAD_FIRST_MESSAGES, 0, users);
            } else if (chat.getType().equals(Consts.VK)) {
                model.sendVkMessagesRequest(context, presenter, chat, Consts.FLAG_LOAD_FIRST_MESSAGES, 0, users);
            }
    }

    public void retryLoad(Chat chat, int flag, int totalMessages, SparseArray<User> users) {
        if (chat.getType().equals(Consts.Telegram)) {
            model.sendTelegramMessagesRequest(context, presenter, chat, Consts.FLAG_LOAD_NEW_MESSAGES, totalMessages, users);
        } else if (chat.getType().equals(Consts.VK)) {
            model.sendVkMessagesRequest(context, presenter, chat, Consts.FLAG_LOAD_NEW_MESSAGES, 0, users);
        }
    }

    private boolean messageAlreadyAdded(ChatMessage chatMessage) {
        for (ChatMessage message : messages) {
            if (message.getId().equals(chatMessage.getId()))
                return true;
        }

        return false;
    }

    public void destroy() {
        if (client != null && !client.isClosed())
            client.close(false);
        fragment = null;
        model = null;
    }

    public void loadmoreVkMessages() {
    }
}
