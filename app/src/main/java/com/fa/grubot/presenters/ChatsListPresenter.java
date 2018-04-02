package com.fa.grubot.presenters;


import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;

import com.fa.grubot.App;
import com.fa.grubot.abstractions.ChatsListFragmentBase;
import com.fa.grubot.abstractions.ChatsListRequestResponse;
import com.fa.grubot.callbacks.TelegramEventCallback;
import com.fa.grubot.models.ChatsListModel;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.events.telegram.TelegramMessageEvent;
import com.fa.grubot.objects.events.telegram.TelegramUpdateUserNameEvent;
import com.fa.grubot.objects.events.telegram.TelegramUpdateUserPhotoEvent;
import com.fa.grubot.util.Consts;
import com.github.badoualy.telegram.api.TelegramClient;

import org.json.JSONException;

import java.util.ArrayList;

public class ChatsListPresenter implements ChatsListRequestResponse {

    private ChatsListFragmentBase fragment;
    private ChatsListModel model;
    private Context context;

    private TelegramEventCallback.TelegramEventListener telegramEventListener;
    private ChatsListPresenter presenter = this;
    private TelegramClient client;

    private ArrayList<Chat> chats = new ArrayList<>();

    public ChatsListPresenter(ChatsListFragmentBase fragment, Context context) {
        this.fragment = fragment;
        this.model = new ChatsListModel();
        this.context = context;
    }

    public void notifyFragmentStarted() throws JSONException {
        fragment.setupToolbar();
        if (App.INSTANCE.getCurrentUser().hasTelegramUser())
            model.sendChatsListRequest(context, presenter);

        if (App.INSTANCE.getCurrentUser().hasVkUser())
            model.sendVkChatListRequest(this);
    }

    private void notifyViewCreated(int state) {
        fragment.showRequiredViews();

        switch (state) {
            case Consts.STATE_CONTENT:
                fragment.setupRecyclerView(chats);
                break;
            case Consts.STATE_NO_INTERNET_CONNECTION:
                fragment.setupRetryButton();
                break;
            case Consts.STATE_NO_DATA:
                break;
        }
    }

    public void onChatsListResult(ArrayList<Chat> chats, boolean moveToTop) {
        this.chats = chats;

        if (fragment != null) {
            if (chats.isEmpty()) {
                fragment.setupLayouts(true, false);
                notifyViewCreated(Consts.STATE_NO_DATA);
                this.chats = chats;
            } else if (!fragment.isAdapterExists() || fragment.isListEmpty()) {
                this.chats = chats;
                fragment.setupLayouts(true, true);
                notifyViewCreated(Consts.STATE_CONTENT);
            } else if (fragment.isAdapterExists()) {
                fragment.updateChatsList(chats, moveToTop);
            }

            if (client == null || client.isClosed())
                setUpdateCallback();
        }
    }

    public void setUpdateCallback() {
        AsyncTask.execute(() -> {
            telegramEventListener = new TelegramEventCallback.TelegramEventListener() {
                @Override
                public void onMessage(TelegramMessageEvent telegramMessageEvent) {
                    ((AppCompatActivity) context).runOnUiThread(() -> onChatsListResult(model.onNewMessage(chats, telegramMessageEvent), true));
                }

                @Override
                public void onUserNameUpdate(TelegramUpdateUserNameEvent telegramUpdateUserNameEvent) {
                    ((AppCompatActivity) context).runOnUiThread(() -> onChatsListResult(model.onUserNameUpdate(chats, telegramUpdateUserNameEvent), false));
                }

                @Override
                public void onUserPhotoUpdate(TelegramUpdateUserPhotoEvent telegramUpdateUserPhotoEvent) {
                    ((AppCompatActivity) context).runOnUiThread(() -> onChatsListResult(model.onUserPhotoUpdate(chats, telegramUpdateUserPhotoEvent), false));
                }
            };
            client = App.INSTANCE.getNewTelegramClient(new TelegramEventCallback(telegramEventListener, context));
        });
    }

    public void onRetryBtnClick() {
        model.sendChatsListRequest(context, presenter);
    }

    public void destroy() {
        fragment = null;
        model = null;
    }
}
