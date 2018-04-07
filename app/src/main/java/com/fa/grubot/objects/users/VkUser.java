package com.fa.grubot.objects.users;

import android.util.Log;

import com.fa.grubot.objects.pojos.VkUserResponse;
import com.google.gson.Gson;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;

/**
 * Created by ni.petrov on 16/03/2018.
 */

public class VkUser {

    private String firstName;

    private String lastName;

    private Integer id;

    private String accessToken;

    private String domain;

    public VkUser(String access_token) {
        accessToken = access_token;
        loadUserInfo();
    }

    private void loadUserInfo() {
        VKRequest request = VKApi.users().get();
        request.executeSyncWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                Gson gson = new Gson();
                try {
                    VkUserResponse res = gson.fromJson(response.json.getJSONArray("response").get(0).toString(), VkUserResponse.class);
                    setFirstName(res.getFirstName());
                    setLastName(res.getLastName());
                    setId(res.getId());
                    setDomain(res.getDomain());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("VkUser", "vkUser intance created " + this.toString());
            }
        });
    }


    public String getFirstName() {
        return firstName;
    }

    private void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public int getId() {
        return id;
    }

    private VkUser setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public VkUser setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public VkUser setDomain(String domain) {
        this.domain = domain;
        return this;
    }


    @Override
    public String toString() {
        return "VkUser{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", domain=" + domain + '\'' +
                ", id=" + id +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }
}