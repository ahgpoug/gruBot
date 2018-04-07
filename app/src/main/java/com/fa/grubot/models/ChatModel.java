package com.fa.grubot.models;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.SparseArray;

import com.fa.grubot.App;
import com.fa.grubot.abstractions.ChatMessageSendRequestResponse;
import com.fa.grubot.abstractions.MessagesListRequestResponse;
import com.fa.grubot.helpers.TelegramHelper;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.chat.ChatMessage;
import com.fa.grubot.objects.misc.CombinedMessagesListObject;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.presenters.ChatPresenter;
import com.fa.grubot.util.Consts;
import com.fa.grubot.util.Globals;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.tl.api.TLAbsInputPeer;
import com.github.badoualy.telegram.tl.api.TLAbsMessage;
import com.github.badoualy.telegram.tl.api.TLAbsUpdate;
import com.github.badoualy.telegram.tl.api.TLAbsUpdates;
import com.github.badoualy.telegram.tl.api.TLMessage;
import com.github.badoualy.telegram.tl.api.TLPeerChannel;
import com.github.badoualy.telegram.tl.api.TLUpdateNewChannelMessage;
import com.github.badoualy.telegram.tl.api.TLUpdateNewMessage;
import com.github.badoualy.telegram.tl.api.TLUpdateShortSentMessage;
import com.github.badoualy.telegram.tl.api.TLUpdates;
import com.github.badoualy.telegram.tl.api.messages.TLAbsMessages;
import com.github.badoualy.telegram.tl.exception.RpcErrorException;

import java.lang.ref.WeakReference;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Random;

public class ChatModel {

    public ChatModel() {
    }

    public void sendTelegramMessagesRequest(Context context, ChatPresenter presenter, Chat chat, int flag, int totalMessages, SparseArray<User> users) {
        GetMessagesList request = new GetMessagesList(context, chat, flag, totalMessages, users);
        request.response = presenter;
        request.execute();
    }

    public void sendMessage(Context context, Chat chat, ChatPresenter presenter, String message) {
        SendMessage request = new SendMessage(context, chat, message);
        request.response = presenter;
        request.execute();
    }

    public static class SendMessage extends AsyncTask<Void, Void, Object> {
        private WeakReference<Context> context;
        private String message;
        private Chat chat;
        private ChatMessageSendRequestResponse response = null;

        private SendMessage(Context context, Chat chat, String message) {
            this.context = new WeakReference<>(context);
            this.message = message;
            this.chat = chat;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Void... params) {
            TelegramClient client = App.INSTANCE.getNewDownloaderClient().getDownloaderClient();
            Object returnObject;

            try {
                TLAbsUpdates tlAbsUpdates = client.messagesSendMessage(chat.getInputPeer(), message, Math.abs(new Random().nextLong()));

                User user;
                if (App.INSTANCE.getCurrentUser().getTelegramChatUser() != null)
                    user = App.INSTANCE.getCurrentUser().getTelegramChatUser();
                else
                    user = TelegramHelper.Chats.getChatUser(client, App.INSTANCE.getCurrentUser().getTelegramUser().getId(), context.get());

                String messageId = null;
                Date messageDate = null;

                if (tlAbsUpdates instanceof TLUpdates) {
                    TLUpdates tlUpdates = (TLUpdates) tlAbsUpdates;

                    for (TLAbsUpdate absUpdate : tlUpdates.getUpdates()) {
                        if (absUpdate instanceof TLUpdateNewMessage) {
                            TLAbsMessage message = ((TLUpdateNewMessage) absUpdate).getMessage();
                            if (message instanceof TLMessage) {
                                TLMessage tlMessage = (TLMessage) message;
                                messageId = String.valueOf(tlMessage.getId());
                                messageDate = new Date(((long) tlMessage.getDate()) * 1000);
                            }
                        } else if (absUpdate instanceof TLUpdateNewChannelMessage) {
                            TLAbsMessage message = ((TLUpdateNewChannelMessage) absUpdate).getMessage();
                            if (message instanceof TLMessage) {
                                TLMessage tlMessage = (TLMessage) message;
                                messageId = String.valueOf(tlMessage.getId());
                                messageDate = new Date(((long) tlMessage.getDate()) * 1000);
                            }
                        }
                    }
                } else if (tlAbsUpdates instanceof TLUpdateShortSentMessage) {
                    TLUpdateShortSentMessage updateMessageSent = (TLUpdateShortSentMessage) tlAbsUpdates;

                    messageId = String.valueOf(updateMessageSent.getId());
                    messageDate = new Date(((long) updateMessageSent.getDate()) * 1000);
                }

                if (messageId != null && messageDate != null)
                    returnObject = new ChatMessage(messageId, message, user, messageDate);
                else
                    returnObject = null;
            } catch (Exception e) {
                e.printStackTrace();
                returnObject = e;
            } finally {
                client.close(false);
            }
            return returnObject;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(Object result) {
            if (response != null && result != null && result instanceof ChatMessage)
                response.onMessageSent((ChatMessage) result);
        }
    }

    private static SparseArray<User> mergeUsersArrays(SparseArray<User> mergeTo, SparseArray<User> mergeFrom) {
        for (int i = 0; i < mergeFrom.size(); i++)
            mergeTo.put(mergeFrom.keyAt(i), mergeFrom.valueAt(i));

        return mergeTo;
    }

    public static class GetMessagesList extends AsyncTask<Void, Void, Object> {
        private WeakReference<Context> context;
        private Chat chat;
        private int totalMessages;
        private int flag;
        private SparseArray<User> users;
        private MessagesListRequestResponse response = null;

        private GetMessagesList(Context context, Chat chat, int flag, int totalMessages, SparseArray<User> users) {
            this.context = new WeakReference<>(context);
            this.chat = chat;
            this.flag = flag;
            this.totalMessages = totalMessages;
            this.users = users;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Void... params) {
            TelegramClient client = App.INSTANCE.getNewDownloaderClient();
            Object returnObject;

            try {
                ArrayList<ChatMessage> messages = new ArrayList<>();
                TLAbsInputPeer inputPeer = chat.getInputPeer();
                TLAbsMessages tlAbsMessages = client.messagesGetHistory(inputPeer, 0, 0, totalMessages, Consts.defaultMessagesCountToLoad, 0, 0);

                if (users == null || users.size() == 0)
                    users = mergeUsersArrays(users, TelegramHelper.Chats.getChatUsers(client, tlAbsMessages, context.get()));

                tlAbsMessages.getMessages().forEach(message -> {
                    if (message instanceof TLMessage) {
                        TLMessage tlMessage = (TLMessage) message;

                        User user;
                        try {
                            int userId = tlMessage.getFromId();
                            if (users.get(userId) == null)
                                users = mergeUsersArrays(users, TelegramHelper.Chats.getChatUsers(client, tlAbsMessages, context.get()));

                            user = users.get(tlMessage.getFromId());
                        } catch (Exception e) {
                            int chatId = ((TLPeerChannel) tlMessage.getToId()).getChannelId();
                            if (users.get(chatId) == null)
                                users = mergeUsersArrays(users, TelegramHelper.Chats.getChatUsers(client, tlAbsMessages, context.get()));

                            user = users.get(chatId);
                        }

                        ChatMessage chatMessage = new ChatMessage(String.valueOf(tlMessage.getId()),
                                tlMessage.getMessage(),
                                user,
                                new Date(((long) tlMessage.getDate()) * 1000));

                        messages.add(chatMessage);
                    } else
                        System.out.println("Service message");
                });
                returnObject = new CombinedMessagesListObject(messages, users);
            } catch (Exception e) {
                e.printStackTrace();
                returnObject = e;
            } finally {
                client.close(false);
            }

            return returnObject;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onPostExecute(Object result) {
            if (response != null && result != null && result instanceof CombinedMessagesListObject)
                response.onMessagesListResult((CombinedMessagesListObject) result, flag, false);
            else if (response != null && result instanceof RpcErrorException && ((RpcErrorException) result).getCode() == 420) {
                int floodTime = Globals.extractMillisFromRpcException((RpcErrorException) result);
                (new Handler()).postDelayed(() -> {
                    response.retryLoad(chat, flag, totalMessages, users);
                }, floodTime + 500);
            }
        }
    }
}
