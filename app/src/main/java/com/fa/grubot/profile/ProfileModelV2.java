package com.fa.grubot.profile;

import android.util.Log;

import com.fa.grubot.objects.pojos.VkUserResponse;
import com.fa.grubot.objects.pojos.VkUserResponseWithPhoto;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;

import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by ni.petrov on 04/04/2018.
 */

public class ProfileModelV2 {
    private ProfilePresenterV2 presenter;


    public ProfileModelV2(ProfilePresenterV2 presenter) {
        this.presenter = presenter;
    }

    public Single<VkUserResponseWithPhoto> askForMyInfo() {
        Log.d("PROFILE", "start load data");
        return Single.create(singleSubscriber  -> {
            VKApi.users().get(
                    VKParameters.from(VKApiConst.USER_ID,
                            VKAccessToken.currentToken().userId, VKApiConst.FIELDS, "photo_200"))
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
}
