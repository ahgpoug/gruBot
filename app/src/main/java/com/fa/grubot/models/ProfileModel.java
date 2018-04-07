package com.fa.grubot.models;

import android.content.Context;
import android.util.Log;

import com.fa.grubot.App;
import com.fa.grubot.helpers.TelegramHelper;
import com.fa.grubot.objects.pojos.VkUserResponseWithPhoto;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.util.Consts;
import com.fa.grubot.util.Globals;
import com.github.badoualy.telegram.api.TelegramClient;
import com.github.badoualy.telegram.tl.exception.RpcErrorException;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;

import io.reactivex.Single;

/**
 * Created by ni.petrov on 04/04/2018.
 */

public class ProfileModel {

    public ProfileModel() {
    }

    public Single<VkUserResponseWithPhoto> askForVkUserInfo(int userId) {
        Log.d("PROFILE", "start load vk data");
        return Single.create(singleSubscriber  -> {
            VKApi.users().get(
                VKParameters.from(VKApiConst.USER_ID, userId, VKApiConst.FIELDS, "domain, photo_200"))
                    .executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            VkUserResponseWithPhoto userVk = new VkUserResponseWithPhoto();
                            try {
                                userVk.setFirstName(response.json.getJSONArray("response")
                                        .getJSONObject(0)
                                        .getString("first_name"));

                                userVk.setLastName(response.json.getJSONArray("response")
                                        .getJSONObject(0)
                                        .getString("last_name"));

                                userVk.setDomain(response.json.getJSONArray("response")
                                        .getJSONObject(0)
                                        .getString("domain"));

                                userVk.setPhoto100(response.json.getJSONArray("response")
                                        .getJSONObject(0)
                                        .getString("photo_200"));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Log.d("PROFILE", "data loaded");
                            singleSubscriber.onSuccess(userVk);
                        }

                        @Override
                        public void onError(VKError error) {
                            super.onError(error);
                            singleSubscriber.onError(new Exception(error.httpError));
                        }
                    });
        });
    }

    public User askForTelegramUserInfo(int userId, Context context) throws Exception {
        TelegramClient client = App.INSTANCE.getNewDownloaderClient();

        User telegramUser;
        try {
            telegramUser = TelegramHelper.Chats.getChatUser(client, userId, context);
        } catch (Exception e) {
            if (e instanceof RpcErrorException && ((RpcErrorException) e).getCode() == 420) {
                int floodTime = Globals.extractMillisFromRpcException((RpcErrorException) e);
                Thread.sleep(floodTime + 500);
                telegramUser = askForTelegramUserInfo(userId, context);
            } else {
                telegramUser = new User(e.getMessage(), Consts.Telegram, e.getMessage(), e.getMessage(), e.getMessage());
            }
        } finally {
            client.close(false);
        }

        return telegramUser;
    }
}
