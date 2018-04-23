package com.fa.grubot.objects.dashboard;

import com.fa.grubot.objects.misc.VoteOption;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class ActionPoll extends Action {
    private ArrayList<String> options = new ArrayList<>();

    public ActionPoll(String id, String group, String groupName, String author, String authorName, String desc, Date date, ArrayList<VoteOption> options, Map<String, String> users, long messageId, String type) {
        super(id, group, groupName, author, authorName, desc, date, users, messageId, type);

        for (VoteOption option : options){
            this.options.add(option.getText());
        }
    }

    public ArrayList<String> getOptions() {
        return options;
    }
}
