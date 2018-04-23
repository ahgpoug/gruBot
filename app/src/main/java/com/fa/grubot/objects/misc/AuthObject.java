package com.fa.grubot.objects.misc;

public class AuthObject {
    private boolean telegramAuth;
    private boolean vkAuth;

    public AuthObject(boolean telegramAuth, boolean vkAuth) {
        this.telegramAuth = telegramAuth;
        this.vkAuth = vkAuth;
    }

    public boolean isHasTelegramAuth() {
        return telegramAuth;
    }

    public boolean isHasVkAuth() {
        return vkAuth;
    }
}
