package com.fa.grubot.models;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.fa.grubot.App;
import com.fa.grubot.helpers.TelegramHelper;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.users.User;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.tl.api.TLAbsChannelParticipant;
import com.github.badoualy.telegram.tl.api.TLAbsChatFull;
import com.github.badoualy.telegram.tl.api.TLAbsInputPeer;
import com.github.badoualy.telegram.tl.api.TLChannelFull;
import com.github.badoualy.telegram.tl.api.TLInputChannel;
import com.github.badoualy.telegram.tl.api.TLInputPeerChannel;
import com.github.badoualy.telegram.tl.api.TLInputPeerChat;
import com.github.badoualy.telegram.tl.api.messages.TLAbsMessages;
import com.github.badoualy.telegram.tl.api.request.TLRequestChannelsGetFullChannel;

import java.util.Random;

public class GroupInfoModel {
    public GroupInfoModel() {

    }

    @SuppressWarnings("unchecked")
    public Object getChatParticipants(Chat chat, Context context) {
        Object returnObject = 0;

        TelegramClient client = App.INSTANCE.getNewDownloaderClient();
        int chatId = Integer.valueOf(chat.getId());

        try {
            TLAbsInputPeer inputPeer = chat.getInputPeer();

            if (inputPeer instanceof TLInputPeerChannel) {
                TLInputPeerChannel inputPeerChannel = (TLInputPeerChannel) chat.getInputPeer();
                TLInputChannel inputChannel = new TLInputChannel(inputPeerChannel.getChannelId(), inputPeerChannel.getAccessHash());

                TLRequestChannelsGetFullChannel tlRequestChannelsGetFullChannel = new TLRequestChannelsGetFullChannel(inputChannel);
                TLAbsChatFull tlAbsChatFull = client.executeRpcQuery(tlRequestChannelsGetFullChannel).getFullChat();

                if (tlAbsChatFull instanceof TLChannelFull) {
                    TLChannelFull tlChannelFull = (TLChannelFull) tlAbsChatFull;

                    if (tlChannelFull.getCanViewParticipants()) {
                        TLAbsMessages tlAbsMessages = client.messagesGetHistory(chat.getInputPeer(), 0, 0, 0, 1000000000, 0, 0);
                        SparseArray<User> users = TelegramHelper.Chats.getChatUsers(client, tlAbsMessages, context);
                        for (int i = 0; i < users.size(); i++) {
                            User user = users.valueAt(i);
                            try {
                                TLAbsChannelParticipant channelParticipant = client.channelsGetParticipant(inputChannel, user.getInputUser()).getParticipant();
                                user.setChatRole(TelegramHelper.Chats.extractParticipantRole(channelParticipant));
                            } catch (Exception e) {
                                Log.d("debug", e.getMessage());
                            }
                        }

                        returnObject = users;
                    } else {
                        returnObject = tlChannelFull.getParticipantsCount();
                    }
                }
            } else if (inputPeer instanceof TLInputPeerChat) {
                TLAbsMessages tlAbsMessages = client.messagesGetHistory(chat.getInputPeer(), 0, 0, 0, 1000000000, 0, 0);
                returnObject = TelegramHelper.Chats.getChatUsers(client, tlAbsMessages, context);
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnObject = e;
        }

        if (returnObject instanceof SparseArray<?>) {
            SparseArray<User> users = (SparseArray<User>) returnObject;
            users.remove(chatId);
        }

        return returnObject;
    }

    public boolean sendMessage(Chat chat, String message) {
        boolean isSent = false;
        TelegramClient client = App.INSTANCE.getNewDownloaderClient().getDownloaderClient();
        try {
            client.messagesSendMessage(chat.getInputPeer(), message, Math.abs(new Random().nextLong()));
            isSent = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close(false);
        }
        return isSent;
    }
}
