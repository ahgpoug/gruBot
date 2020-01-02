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
import com.github.badoualy.telegram.tl.api.TLInputPeerUser;
import com.github.badoualy.telegram.tl.api.TLInputUser;
import com.github.badoualy.telegram.tl.api.TLMessage;
import com.github.badoualy.telegram.tl.api.TLPeerChannel;
import com.github.badoualy.telegram.tl.api.TLUpdateNewChannelMessage;
import com.github.badoualy.telegram.tl.api.TLUpdateNewMessage;
import com.github.badoualy.telegram.tl.api.TLUpdateShortSentMessage;
import com.github.badoualy.telegram.tl.api.TLUpdates;
import com.github.badoualy.telegram.tl.api.messages.TLAbsMessages;
import com.github.badoualy.telegram.tl.exception.RpcErrorException;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.methods.VKApiMessages;
import com.vk.sdk.api.methods.VKApiUsers;
import com.vk.sdk.api.model.VKApiGetMessagesResponse;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKApiUserFull;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChatModel {

    private String oldestLoadedMessage;

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

    public void sendVkMessage(Context context, Chat chat, ChatPresenter presenter, String message) {
        String peerId;
        if (chat.getChatId() != null){
            //peerId = String.valueOf(2000000000l + Long.valueOf(chat.getChatId()));
            peerId = chat.getChatId();
        } else {
            peerId = chat.getId();
        }

        VKRequest request;
        if (chat.getChatId() == null) {
            request = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, peerId, VKApiConst.MESSAGE, message));
        } else {
            request = new VKRequest("messages.send", VKParameters.from("chat_id", peerId, VKApiConst.MESSAGE, message));
        }
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });

    }

    public void sendVkMessagesRequest(ChatPresenter presenter, Chat chat, int flag, int page, SparseArray<User> users) {

        String peerId;
        if (chat.getChatId() != null){
            peerId = String.valueOf(2000000000l + Long.valueOf(chat.getChatId()));
        } else {
            peerId = chat.getId();
        }


        VKRequest request;
        if (flag == Consts.FLAG_LOAD_FIRST_MESSAGES) {
            request = new VKRequest("messages.getHistory",
                    VKParameters.from(VKApiConst.USER_ID, peerId));
        } else {
            request = new VKRequest("messages.getHistory",
                    VKParameters.from(VKApiConst.USER_ID, peerId, "offset",
                            page * 20));
            oldestLoadedMessage = String.valueOf(Integer.valueOf(oldestLoadedMessage) - 20);
        }

        request.executeWithListener(new VKRequest.VKRequestListener() {

            @Override
            public void onComplete(VKResponse response) {
                VKApiGetMessagesResponse messages = new VKApiGetMessagesResponse(response.json);
                SparseArray<User> users = new SparseArray<>();
                ArrayList<ChatMessage> msgs = new ArrayList<>();

                for (VKApiMessage msg: messages.items) {
                    users.put(msg.out ? Integer.valueOf(VKAccessToken.currentToken().userId)  : msg.user_id, new User(msg.out ? VKAccessToken.currentToken().userId  : String.valueOf(msg.user_id), Consts.VK, null, null, null));
                    msgs.add(new ChatMessage(String.valueOf(msg.getId()), msg.body, new User(msg.out ? VKAccessToken.currentToken().userId  : String.valueOf(msg.user_id), Consts.VK, null, null, null),
                            new Date(msg.date * 1000)));
                }

                if (flag == Consts.FLAG_LOAD_NEW_MESSAGES){
                    Collections.reverse(msgs);
                }

                CombinedMessagesListObject cmlo = new CombinedMessagesListObject(msgs, users);

                enrichUsers(users);

                for (ChatMessage msg : msgs) {
                    User usr = (User) msg.getUser();
                    usr.setImgUrl(users.get(Integer.valueOf(usr.getId())).getAvatar());
                    usr.setFullname(users.get(Integer.valueOf(usr.getId())).getFullname());
                    usr.setUserName(users.get(Integer.valueOf(usr.getId())).getFullname());
                }

                oldestLoadedMessage = msgs.get(0).getId();
                presenter.onMessagesListResult(cmlo, flag, false);

            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });

    }

    private void enrichUsers(SparseArray<User> users){
        String ids = "";

        for (int i = 0; i < users.size(); i++) {
            ids += users.get(users.keyAt(i)).getId() + ", ";
        }

        VKRequest request = new VKRequest("users.get", VKParameters.from(VKApiConst.USER_IDS, ids, VKApiConst.FIELDS, "photo_100"));
        request.executeSyncWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                List<VKApiUserFull> usersParsed = new ArrayList<>();
                try {
                    for (int i = 0; i < response.json.getJSONArray("response").length(); i++) {
                        VKApiUserFull user = new VKApiUserFull(response.json.getJSONArray("response").getJSONObject(i));
                        usersParsed.add(user);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < usersParsed.size(); i++) {
                    users.get(usersParsed.get(i).getId()).setImgUrl(usersParsed.get(i).photo_100)
                            .setFullname(usersParsed.get(i).first_name + " " + usersParsed.get(i).last_name)
                            .setUserName(usersParsed.get(i).first_name + " " + usersParsed.get(i).last_name);
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });
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
