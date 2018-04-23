package com.fa.grubot.objects;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.fa.grubot.helpers.VkDialogParser;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.chat.ChatMessage;
import com.fa.grubot.objects.users.User;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class VkMessenger extends Messenger {
    private File vkAccessTokenFile;

    private VKAccessTokenTracker vkAccessTokenTracker;

    public VkMessenger(Context context) {
        super(context);

        this.vkAccessTokenFile = new File(context.getApplicationContext().getFilesDir(), "vk.token");

        this.vkAccessTokenTracker = new VKAccessTokenTracker() {
            @Override
            public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
                if (newToken == null) {
                    Log.d("TokenTracker", "token has been destroyed");
                } else {
                    Log.d("TokenTracker", "token just right");
                }
            }
        };

        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(context.getApplicationContext());
    }

    @Override
    public Observable<Boolean> checkUserAuthObs() {
        return Observable.create(observableVkAuth -> {
            boolean isHasAuth = false;
            if (VKSdk.isLoggedIn() && VKAccessToken.tokenFromFile(vkAccessTokenFile.getPath()) != null) {
                isHasAuth = true;
            }

            observableVkAuth.onNext(isHasAuth);
        });
    }

    @Override
    public Observable<List<Chat>> getChatsListObs() {
        return Observable.create(observableVkMessages -> {
            Log.d("VK DIALOGS", "trying to get list of dialogs... ");
            VKRequest request = VKApi.messages()
                    .getDialogs(VKParameters.from(VKApiConst.COUNT, 20));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @SuppressLint("CheckResult")
                @Override
                public void onComplete(VKResponse response) {
                    Log.d("VK DIALOGS", "dialogs successfully received");
                    VkDialogParser parser = new VkDialogParser(response);
                    List<Chat> dialogs = new ArrayList<>();
                    parser.getDialogsSubscription()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    dialogs::add,
                                    error -> {
                                        error.printStackTrace();
                                        observableVkMessages.onError(error);
                                    },
                                    () -> observableVkMessages.onNext(dialogs)

                            );
                }

                @Override
                public void onError(VKError error) {
                    Log.d("VK DIALOGS", "dialogs not received with error: " + error.toString());
                    observableVkMessages.onError(error.httpError);
                }
            });
        });
    }

    @Override
    public void sendChatMessage() {

    }

    @Override
    public ArrayList<ChatMessage> getChatMessagesListObs(int start, int end) {
        return null;
    }

    @Override
    public User getUser(int userId) {
        return null;
    }
}
