package com.fa.grubot.objects.dashboard;

import com.fa.grubot.objects.group.Group;
import com.fa.grubot.objects.misc.VoteOption;

import java.util.ArrayList;
import java.util.Date;

public class ActionVote extends Action {
    private ArrayList<String> options = new ArrayList<>();

    public ActionVote(int id, Group group, String author, String desc, Date date, ArrayList<VoteOption> options) {
        super(id, group, author, desc,date);

        for (VoteOption option : options){
            this.options.add(option.getText());
        }
    }

    public ArrayList<String> getOptions() {
        return options;
    }
}
