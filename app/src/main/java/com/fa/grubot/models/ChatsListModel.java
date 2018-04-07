package com.fa.grubot.models;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseArray;

import com.fa.grubot.App;
import com.fa.grubot.abstractions.ChatsListRequestResponse;
import com.fa.grubot.helpers.TelegramHelper;
import com.fa.grubot.helpers.VkDialogParser;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.events.telegram.TelegramMessageEvent;
import com.fa.grubot.objects.events.telegram.TelegramUpdateUserNameEvent;
import com.fa.grubot.objects.events.telegram.TelegramUpdateUserPhotoEvent;
import com.fa.grubot.objects.misc.TelegramPhoto;
import com.fa.grubot.objects.users.CurrentUser;
import com.fa.grubot.presenters.ChatsListPresenter;
import com.fa.grubot.util.Consts;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.tl.api.TLAbsInputPeer;
import com.github.badoualy.telegram.tl.api.TLAbsMessage;
import com.github.badoualy.telegram.tl.api.TLAbsMessageAction;
import com.github.badoualy.telegram.tl.api.TLAbsPeer;
import com.github.badoualy.telegram.tl.api.TLInputPeerEmpty;
import com.github.badoualy.telegram.tl.api.TLMessage;
import com.github.badoualy.telegram.tl.api.TLMessageService;
import com.github.badoualy.telegram.tl.api.TLPeerChannel;
import com.github.badoualy.telegram.tl.api.TLPeerChat;
import com.github.badoualy.telegram.tl.api.TLPeerUser;
import com.github.badoualy.telegram.tl.api.TLUser;
import com.github.badoualy.telegram.tl.api.messages.TLAbsDialogs;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ChatsListModel {

    public Observable<List<Chat>> sendChatsListRequest(Context context, ChatsListPresenter presenter) {

        return Observable.create(observableTMessages -> {
            ArrayList<Chat> chatsList = new ArrayList<>();
            TelegramClient client = App.INSTANCE.getNewTelegramClient(null).getDownloaderClient();

            CurrentUser currentUser = App.INSTANCE.getCurrentUser();
            if (currentUser.getTelegramChatUser() == null)
                currentUser.setTelegramChatUser(TelegramHelper.Chats.getChatUser(client, currentUser.getTelegramUser().getId(), context));

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
                                if (message.getFromId() == App.INSTANCE.getCurrentUser().getTelegramUser().getId()) {
                                    fromName = "Вы";
                                } else {
                                    TLUser user = TelegramHelper.Users.getUser(client, message.getFromId()).getUser().getAsUser();
                                    fromName = user.getFirstName();
                                    fromName = fromName.replace("null", "").trim();

                                    if (fromName.isEmpty())
                                        fromName = user.getUsername();
                                }
                            }

                            if (peer instanceof TLPeerUser && message.getFromId() == App.INSTANCE.getCurrentUser().getTelegramUser().getId())
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
                observableTMessages.onNext(chatsList);
        });

    }

    public Observable<List<Chat>> sendVkChatListRequest(ChatsListPresenter presenter) {
        return Observable.create(observableVkMessages -> {
            Log.d("VK DIALOGS", "trying to get list of dialogs... ");
            VKRequest request = VKApi.messages()
                    .getDialogs(VKParameters.from(VKApiConst.COUNT, 20));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    Log.d("VK DIALOGS", "dialogs successfully received");
                    VkDialogParser parser = new VkDialogParser(response);
                    List<Chat> dialogs = new ArrayList<>();
                    parser.getDialogsSubscription()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    chat -> {
                                        dialogs.add(chat);
                                    },
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


    public static class GetChatsList extends AsyncTask<Void, Void, Object> {
        private WeakReference<Context> context;
        private ChatsListRequestResponse response = null;

        private GetChatsList(Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Void... params) {
            ArrayList<Chat> chatsList = new ArrayList<>();
            TelegramClient client = App.INSTANCE.getNewTelegramClient(null).getDownloaderClient();

            CurrentUser currentUser = App.INSTANCE.getCurrentUser();
            if (currentUser.getTelegramChatUser() == null)
                currentUser.setTelegramChatUser(TelegramHelper.Chats.getChatUser(client, currentUser.getTelegramUser().getId(), context.get()));

            try {
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
                                if (message.getFromId() == App.INSTANCE.getCurrentUser().getTelegramUser().getId()) {
                                    fromName = "Вы";
                                } else {
                                    TLUser user = TelegramHelper.Users.getUser(client, message.getFromId()).getUser().getAsUser();
                                    fromName = user.getFirstName();
                                    fromName = fromName.replace("null", "").trim();

                                    if (fromName.isEmpty())
                                        fromName = user.getUsername();
                                }
                            }

                            if (peer instanceof TLPeerUser && message.getFromId() == App.INSTANCE.getCurrentUser().getTelegramUser().getId())
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
                    String imgUri = TelegramHelper.Files.getImgById(client, telegramPhoto, context.get());
                    if (imgUri == null)
                        imgUri = chatName;

                    chat = new Chat(String.valueOf(chatId), chatName, null, imgUri, lastMessageText, Consts.Telegram, lastMessageDate * 1000, fromName);
                    chat.setInputPeer(inputPeer);
                    chatsList.add(chat);
                });
                return chatsList;
            } catch (Exception e) {
                e.printStackTrace();
                return e;
            } finally {
                client.close(false);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(Object result) {
            if (response != null && result instanceof ArrayList<?>)
                response.onChatsListResult((ArrayList<Chat>) result, true);
        }
    }

    public ArrayList<Chat> onNewMessage(ArrayList<Chat> chats, TelegramMessageEvent event) {
        Chat chatToChange = null;
        int index = -1;
        int idToFind = -1;

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
