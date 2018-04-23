package com.fa.grubot.objects.users;

import com.github.badoualy.telegram.tl.api.TLAbsInputUser;
import com.github.badoualy.telegram.tl.api.TLAbsUser;
import com.stfalcon.chatkit.commons.models.IUser;

import java.io.Serializable;

public class User implements Serializable, IUser {

    private String userId;
    private String userType;
    private String fullname;
    private String userName;
    private String imgUrl;
    private String chatRole;
    private String phoneNumber;
    private TLAbsInputUser inputUser;
    private TLAbsUser absUser;

    public User(String userId, String userType, String fullname, String userName, String imgUrl) {
        this.userId = userId;
        this.userType = userType;
        this.fullname = fullname;
        this.userName = userName;
        this.imgUrl = imgUrl;
    }

    @Override
    public String getId() {
        return userId;
    }

    @Override
    public String getName() {
        return fullname;
    }

    @Override
    public String getAvatar() {
        return imgUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserType() {
        return userType;
    }

    public String getFullname() {
        return fullname;
    }

    public String getUserName() {
        return userName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getChatRole() {
        return chatRole;
    }

    public void setChatRole(String chatRole) {
        this.chatRole = chatRole;
    }

    public TLAbsInputUser getInputUser() {
        return inputUser;
    }

    public void setInputUser(TLAbsInputUser inputUser) {
        this.inputUser = inputUser;
    }

    public TLAbsUser getAbsUser() {
        return absUser;
    }

    public void setAbsUser(TLAbsUser absUser) {
        this.absUser = absUser;
    }

    public User setFullname(String fullname) {
        this.fullname = fullname;
        return this;
    }

    public User setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public User setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
        return this;
    }
}
