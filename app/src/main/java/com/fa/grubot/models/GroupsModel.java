package com.fa.grubot.models;

import android.content.Context;

import com.fa.grubot.App;
import com.fa.grubot.objects.group.Group;
import com.fa.grubot.util.Globals;

import java.util.ArrayList;

import io.reactivex.Observable;

public class GroupsModel {

    public GroupsModel(){

    }
    public ArrayList<Group> loadGroups(){
        return App.INSTANCE.getDataHelper().getGroups();
    }

    public Observable<Boolean> isNetworkAvailable(Context context) {
        return Globals.InternetMethods.getNetworkObservable(context);
    }
}
