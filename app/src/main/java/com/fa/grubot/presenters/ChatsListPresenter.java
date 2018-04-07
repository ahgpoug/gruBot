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
import com.fa.grubot.util.Globals;
import com.github.badoualy.telegram.api.TelegramClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.fa.grubot.util.Consts.STATE_NO_INTERNET_CONNECTION;

public class ChatsListPresenter implements ChatsListRequestResponse {

    private ChatsListFragmentBase fragment;
    private ChatsListModel model;
    private Context context;

    private TelegramEventCallback.TelegramEventListener telegramEventListener;
    private ChatsListPresenter presenter = this;
    private TelegramClient client;

    private ArrayList<Chat> chats = new ArrayList<>();

    private Observable<List<Chat>> vkListObservable;

    private Observable<List<Chat>> tListObservable;

    private boolean callbackInited = false;

    public ChatsListPresenter(ChatsListFragmentBase fragment, Context context) {
        this.fragment = fragment;
        this.model = new ChatsListModel();
        this.context = context;
    }

    public void notifyFragmentStarted() {
        fragment.setupToolbar();

        if (Globals.InternetMethods.isNetworkAvailable(context)) {
            if (App.INSTANCE.getCurrentUser().hasTelegramUser())
                model.sendChatsListRequest(context, presenter);

            if (App.INSTANCE.getCurrentUser().hasVkUser())
                model.sendVkChatListRequest(this);
        } else {
            notifyViewCreated(STATE_NO_INTERNET_CONNECTION);
        }

        if (App.INSTANCE.getCurrentUser().hasTelegramUser()) {
            tListObservable = model.sendChatsListRequest(context, presenter);
        } else {
            tListObservable = Observable.just(new ArrayList<>());
        }

        if (App.INSTANCE.getCurrentUser().hasVkUser()) {
            vkListObservable = model.sendVkChatListRequest(this);
        } else {
            vkListObservable = Observable.just(new ArrayList<>());
        }

        Observable.combineLatest(vkListObservable, tListObservable, (v, t) -> {
            List<Chat> megaChat = new ArrayList<>();
            if (v != null) megaChat.addAll(v);
            if (t != null) megaChat.addAll(t);
            Collections.sort(megaChat);
            return megaChat;
        })
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(combinedChat -> onChatsListResult(new ArrayList<>(combinedChat), false));
    }

    private void notifyViewCreated(int state) {
        fragment.showRequiredViews();

        switch (state) {
            case Consts.STATE_CONTENT:
                fragment.setupRecyclerView(chats);
                break;
            case STATE_NO_INTERNET_CONNECTION:
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
        if (!callbackInited) {
            callbackInited = true;
            AsyncTask.execute(() -> {
                try {
                    Thread.sleep(3000); //todo dirty little hack
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (Globals.InternetMethods.isNetworkAvailable(context))
                    client = App.INSTANCE.getNewTelegramClient(new TelegramEventCallback(telegramEventListener, context));
                else
                    notifyViewCreated(STATE_NO_INTERNET_CONNECTION);

                telegramEventListener = new TelegramEventCallback.TelegramEventListener() {
                    @Override
                    public void onMessage(TelegramMessageEvent telegramMessageEvent) {
                        ((AppCompatActivity) context).runOnUiThread(()
                                -> onChatsListResult(model.onNewMessage(chats, telegramMessageEvent),
                                true));
                    }

                    @Override
                    public void onUserNameUpdate(TelegramUpdateUserNameEvent telegramUpdateUserNameEvent) {
                        ((AppCompatActivity) context).runOnUiThread(()
                                -> onChatsListResult(model.onUserNameUpdate(chats, telegramUpdateUserNameEvent),
                                false));
                    }

                    @Override
                    public void onUserPhotoUpdate(TelegramUpdateUserPhotoEvent telegramUpdateUserPhotoEvent) {
                        ((AppCompatActivity) context).runOnUiThread(()
                                -> onChatsListResult(model.onUserPhotoUpdate(chats, telegramUpdateUserPhotoEvent),
                                false));
                    }
                };
            });
        }
    }

    @Override
    public void onFloodException() {
        if (Globals.InternetMethods.isNetworkAvailable(context))
            model.sendChatsListRequest(context, presenter);
    }

    public void onRetryBtnClick() {
        if (Globals.InternetMethods.isNetworkAvailable(context))
            model.sendChatsListRequest(context, presenter);
    }

    public void destroy() {
        if (client != null && !client.isClosed())
            client.close(false);
        fragment = null;
        model = null;
    }
}
