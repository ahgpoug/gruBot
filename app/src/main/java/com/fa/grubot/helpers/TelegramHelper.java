package com.fa.grubot.helpers;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;

import com.fa.grubot.App;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.misc.TelegramPhoto;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.util.Consts;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.api.utils.InputFileLocation;
import com.github.badoualy.telegram.api.utils.TLMediaUtilsKt;
import com.github.badoualy.telegram.tl.api.TLAbsChannelParticipant;
import com.github.badoualy.telegram.tl.api.TLAbsChat;
import com.github.badoualy.telegram.tl.api.TLAbsChatPhoto;
import com.github.badoualy.telegram.tl.api.TLAbsFileLocation;
import com.github.badoualy.telegram.tl.api.TLAbsInputPeer;
import com.github.badoualy.telegram.tl.api.TLAbsInputUser;
import com.github.badoualy.telegram.tl.api.TLAbsMessageAction;
import com.github.badoualy.telegram.tl.api.TLAbsMessageMedia;
import com.github.badoualy.telegram.tl.api.TLAbsPeer;
import com.github.badoualy.telegram.tl.api.TLAbsUser;
import com.github.badoualy.telegram.tl.api.TLAbsUserProfilePhoto;
import com.github.badoualy.telegram.tl.api.TLChannel;
import com.github.badoualy.telegram.tl.api.TLChannelForbidden;
import com.github.badoualy.telegram.tl.api.TLChannelParticipant;
import com.github.badoualy.telegram.tl.api.TLChannelParticipantCreator;
import com.github.badoualy.telegram.tl.api.TLChannelParticipantEditor;
import com.github.badoualy.telegram.tl.api.TLChannelParticipantKicked;
import com.github.badoualy.telegram.tl.api.TLChannelParticipantModerator;
import com.github.badoualy.telegram.tl.api.TLChannelParticipantSelf;
import com.github.badoualy.telegram.tl.api.TLChat;
import com.github.badoualy.telegram.tl.api.TLChatEmpty;
import com.github.badoualy.telegram.tl.api.TLChatForbidden;
import com.github.badoualy.telegram.tl.api.TLChatPhoto;
import com.github.badoualy.telegram.tl.api.TLInputPeerChannel;
import com.github.badoualy.telegram.tl.api.TLInputPeerChat;
import com.github.badoualy.telegram.tl.api.TLInputPeerEmpty;
import com.github.badoualy.telegram.tl.api.TLInputPeerSelf;
import com.github.badoualy.telegram.tl.api.TLInputPeerUser;
import com.github.badoualy.telegram.tl.api.TLInputUser;
import com.github.badoualy.telegram.tl.api.TLMessageActionChannelCreate;
import com.github.badoualy.telegram.tl.api.TLMessageActionChatAddUser;
import com.github.badoualy.telegram.tl.api.TLMessageActionChatCreate;
import com.github.badoualy.telegram.tl.api.TLMessageActionChatDeletePhoto;
import com.github.badoualy.telegram.tl.api.TLMessageActionChatDeleteUser;
import com.github.badoualy.telegram.tl.api.TLMessageActionChatEditPhoto;
import com.github.badoualy.telegram.tl.api.TLMessageActionChatEditTitle;
import com.github.badoualy.telegram.tl.api.TLMessageActionPinMessage;
import com.github.badoualy.telegram.tl.api.TLMessageMediaContact;
import com.github.badoualy.telegram.tl.api.TLMessageMediaDocument;
import com.github.badoualy.telegram.tl.api.TLMessageMediaGame;
import com.github.badoualy.telegram.tl.api.TLMessageMediaGeo;
import com.github.badoualy.telegram.tl.api.TLMessageMediaPhoto;
import com.github.badoualy.telegram.tl.api.TLPeerChannel;
import com.github.badoualy.telegram.tl.api.TLPeerChat;
import com.github.badoualy.telegram.tl.api.TLPeerUser;
import com.github.badoualy.telegram.tl.api.TLUser;
import com.github.badoualy.telegram.tl.api.TLUserFull;
import com.github.badoualy.telegram.tl.api.messages.TLAbsDialogs;
import com.github.badoualy.telegram.tl.api.messages.TLAbsMessages;
import com.github.badoualy.telegram.tl.api.request.TLRequestUsersGetFullUser;
import com.github.badoualy.telegram.tl.core.TLIntVector;
import com.github.badoualy.telegram.tl.core.TLVector;
import com.github.badoualy.telegram.tl.exception.RpcErrorException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import static java.lang.String.format;

public class TelegramHelper {
    public static class Files {
        public static String getImgById(TelegramClient client, TelegramPhoto photo, Context context) {
            long photoId = photo.getPhotoId();

            if (photo.getPhotoId() == 0)
                return null;

            File file = getFileById(photoId, context);

            if (file.exists())
                return Uri.fromFile(file).toString();
            else
                return Uri.fromFile(downloadFile(client, photo, context)).toString();
        }

        private static File getFileById(long id, Context context) {
            String path = format(Locale.getDefault(), "%s/%d.png", context.getExternalCacheDir(), id);
            return new File(path);
        }

        private static File downloadFile(TelegramClient client, TelegramPhoto photo, Context context) {
            InputFileLocation inputFileLocation = photo.getFileLocation();
            long photoId = photo.getPhotoId();

            File file = new File(context.getExternalCacheDir(), String.format("%s.png", photoId));
            try {
                FileOutputStream fos = new FileOutputStream(file);
                client.downloadSync(inputFileLocation, 0, fos);
                return file;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class Users {
        public static TLUserFull getUser(TelegramClient telegramClient, int userId) throws Exception {
            TLInputUser user = new TLInputUser();
            user.setUserId(userId);
            TLRequestUsersGetFullUser requestUsersGetFullUser = new TLRequestUsersGetFullUser(user);

            return telegramClient.executeRpcQuery(requestUsersGetFullUser);
        }

        public static String extractName(TLUser tlUser) {
            StringBuilder userName = new StringBuilder();
            if (tlUser.getFirstName() != null) {
                userName.append(tlUser.getFirstName());
            }
            if (tlUser.getLastName() != null) {
                userName.append(" ").append(tlUser.getLastName());
            }
            return userName.toString();
        }
    }

    public static class Chats {
        public static String getTitle(TelegramClient telegramClient, int chatId) {
            TLAbsChat tlAbsChat = getAbsChat(telegramClient, chatId);
            if (tlAbsChat != null) {
                return extractChatTitle(tlAbsChat);
            }
            return null;
        }

        public static TLAbsInputPeer getInputPeer(TLAbsDialogs tlAbsDialogs, String chatId) {
            TLVector<TLAbsUser> users = tlAbsDialogs.getUsers();
            for (TLAbsUser absUser : users) {
                if (absUser.getId() == Integer.valueOf(chatId)) {
                    TLUser user = absUser.getAsUser();
                    return (new TLInputPeerUser(user.getId(), user.getAccessHash()));
                }
            }

            TLVector<TLAbsChat> chats = tlAbsDialogs.getChats();
            for (TLAbsChat absChat : chats) {
                if (absChat.getId() == Integer.valueOf(chatId)) {
                    if (absChat instanceof TLChannel) {
                        TLChannel channel = (TLChannel) absChat;
                        return (new TLInputPeerChannel(channel.getId(), channel.getAccessHash()));
                    } else if (absChat instanceof TLChat) {
                        TLChat chat = (TLChat) absChat;
                        return (new TLInputPeerChat(chat.getId()));
                    }
                }
            }

            return new TLInputPeerEmpty();
        }

        public static String extractChatTitle(TLAbsChat tlAbsChat) {
            if (tlAbsChat instanceof TLChannel)
                return ((TLChannel) tlAbsChat).getTitle();
            else if (tlAbsChat instanceof TLChat)
                return ((TLChat) tlAbsChat).getTitle();
            else if (tlAbsChat instanceof TLChatForbidden)
                return ((TLChatForbidden) tlAbsChat).getTitle();
            else if (tlAbsChat instanceof TLChannelForbidden)
                return ((TLChannelForbidden) tlAbsChat).getTitle();
            else if (tlAbsChat instanceof TLChatEmpty)
                return null;

            return null;
        }

        public static TLAbsChatPhoto extractChatPhoto(TLAbsChat tlAbsChat) {
            if (tlAbsChat instanceof TLChannel)
                return ((TLChannel) tlAbsChat).getPhoto();
            else if (tlAbsChat instanceof TLChat)
                return ((TLChat) tlAbsChat).getPhoto();
            else if (tlAbsChat instanceof TLChatForbidden)
                return null;
            else if (tlAbsChat instanceof TLChannelForbidden)
                return null;
            else if (tlAbsChat instanceof TLChatEmpty)
                return null;

            return null;
        }

        public static String extractMediaType(TLAbsMessageMedia messageMedia) {
            if (messageMedia instanceof TLMessageMediaPhoto)
                return "Фото";
            else if (messageMedia instanceof TLMessageMediaDocument)
                return "Документ";
            else if (messageMedia instanceof TLMessageMediaContact)
                return "Контакт";
            else if (messageMedia instanceof TLMessageMediaGame)
                return "Игра";
            else if (messageMedia instanceof TLMessageMediaGeo)
                return "Адрес";
            else
                return "";
        }

        public static String extractActionType(TLAbsMessageAction action) {
            if (action instanceof TLMessageActionChannelCreate)
                return "Канал создан";
            else if (action instanceof TLMessageActionChatAddUser)
                return "Пользователь добавлен";
            else if (action instanceof TLMessageActionChatCreate)
                return "Чат создан";
            else if (action instanceof TLMessageActionChatDeletePhoto)
                return "Фото удалено";
            else if (action instanceof TLMessageActionChatDeleteUser)
                return "Пользователь исключен";
            else if (action instanceof TLMessageActionChatEditPhoto)
                return "Фото изменено";
            else if (action instanceof TLMessageActionChatEditTitle)
                return "Название изменено";
            else if (action instanceof TLMessageActionPinMessage)
                return "Сообщение закреплено";
            else
                return "";
        }

        public static String extractParticipantRole(TLAbsChannelParticipant channelParticipant) {
            if (channelParticipant instanceof TLChannelParticipantCreator)
                return "Создатель";
            else if (channelParticipant instanceof TLChannelParticipantModerator)
                return "Модератор";
            else if (channelParticipant instanceof TLChannelParticipantEditor)
                return "Редактор";
            else if (channelParticipant instanceof TLChannelParticipantKicked)
                return "Исключен";
            else if (channelParticipant instanceof TLChannelParticipantSelf)
                return "Вы";
            else if (channelParticipant instanceof TLChannelParticipant)
                return "Участник";
            else
                return "Участник";
        }

        public static TLAbsChat getAbsChat(TelegramClient client, int chatId) {
            try {
                TLVector<TLAbsChat> tlAbsChats = client.messagesGetAllChats(new TLIntVector()).getChats();
                if (tlAbsChats.isEmpty())
                    return null;

                for (TLAbsChat chat : tlAbsChats) {
                    if (chat.getId() == chatId)
                        return chat;
                }
            } catch (RpcErrorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public static Chat getChat(TelegramClient client, Context context, User user, int chatId) {
            try {
                TLVector<TLAbsChat> tlAbsChats = client.messagesGetAllChats(new TLIntVector()).getChats();

                for (TLAbsChat chat : tlAbsChats) {
                    if (chat.getId() == chatId)
                        return getChat(client, chat, context);
                }

                TLInputUser inputUser = new TLInputUser();
                inputUser.setUserId(chatId);
                inputUser.setAccessHash(App.INSTANCE.getCurrentUser().getTelegramUser().getAccessHash());

                TLVector<TLAbsInputUser> users = new TLVector<>();
                users.add(inputUser);
                TLVector<TLAbsUser> absUsers = client.usersGetUsers(users);

                return getChat(client, context, absUsers.get(0));
            } catch (RpcErrorException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return convertUserToChat(user);
        }

        private static Chat convertUserToChat(User user) {
            Chat chat = new Chat(user.getId(), user.getName(), null, user.getImgUrl(), null, Consts.Telegram, 0, null);
            chat.setInputPeer(new TLInputPeerUser(Integer.valueOf(user.getId()), user.getAbsUser().getAsUser().getAccessHash()));

            return chat;
        }

        public static Chat getChat(TelegramClient client, Context context, TLAbsUser absUser) {
            TLUser tlUser = absUser.getAsUser();
            String fullname = tlUser.getFirstName() + " " + tlUser.getLastName();
            String imgUri = getUserImgUri(client, absUser, context);
            if (imgUri == null)
                imgUri = fullname;

            Chat chat = new Chat(String.valueOf(tlUser.getId()), fullname, null, imgUri, null, Consts.Telegram, 0, null);
            chat.setInputPeer(new TLInputPeerUser(tlUser.getId(), tlUser.getAccessHash()));

            return chat;
        }

        public static Chat getChat(TelegramClient client, TLAbsChat absChat, Context context) {
            Chat chat = null;
            String imgUri = getChatImgUri(client, absChat, context);

            if (absChat instanceof TLChat) {
                TLChat tlChat = (TLChat) absChat;

                if (imgUri == null)
                    imgUri = tlChat.getTitle();

                chat = new Chat(String.valueOf(absChat.getId()), tlChat.getTitle(), null, imgUri, null, Consts.Telegram, 0, null);
                chat.setInputPeer(new TLInputPeerChat(tlChat.getId()));
            } else if (absChat instanceof TLChannel) {
                TLChannel tlChannel = (TLChannel) absChat;

                if (imgUri == null)
                    imgUri = tlChannel.getTitle();

                chat = new Chat(String.valueOf(absChat.getId()), tlChannel.getTitle(), null, imgUri, null, Consts.Telegram, 0, null);
                chat.setInputPeer((new TLInputPeerChannel(tlChannel.getId(), tlChannel.getAccessHash())));
            }

            return chat;
        }

        public static String getChatImgUri(TelegramClient client, TLAbsChat absChat, Context context) {
            TLAbsChatPhoto absPhoto = Chats.extractChatPhoto(absChat);
            TLChatPhoto chatPhoto = null;

            if (absPhoto != null)
                chatPhoto = absPhoto.getAsChatPhoto();

            InputFileLocation inputFileLocation = null;
            long photoId = 0;

            if (chatPhoto != null) {
                inputFileLocation = TLMediaUtilsKt.toInputFileLocation(chatPhoto.getPhotoBig());
                photoId = absPhoto.getAsChatPhoto().getPhotoBig().getLocalId();
            }

            TelegramPhoto telegramPhoto = new TelegramPhoto(inputFileLocation, photoId);
            return TelegramHelper.Files.getImgById(client, telegramPhoto, context);
        }

        public static String getUserImgUri(TelegramClient client, TLAbsUser tlAbsUser, Context context) {
            TLAbsUserProfilePhoto absPhoto = tlAbsUser.getAsUser().getPhoto();
            InputFileLocation inputFileLocation = null;
            long photoId = 0;

            if (absPhoto != null) {
                TLAbsFileLocation fileLocation = absPhoto.getAsUserProfilePhoto().getPhotoBig();
                inputFileLocation = TLMediaUtilsKt.toInputFileLocation(fileLocation);
                photoId = tlAbsUser.getAsUser().getPhoto().getAsUserProfilePhoto().getPhotoId();
            }

            TelegramPhoto telegramPhoto = new TelegramPhoto(inputFileLocation, photoId);
            return TelegramHelper.Files.getImgById(client, telegramPhoto, context);
        }

        public static SparseArray<String> getChatNamesMap(TLAbsDialogs tlAbsDialogs) {
            SparseArray<String> nameMap = new SparseArray<>();

            TLVector<TLAbsUser> users = tlAbsDialogs.getUsers();

            users.forEach(absUser -> {
                TLUser user = absUser.getAsUser();
                String userName = user.getFirstName() + " " + user.getLastName();
                userName = userName.replace("null", "").trim();

                nameMap.put(user.getId(), userName);
            });

            TLVector<TLAbsChat> chats = tlAbsDialogs.getChats();

            chats.forEach(chat -> {
                String chatTitle = extractChatTitle(chat);
                chatTitle = chatTitle.replace("null", "").trim();
                nameMap.put(chat.getId(), chatTitle);
            });

            return nameMap;
        }

        public static SparseArray<TelegramPhoto> getPhotoMap(TLAbsDialogs tlAbsDialogs) {
            SparseArray<TelegramPhoto> photoMap = new SparseArray<>();

            TLVector<TLAbsUser> users = tlAbsDialogs.getUsers();

            users.forEach(absUser -> {
                TLUser user = absUser.getAsUser();
                TLAbsUserProfilePhoto absPhoto = user.getPhoto();
                InputFileLocation inputFileLocation = null;
                long photoId = 0;

                if (absPhoto != null) {
                    TLAbsFileLocation fileLocation = absPhoto.getAsUserProfilePhoto().getPhotoBig();
                    inputFileLocation = TLMediaUtilsKt.toInputFileLocation(fileLocation);
                    photoId = user.getPhoto().getAsUserProfilePhoto().getPhotoId();
                }

                TelegramPhoto telegramPhoto = new TelegramPhoto(inputFileLocation, photoId);
                photoMap.put(user.getId(), telegramPhoto);
            });

            TLVector<TLAbsChat> chats = tlAbsDialogs.getChats();

            chats.forEach(chat -> {
                TLAbsChatPhoto absPhoto = Chats.extractChatPhoto(chat);
                TLChatPhoto chatPhoto = null;

                if (absPhoto != null)
                    chatPhoto = absPhoto.getAsChatPhoto();

                InputFileLocation inputFileLocation = null;
                long photoId = 0;

                if (chatPhoto != null) {
                    inputFileLocation = TLMediaUtilsKt.toInputFileLocation(chatPhoto.getPhotoBig());
                    photoId = absPhoto.getAsChatPhoto().getPhotoBig().getLocalId();
                }

                TelegramPhoto telegramPhoto = new TelegramPhoto(inputFileLocation, photoId);
                photoMap.put(chat.getId(), telegramPhoto);
            });

            return photoMap;
        }

        public static SparseArray<User> getChatUsers(TelegramClient client, TLAbsMessages messages, Context context) {
            SparseArray<User> users = new SparseArray<>();
            for (TLAbsUser absUser : messages.getUsers()) {
                TLUser tlUser = absUser.getAsUser();
                int userId = absUser.getId();
                String fullname = TelegramHelper.Users.extractName(tlUser);
                String userName = tlUser.getUsername();
                String phoneNumber = tlUser.getPhone();
                Log.d("debug", "Helper, user id: " + String.valueOf(userId) + ", name: " + fullname);

                String imgUri = getUserImgUri(client, absUser, context);
                if (imgUri == null)
                    imgUri = fullname;

                User user = new User(String.valueOf(userId), Consts.Telegram, fullname, userName, imgUri);
                user.setInputUser(new TLInputUser(tlUser.getId(), tlUser.getAccessHash()));
                user.setAbsUser(absUser);
                user.setPhoneNumber(phoneNumber);
                users.put(userId, user);
            }

            for (TLAbsChat absChat : messages.getChats()) {
                int userId = absChat.getId();
                String fullname = TelegramHelper.Chats.extractChatTitle(absChat);

                String imgUri = getChatImgUri(client, absChat, context);
                if (imgUri == null)
                    imgUri = fullname;

                User user = new User(String.valueOf(userId), Consts.Telegram, fullname, fullname, imgUri);
                users.put(userId, user);
            }
            return users;
        }

        public static User getChatUser(TelegramClient client, int userId, Context context) throws Exception {
            TLUser tlUser = Users.getUser(client, userId).getUser().getAsUser();
            String fullname = TelegramHelper.Users.extractName(tlUser);
            String userName = "@" + tlUser.getUsername();
            String phoneNumber = tlUser.getPhone();

            String imgUri = getUserImgUri(client, (TLAbsUser) tlUser, context);
            if (imgUri == null)
                imgUri = fullname;

            User user = new User(String.valueOf(userId), Consts.Telegram, fullname, userName, imgUri);
            user.setInputUser(new TLInputUser(tlUser.getId(), tlUser.getAccessHash()));
            user.setAbsUser(tlUser);
            user.setPhoneNumber(phoneNumber);
            return user;
        }

        public static User getChatAsUser(TelegramClient client, int chatId, Context context) {
            String fullname;
            String userName;
            String imgUri;

            TLAbsChat absChat = TelegramHelper.Chats.getAbsChat(client, chatId);

            fullname = TelegramHelper.Chats.extractChatTitle(absChat);
            userName = fullname;

            imgUri = getChatImgUri(client, absChat, context);
            if (imgUri == null)
                imgUri = fullname;

            User user = new User(String.valueOf(chatId),
                    Consts.Telegram,
                    fullname,
                    userName,
                    imgUri);

            return user;
        }

        public static int getId(TLAbsPeer peer) {
            if (peer instanceof TLPeerUser)
                return ((TLPeerUser) peer).getUserId();
            if (peer instanceof TLPeerChat)
                return ((TLPeerChat) peer).getChatId();

            return ((TLPeerChannel) peer).getChannelId();
        }
    }
}
