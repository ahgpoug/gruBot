package com.fa.grubot.objects;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.fa.grubot.helpers.TelegramHelper;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.chat.ChatMessage;
import com.fa.grubot.objects.misc.TelegramPhoto;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.util.Consts;
import com.fa.grubot.util.TmApiStorage;
import com.github.badoualy.telegram.api.Kotlogram;
import com.github.badoualy.telegram.api.TelegramApp;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.api.UpdateCallback;
import com.github.badoualy.telegram.tl.api.TLAbsInputPeer;
import com.github.badoualy.telegram.tl.api.TLAbsMessage;
import com.github.badoualy.telegram.tl.api.TLAbsMessageAction;
import com.github.badoualy.telegram.tl.api.TLAbsPeer;
import com.github.badoualy.telegram.tl.api.TLInputPeerEmpty;
import com.github.badoualy.telegram.tl.api.TLInputUserSelf;
import com.github.badoualy.telegram.tl.api.TLMessage;
import com.github.badoualy.telegram.tl.api.TLMessageService;
import com.github.badoualy.telegram.tl.api.TLPeerChannel;
import com.github.badoualy.telegram.tl.api.TLPeerChat;
import com.github.badoualy.telegram.tl.api.TLPeerUser;
import com.github.badoualy.telegram.tl.api.TLUser;
import com.github.badoualy.telegram.tl.api.auth.TLAuthorization;
import com.github.badoualy.telegram.tl.api.auth.TLSentCode;
import com.github.badoualy.telegram.tl.api.messages.TLAbsDialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class TelegramMessenger extends Messenger {
    private static final int API_ID = 0;
    private static final String API_HASH = "";

    private static final String APP_VERSION = "1.0";
    private static final String MODEL = "Dev";
    private static final String SYSTEM_VERSION = "Dev";
    private static final String LANG_CODE = "en";

    private File authKeyFile;
    private File nearestDcFile;

    private TelegramApp application;

    public TelegramMessenger(Context context) {
        super(context);

        authKeyFile = new File(context.getApplicationContext().getFilesDir(), "auth.key");
        nearestDcFile = new File(context.getApplicationContext().getFilesDir(), "dc.save");

        application = new TelegramApp(API_ID, API_HASH, MODEL, SYSTEM_VERSION, APP_VERSION, LANG_CODE);
    }

    public TelegramClient getNewTelegramClient(UpdateCallback callback) {
        TelegramClient telegramClient;

        TmApiStorage apiStorage = new TmApiStorage(authKeyFile, nearestDcFile);

        if (apiStorage.loadDc() != null) {
            if (callback != null)
                telegramClient = Kotlogram.getDefaultClient(application, apiStorage, apiStorage.loadDc(), callback);
            else
                telegramClient = Kotlogram.getDefaultClient(application, apiStorage, apiStorage.loadDc());
        } else {
            telegramClient = Kotlogram.getDefaultClient(application, apiStorage);
        }

        telegramClient.setTimeout(3000);
        return telegramClient;
    }

    public TelegramClient getDownloaderClient() {
        return getNewTelegramClient(null).getDownloaderClient();
    }

    public Observable<Object> logOutObs() {
        return Observable.create(logOutObs -> {
            Object returnObject = null;
            TelegramClient client = getNewTelegramClient(null);
            try {
                returnObject = client.authLogOut();
                setCurrentUser(null);
            } catch (Exception e) {
                e.printStackTrace();
                returnObject = e;
            } finally {
                client.close(false);
                logOutObs.onNext(returnObject);
            }
        });
    }

    @Override
    public Observable<Boolean> checkUserAuthObs() {
        return Observable.create(telegramAuthObs -> {
            boolean isHasAuth = false;
            TelegramClient client = getNewTelegramClient(null);

            try {
                setCurrentUser(TelegramHelper.Users.getChatUserFromInput(client, context, new TLInputUserSelf()));
                isHasAuth = true;
            } catch (Exception e) {
                e.printStackTrace();
                isHasAuth = false;
            } finally {
                client.close(false);
                telegramAuthObs.onNext(isHasAuth);
            }
        });
    }

    @Override
    public Observable<List<Chat>> getChatsListObs() {
        return Observable.create(messagesObs -> {
            ArrayList<Chat> chatsList = new ArrayList<>();
            TelegramClient client = getNewTelegramClient(null).getDownloaderClient();

            TLAbsDialogs tlAbsDialogs = client.messagesGetDialogs(false, 0, 0, new TLInputPeerEmpty(), 10000); //have no idea how to avoid the limit without a huge number

            SparseArray<String> namesMap = TelegramHelper.Chats.getChatNamesMap(tlAbsDialogs);
            SparseArray<TelegramPhoto> photoMap = TelegramHelper.Chats.getPhotoMap(tlAbsDialogs);
            SparseArray<TLAbsMessage> messagesMap = new SparseArray<>();

            tlAbsDialogs.getMessages().forEach(message -> messagesMap.put(message.getId(), message));

            tlAbsDialogs.getDialogs().forEach(dialog -> {
                TLAbsPeer peer = dialog.getPeer();
                Chat chat;
                String lastMessageText = "";

                int chatId = TelegramHelper.Chats.getId(peer);
                long lastMessageDate = 0;
                String chatName = namesMap.get(chatId);
                String fromName = null;

                TLAbsMessage lastMessage = messagesMap.get(dialog.getTopMessage());
                TLAbsInputPeer inputPeer = TelegramHelper.Chats.getInputPeer(tlAbsDialogs, String.valueOf(chatId));

                if (lastMessage instanceof TLMessage) {
                    TLMessage message = (TLMessage) lastMessage;
                    lastMessageDate = ((TLMessage) lastMessage).getDate();

                    try {
                        if (peer instanceof TLPeerChat || peer instanceof TLPeerChannel) {
                            if (message.getFromId() == getCurrentUser().getIntId()) {
                                fromName = "Вы";
                            } else {
                                TLUser user = TelegramHelper.Users.getUser(client, message.getFromId()).getUser().getAsUser();
                                fromName = user.getFirstName();
                                fromName = fromName.replace("null", "").trim();

                                if (fromName.isEmpty())
                                    fromName = user.getUsername();
                            }
                        }

                        if (peer instanceof TLPeerUser && message.getFromId() == getCurrentUser().getIntId())
                            fromName = "Вы";
                    } catch (Exception e) {
                        Log.e("TAG", "Is not a user");
                    }

                    if (message.getMedia() != null && message.getMessage().isEmpty())
                        lastMessageText = TelegramHelper.Chats.extractMediaType(message.getMedia());
                    else
                        lastMessageText = message.getMessage();

                } else if (lastMessage instanceof TLMessageService) {
                    TLAbsMessageAction action = ((TLMessageService) lastMessage).getAction();
                    lastMessageText = TelegramHelper.Chats.extractActionType(action);
                    lastMessageDate = ((TLMessageService) lastMessage).getDate();
                }

                TelegramPhoto telegramPhoto = photoMap.get(chatId);
                String imgUri = TelegramHelper.Files.getImgById(client, telegramPhoto, context);
                if (imgUri == null)
                    imgUri = chatName;

                chat = new Chat(String.valueOf(chatId), chatName, null, imgUri, lastMessageText, Consts.Telegram, lastMessageDate * 1000, fromName);
                chat.setInputPeer(inputPeer);
                chatsList.add(chat);
            });
            client.close(false);
            messagesObs.onNext(chatsList);
        });
    }

    public Observable<Object> sendAuthCodeObs(final String phoneNumber) {
        return Observable.create(authCodeObs -> {
            Object returnObject = null;
            TelegramClient client = getNewTelegramClient(null);

            try {
                returnObject = client.authSendCode(false, phoneNumber, true);
            } catch (Exception e) {
                e.printStackTrace();
                returnObject = e;
            } finally {
                client.close(false);
                authCodeObs.onNext(returnObject);
            }
        });
    }

    public Observable<Object> verifyAuthCodeObs(final TLSentCode sentCode, final String phoneNumber, final String authCode) {
        return Observable.create(authCodeObs -> {
            Object returnObject = null;
            TelegramClient client = getNewTelegramClient(null);

            try {
                TLAuthorization authorization = client.authSignIn(phoneNumber, sentCode.getPhoneCodeHash(), authCode);
                setCurrentUser(TelegramHelper.Users.getChatUserFromInput(client, context, new TLInputUserSelf()));
                returnObject = authorization;
            } catch (Exception e) {
                e.printStackTrace();
                returnObject = e;
            } finally {
                client.close(false);
                authCodeObs.onNext(returnObject);
            }
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
