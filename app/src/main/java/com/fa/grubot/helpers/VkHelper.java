package com.fa.grubot.helpers;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

/**
 * Created by ni.petrov on 23/03/2018.
 */

public class VkHelper {

    private static UserInfo info;

    public static UserInfo getVkUserInfoById(String id){
        UserInfo user;

        VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, id, VKApiConst.FIELDS, "photo_100"));
        request.executeSyncWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                try {
                    UserInfo user = new UserInfo();
                    user.setId(response.json.getJSONArray("response").getJSONObject(0).getInt("id"));
                    user.setFirst_name(response.json.getJSONArray("response").getJSONObject(0).getString("first_name"));
                    user.setLast_name(response.json.getJSONArray("response").getJSONObject(0).getString("last_name"));
                    user.setPhoto_100(response.json.getJSONArray("response").getJSONObject(0).getString("photo_100"));
                    info = user;
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VKError error) {

            }
        });
        return info;
    }

    public static class UserInfo{
        int id;
        String first_name;
        String last_name;

        String photo_100;

        public int getId() {
            return id;
        }

        public UserInfo setId(int id) {
            this.id = id;
            return this;
        }

        public String getFirst_name() {
            return first_name;
        }

        public UserInfo setFirst_name(String first_name) {
            this.first_name = first_name;
            return this;
        }

        public String getLast_name() {
            return last_name;
        }

        public UserInfo setLast_name(String last_name) {
            this.last_name = last_name;
            return this;
        }

        public String getPhoto_100() {
            return photo_100;
        }

        public UserInfo setPhoto_100(String photo_100) {
            this.photo_100 = photo_100;
            return this;
        }
    }
}
