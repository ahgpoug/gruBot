package com.fa.grubot.objects.dashboard;

import java.util.Date;
import java.util.Map;

public class ActionArticle extends Action {
    private String text;

    public ActionArticle(String id, String group, String groupName, String author, String authorName, String desc, Date date, String text, Map<String, String> users, long messageId, String type) {
        super(id, group, groupName, author, authorName, desc, date, users, messageId, type);

        this.text = text;
    }

    public String getText() {
        return text;
    }
}
