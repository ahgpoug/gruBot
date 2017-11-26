package com.fa.grubot.objects.chat;


import java.util.Calendar;

/**
 * Created by ni.petrov on 18/11/2017.
 */

public class BranchOfDiscussions {
    private int id;

    private int authorsId;

    private String theme;

    private Calendar startDate;

    private Calendar lastDate;

    private int messagesCount;

    public BranchOfDiscussions(int id, int authorsId, String theme, Calendar startDate, Calendar lastDate, int messagesCount) {
        this.id = id;
        this.authorsId = authorsId;
        this.theme = theme;
        this.startDate = startDate;
        this.lastDate = lastDate;
        this.messagesCount = messagesCount;
    }

    public int getId() {
        return id;
    }

    public int getAuthorsId() {
        return authorsId;
    }

    public String getTheme() {
        return theme;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public Calendar getLastDate() {
        return lastDate;
    }

    public int getMessagesCount() {
        return messagesCount;
    }
}
