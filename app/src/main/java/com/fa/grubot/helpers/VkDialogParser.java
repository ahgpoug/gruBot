package com.fa.grubot.helpers;

import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.chat.RawVkChat;
import com.fa.grubot.objects.pojos.VkMessagePOJO;
import com.fa.grubot.objects.pojos.VkUserResponse;
import com.fa.grubot.objects.pojos.VkUserResponseWithPhoto;
import com.fa.grubot.util.DataType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKBatchRequest;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

/**
 * Created by ni.petrov on 23/03/2018.
 */

public class VkDialogParser {

    private ReplaySubject<Chat> dialogsSubscription = ReplaySubject.create();

    private VKResponse dialogsResponse;

    private Gson gson = new Gson();

    private List<Chat> parsedChats = new ArrayList<>();


    public VkDialogParser(VKResponse dialogsResponse) {
        this.dialogsResponse = dialogsResponse;
        try {
            parse(this.dialogsResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parse(VKResponse dialogsResponse) throws JSONException {
        JSONObject json = dialogsResponse.json;
        JSONArray messagesArrayJson = json.getJSONObject("response").getJSONArray("items");

        for (int i = 0; i < messagesArrayJson.length(); i++) {
            VkMessagePOJO messagePOJO =
                    gson.fromJson(messagesArrayJson.getJSONObject(i)
                            .getJSONObject("message").toString(), VkMessagePOJO.class);

            boolean isGroupDialog = messagesArrayJson.getJSONObject(i).getJSONObject("message").has("chat_id");

            Map<String, Boolean> usersInDialog = new HashMap<>();

            if (isGroupDialog && messagesArrayJson.getJSONObject(i).getJSONObject("message").has("chat_active")) {
                usersInDialog = getUsersFromChatActiveJsonObject(
                        messagesArrayJson.getJSONObject(i).getJSONObject("message")
                                .getJSONArray("chat_active"));
            } else {
                usersInDialog.put(messagePOJO.getId().toString(), true);
            }

            RawVkChat chat = new RawVkChat(messagePOJO.getUserId().toString(),
                    messagePOJO.getTitle(),
                    usersInDialog,
                    getPictureLinkFromJson(messagesArrayJson.getJSONObject(i)),
                    messagePOJO.getBody(),
                    DataType.VK,
                    messagePOJO.getDate(),
                    "author's_name_placeholder",
                    isGroupDialog,
                    messagesArrayJson.getJSONObject(i).getJSONObject("message").getString("out")
                    ); //TODO may be combained into batch request
            parsedChats.add(chat);
        }

        onChatsParsed();
    }

    private void onChatsParsed() {
        //создаем список запросов на запрос имени
        List<Integer> idOfUsersWhoNeedMoreInfo = new ArrayList<>();

        for (Chat rawChat : parsedChats) {
            RawVkChat rawVkChat = (RawVkChat) rawChat;
            //if (!rawVkChat.isGroupDialog()){
                idOfUsersWhoNeedMoreInfo.add(Integer.valueOf(rawVkChat.getId()));
            //}
        }
        // объединяем запросы в один
        String userIdsWithComa = "1, ";
        for (Integer id : idOfUsersWhoNeedMoreInfo) {
            userIdsWithComa += id + ", ";
        }

        VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS,
                userIdsWithComa, VKApiConst.FIELDS, "photo_100"));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    Type itemsListType = new TypeToken<List<VkUserResponseWithPhoto>>() {}.getType();
                    List<VkUserResponseWithPhoto> listItemsDes =
                            gson.fromJson(response.json.getJSONArray("response").toString(), itemsListType);

                    for (VkUserResponseWithPhoto usr: listItemsDes) {
                        for (Chat chat : parsedChats) {
                            if (usr.getId().toString().equals(chat.getId())){
                                boolean isGroupDialog = ((RawVkChat)chat).isGroupDialog();
                                if (!isGroupDialog) {
                                    chat.setName(usr.getFirstName() + " " + usr.getLastName());
                                    chat.setImgUri(usr.getPhoto100());
                                }
                                if (!((RawVkChat)chat).isLastMessageInput()){
                                    chat.setLastMessageFrom(usr.getFirstName());
                                } else {
                                    chat.setLastMessageFrom("Вы");
                                }
                            }
                        }
                    }

                    onChatsReadyToDraw();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });

    }


    private Map<String, Boolean> getUsersFromChatActiveJsonObject(JSONArray chat_active) {
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        Map<String, Boolean> users = new HashMap<>();
        List<String> userList = new Gson().fromJson(chat_active.toString(), listType);
        for (String s : userList) {
            users.put(s, true);
        }
        return users;
    }

    private void onChatsReadyToDraw() {
        for (Chat chat : parsedChats) {
            dialogsSubscription.onNext(chat);
        }

        dialogsSubscription.onComplete();
    }

    public Observable<Chat> getDialogsSubscription() {
        return dialogsSubscription;
    }

    private String getPictureLinkFromJson(JSONObject jsonObject) {
        try {
            if (jsonObject.getJSONObject("message").has("photo_100")) {
                return jsonObject.getJSONObject("message").getString("photo_100");
            } else {
                return "http://nonsoc.com/uploads/posts/2017-04/1492002153_vk-zen-dlya-google-chrome.jpg";
            }

        } catch (JSONException e) {
            return "http://nonsoc.com/uploads/posts/2017-04/1492002153_vk-zen-dlya-google-chrome.jpg";
        }

    }
}
