package com.fa.grubot.models;

import android.content.Context;

import com.fa.grubot.App;
import com.fa.grubot.objects.dashboard.DashboardAnnouncement;
import com.fa.grubot.objects.dashboard.DashboardItem;
import com.fa.grubot.objects.dashboard.DashboardVote;
import com.fa.grubot.util.Globals;

import java.util.ArrayList;

import io.reactivex.Observable;

public class DashboardModel {

    public DashboardModel(){

    }

    public ArrayList<DashboardItem> getItems() {
        ArrayList<DashboardItem> items = new ArrayList<>();
        items.add(new DashboardAnnouncement(getNewAnnouncementsCount(), 0, getNewAnnouncementsCount()));
        items.add(new DashboardVote(getNewVotesCount(), 0, getNewVotesCount()));
        return items;
    }

    private int getNewAnnouncementsCount() {
        return App.INSTANCE.getDataHelper().getAnnouncemtsCount();
    }

    private int getNewVotesCount() {
        return App.INSTANCE.getDataHelper().getVotesCount();
    }

    public Observable<Boolean> isNetworkAvailable(Context context) {
        return Globals.InternetMethods.getNetworkObservable(context);
    }
}
