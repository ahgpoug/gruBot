package com.fa.grubot.callbacks;

import android.content.Context;
import android.util.Log;

import com.fa.grubot.App;
import com.fa.grubot.helpers.TelegramHelper;
import com.fa.grubot.objects.events.telegram.TelegramMessageEvent;
import com.fa.grubot.objects.events.telegram.TelegramUpdateUserNameEvent;
import com.fa.grubot.objects.events.telegram.TelegramUpdateUserPhotoEvent;
import com.fa.grubot.objects.misc.TelegramPhoto;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.api.UpdateCallback;
import com.github.badoualy.telegram.api.utils.InputFileLocation;
import com.github.badoualy.telegram.api.utils.TLMediaUtilsKt;
import com.github.badoualy.telegram.tl.api.TLAbsFileLocation;
import com.github.badoualy.telegram.tl.api.TLAbsMessage;
import com.github.badoualy.telegram.tl.api.TLAbsUpdate;
import com.github.badoualy.telegram.tl.api.TLAbsUserProfilePhoto;
import com.github.badoualy.telegram.tl.api.TLChat;
import com.github.badoualy.telegram.tl.api.TLMessage;
import com.github.badoualy.telegram.tl.api.TLPeerChannel;
import com.github.badoualy.telegram.tl.api.TLPeerChat;
import com.github.badoualy.telegram.tl.api.TLPeerUser;
import com.github.badoualy.telegram.tl.api.TLUpdateNewChannelMessage;
import com.github.badoualy.telegram.tl.api.TLUpdateNewMessage;
import com.github.badoualy.telegram.tl.api.TLUpdateShort;
import com.github.badoualy.telegram.tl.api.TLUpdateShortChatMessage;
import com.github.badoualy.telegram.tl.api.TLUpdateShortMessage;
import com.github.badoualy.telegram.tl.api.TLUpdateShortSentMessage;
import com.github.badoualy.telegram.tl.api.TLUpdateUserName;
import com.github.badoualy.telegram.tl.api.TLUpdateUserPhoto;
import com.github.badoualy.telegram.tl.api.TLUpdates;
import com.github.badoualy.telegram.tl.api.TLUpdatesCombined;
import com.github.badoualy.telegram.tl.api.TLUser;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelegramEventCallback implements UpdateCallback {

    private static final Logger LOG = LoggerFactory.getLogger(TelegramEventCallback.class);
    private TelegramEventListener listener;
    private Context context;

    public TelegramEventCallback(TelegramEventListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    @Override
    public void onUpdates(@NotNull TelegramClient telegramClient, @NotNull TLUpdates tlUpdates) {
        Log.d("debug","TLUpdates called");

        if (tlUpdates.getUpdates().isEmpty())
            Log.d("debug","Update was empty");


        tlUpdates.getUpdates().forEach(absUpdate -> this.processUpdate(absUpdate, telegramClient));
        tlUpdates.getChats().forEach(tlAbsChat -> {
            if (tlAbsChat instanceof TLChat) {
                LOG.info("Chats title was {} ", ((TLChat) tlAbsChat).getTitle());
            }
        });

        tlUpdates.getUsers().forEach(tlAbsUser -> {
            if (tlAbsUser instanceof TLUser) {
                if (((TLUser) tlAbsUser).getSelf())
                    LOG.info("Users was username: {}, firstname: {}", ((TLUser) tlAbsUser).getUsername(), ((TLUser) tlAbsUser).getFirstName());
            }
        });
    }

    @Override
    public void onUpdatesCombined(@NotNull TelegramClient telegramClient, @NotNull TLUpdatesCombined tlUpdatesCombined) {
        Log.d("debug","TLUpdatesCombined called");
    }

    @Override
    public void onUpdateShort(@NotNull TelegramClient telegramClient, @NotNull TLUpdateShort tlUpdateShort) {
        Log.d("debug","TLUpdateShort called");
        processUpdate(tlUpdateShort.getUpdate(), telegramClient);
    }

    @Override
    public void onShortChatMessage(@NotNull TelegramClient client, @NotNull TLUpdateShortChatMessage shortChatMessage) {
        Log.d("debug", "TLUpdateShortChatMessage called");
        String fromName = null;

        if (shortChatMessage.getFromId() != App.INSTANCE.getCurrentUser().getTelegramUser().getId()) {
            try {
                TLUser user = TelegramHelper.Users.getUser(client, shortChatMessage.getFromId()).getUser().getAsUser();
                fromName = user.getFirstName();
                fromName = fromName.replace("null", "").trim();
            } catch (Exception e) {
                Log.e("TAG", "Is not a user");
            }
        } else {
            fromName = "Вы";
        }

        TelegramMessageEvent event = new TelegramMessageEvent(shortChatMessage.getId(),
                shortChatMessage.getMessage(),
                shortChatMessage.getFromId(),
                shortChatMessage.getChatId(),
                ((long) shortChatMessage.getDate()) * 1000,
                fromName);

        if (listener != null)
            listener.onMessage(event);
    }

    @Override
    public void onShortMessage(@NotNull TelegramClient client, @NotNull TLUpdateShortMessage shortMessage) {
        Log.d("debug", "TLUpdateShortMessage called");
        String fromName = null;
        int fromId;

        if (shortMessage.getOut()) {
            fromName = "Вы";
            fromId = App.INSTANCE.getCurrentUser().getTelegramUser().getId();
        } else {
            fromId = shortMessage.getUserId();
        }

        TelegramMessageEvent event = new TelegramMessageEvent(shortMessage.getId(),
                shortMessage.getMessage(),
                fromId,
                shortMessage.getUserId(),
                ((long) shortMessage.getDate()) * 1000,
                fromName);

        if (listener != null)
            listener.onMessage(event);
    }

    @Override
    public void onShortSentMessage(@NotNull TelegramClient telegramClient, @NotNull TLUpdateShortSentMessage shortSentMessage) {
        Log.d("debug", "TLUpdateShortSentMessage called");
    }

    @Override
    public void onUpdateTooLong(@NotNull TelegramClient telegramClient) {
        Log.d("debug","UpdateTooLong called");
    }

    private void processUpdate(TLAbsUpdate update, TelegramClient client) {
        if (update instanceof TLUpdateNewMessage) {
            TLAbsMessage message = ((TLUpdateNewMessage) update).getMessage();
            if (message instanceof TLMessage) {
                TLMessage tlMessage = (TLMessage) message;

                int messageToId = -1;
                String fromName = null;

                if (tlMessage.getToId() instanceof TLPeerUser) {
                    TLPeerUser peerUser = (TLPeerUser) tlMessage.getToId();
                    messageToId = peerUser.getUserId();
                } else if (tlMessage.getToId() instanceof TLPeerChat) {
                    TLPeerChat peerChat = (TLPeerChat) tlMessage.getToId();
                    messageToId = peerChat.getChatId();
                } else if (tlMessage.getToId() instanceof TLPeerChannel) {
                    TLPeerChannel peerChat = (TLPeerChannel) tlMessage.getToId();
                    messageToId = peerChat.getChannelId();
                }

                if (tlMessage.getFromId() != App.INSTANCE.getCurrentUser().getTelegramUser().getId()) {
                    try {
                        TLUser user = TelegramHelper.Users.getUser(client, tlMessage.getFromId()).getUser().getAsUser();
                        fromName = user.getFirstName();
                        fromName = fromName.replace("null", "").trim();
                    } catch (Exception e) {
                        Log.e("TAG", "Is not a user");
                    }
                } else {
                    fromName = "Вы";
                }

                String messageText;
                if (tlMessage.getMedia() != null && tlMessage.getMessage().isEmpty())
                    messageText = TelegramHelper.Chats.extractMediaType(tlMessage.getMedia());
                else
                    messageText = tlMessage.getMessage();

                TelegramMessageEvent event = new TelegramMessageEvent(tlMessage.getId(), messageText, tlMessage.getFromId(), messageToId, ((long) tlMessage.getDate()) * 1000, fromName);
                if (listener != null)
                    listener.onMessage(event);
            }
        } else if (update instanceof TLUpdateNewChannelMessage) {
            TLAbsMessage message = ((TLUpdateNewChannelMessage) update).getMessage();
            if (message instanceof TLMessage) {
                TLMessage tlMessage = (TLMessage) message;

                int messageToId = -1;
                String fromName = null;

                if (tlMessage.getToId() instanceof TLPeerUser) {
                    TLPeerUser peerUser = (TLPeerUser) tlMessage.getToId();
                    messageToId = peerUser.getUserId();
                } else if (tlMessage.getToId() instanceof TLPeerChat) {
                    TLPeerChat peerChat = (TLPeerChat) tlMessage.getToId();
                    messageToId = peerChat.getChatId();
                } else if (tlMessage.getToId() instanceof TLPeerChannel) {
                    TLPeerChannel peerChat = (TLPeerChannel) tlMessage.getToId();
                    messageToId = peerChat.getChannelId();
                }


                if (tlMessage.getFromId() != App.INSTANCE.getCurrentUser().getTelegramUser().getId()) {
                    try {
                        TLUser user = TelegramHelper.Users.getUser(client, tlMessage.getFromId()).getUser().getAsUser();
                        fromName = user.getFirstName();
                        fromName = fromName.replace("null", "").trim();

                        if (fromName.isEmpty())
                            fromName = user.getUsername();
                    } catch (Exception e) {
                        Log.e("debug", "Is not a user");
                    }
                } else {
                    fromName = "Вы";
                }

                String messageText;
                if (tlMessage.getMedia() != null && tlMessage.getMessage().isEmpty())
                    messageText = TelegramHelper.Chats.extractMediaType(tlMessage.getMedia());
                else
                    messageText = tlMessage.getMessage();

                TelegramMessageEvent event = new TelegramMessageEvent(tlMessage.getId(), messageText, tlMessage.getFromId(), messageToId,((long) tlMessage.getDate()) * 1000, fromName);

                if (listener != null)
                    listener.onMessage(event);
            }
        } else if (update instanceof TLUpdateUserName) {
            TLUpdateUserName updateUserName = (TLUpdateUserName) update;
            TelegramUpdateUserNameEvent event = new TelegramUpdateUserNameEvent(updateUserName.getUserId(),
                    updateUserName.getFirstName(),
                    updateUserName.getLastName(),
                    updateUserName.getUsername());

            if (listener != null)
                listener.onUserNameUpdate(event);
        } else if (update instanceof TLUpdateUserPhoto) {
            TLUpdateUserPhoto updateUserPhoto = (TLUpdateUserPhoto) update;

            TLAbsUserProfilePhoto absPhoto = updateUserPhoto.getPhoto();
            InputFileLocation inputFileLocation = null;
            long photoId = 0;

            if (absPhoto != null) {
                TLAbsFileLocation fileLocation = absPhoto.getAsUserProfilePhoto().getPhotoBig();
                inputFileLocation = TLMediaUtilsKt.toInputFileLocation(fileLocation);
                photoId = absPhoto.getAsUserProfilePhoto().getPhotoId();
            }

            TelegramPhoto telegramPhoto = new TelegramPhoto(inputFileLocation, photoId);
            String imgUri = TelegramHelper.Files.getImgById(client, telegramPhoto, context);

            TelegramUpdateUserPhotoEvent event = new TelegramUpdateUserPhotoEvent(updateUserPhoto.getUserId(), imgUri);

            if (listener != null)
                listener.onUserPhotoUpdate(event);
        }
        //TODO MOAR EVENTS11111
    }

    public interface TelegramEventListener {
        void onMessage(TelegramMessageEvent telegramMessageEvent);

        void onUserNameUpdate(TelegramUpdateUserNameEvent telegramUpdateUserNameEvent);

        void onUserPhotoUpdate(TelegramUpdateUserPhotoEvent telegramUpdateUserPhotoEvent);
    }
}