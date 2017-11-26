package com.fa.grubot.presenters;


import android.content.Context;

import com.fa.grubot.abstractions.GroupsFragmentBase;
import com.fa.grubot.models.GroupsModel;
import com.fa.grubot.objects.group.Group;
import com.fa.grubot.util.Globals;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GroupsPresenter {
    private GroupsFragmentBase fragment;
    private GroupsModel model;

    private ArrayList<Group> groups = new ArrayList<>();


    public GroupsPresenter(GroupsFragmentBase fragment){
        this.fragment = fragment;
        this.model = new GroupsModel();
    }

    public void notifyViewCreated(int state){
        fragment.showRequiredViews();

        switch (state) {
            case Globals.FragmentState.STATE_CONTENT:
                fragment.setupToolbar();
                fragment.setupRecyclerView(groups);
                fragment.setupSwipeRefreshLayout();
                break;
            case Globals.FragmentState.STATE_NO_INTERNET_CONNECTION:
                fragment.setupRetryButton();
                break;
            case Globals.FragmentState.STATE_NO_DATA:
                fragment.setupSwipeRefreshLayout();
                break;
        }
    }

    public void notifyFragmentStarted(Context context){
        model.isNetworkAvailable(context)
                .doOnNext(result -> {
                    if (result)
                        getData(true);
                    else {
                        fragment.setupLayouts(false, false);
                        notifyViewCreated(Globals.FragmentState.STATE_NO_INTERNET_CONNECTION);
                    }
                })
                .doOnError(error -> {
                    fragment.setupLayouts(false, false);
                    notifyViewCreated(Globals.FragmentState.STATE_NO_INTERNET_CONNECTION);
                })
                .subscribe();
    }

    private void getData(final boolean isFirst) {
        Observable.just(model.loadGroups())
                .filter(result -> result != null)
                .subscribeOn(Schedulers.io())
                .timeout(15, TimeUnit.SECONDS)
                .doOnSubscribe(d -> {
                    if (!isFirst)
                        fragment.showLoadingView();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(result -> {
                    groups.clear();
                    groups.addAll(result);
                    if (groups.isEmpty()) {
                        fragment.setupLayouts(true, false);
                        notifyViewCreated(Globals.FragmentState.STATE_NO_DATA);
                    } else {
                        fragment.setupLayouts(true, true);
                        notifyViewCreated(Globals.FragmentState.STATE_CONTENT);
                    }
                })
                .doOnError(error -> {
                    fragment.setupLayouts(false, false);
                    notifyViewCreated(Globals.FragmentState.STATE_NO_INTERNET_CONNECTION);
                })
                .subscribe();
    }

    public void onRefresh(Context context) {
        model.isNetworkAvailable(context)
                .doOnNext(result -> {
                    if (result)
                        getData(false);
                    else {
                        fragment.setupLayouts(false, false);
                        notifyViewCreated(Globals.FragmentState.STATE_NO_INTERNET_CONNECTION);
                    }
                })
                .doOnError(error -> {
                    fragment.setupLayouts(false, false);
                    notifyViewCreated(Globals.FragmentState.STATE_NO_INTERNET_CONNECTION);
                })
                .subscribe();
    }

    public void onRetryBtnClick(Context context) {
        model.isNetworkAvailable(context)
                .doOnNext(result -> {
                    if (result)
                        getData(false);
                })
                .subscribe();
    }

    public void destroy(){
        fragment = null;
        model = null;
    }
}
