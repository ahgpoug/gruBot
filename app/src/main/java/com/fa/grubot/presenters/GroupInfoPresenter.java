package com.fa.grubot.presenters;


import android.content.Context;

import com.fa.grubot.abstractions.GroupInfoFragmentBase;
import com.fa.grubot.adapters.GroupInfoRecyclerAdapter;
import com.fa.grubot.models.GroupInfoModel;
import com.fa.grubot.objects.group.Group;
import com.fa.grubot.util.Globals;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class GroupInfoPresenter {
    private GroupInfoFragmentBase fragment;
    private GroupInfoModel model;
    private ArrayList<GroupInfoRecyclerAdapter.GroupInfoRecyclerItem> actions = new ArrayList<>();

    public GroupInfoPresenter(GroupInfoFragmentBase fragment) {
        this.fragment = fragment;
        this.model = new GroupInfoModel();
    }

    public void notifyViewCreated(int state){
        fragment.showRequiredViews();

        switch (state) {
            case Globals.FragmentState.STATE_CONTENT:
                fragment.setupToolbar();
                fragment.setupFab();
                fragment.setupRecyclerView(actions);
                break;
            case Globals.FragmentState.STATE_NO_INTERNET_CONNECTION:
                fragment.setupRetryButton();
                break;
        }
    }

    public void notifyFragmentStarted(Context context, Group group) {
        model.isNetworkAvailable(context)
                .doOnNext(result -> {
                    if (result)
                        getData(true, group);
                    else {
                        fragment.setupLayouts(false);
                        notifyViewCreated(Globals.FragmentState.STATE_NO_INTERNET_CONNECTION);
                    }
                })
                .doOnError(error -> {
                    fragment.setupLayouts(false);
                    notifyViewCreated(Globals.FragmentState.STATE_NO_INTERNET_CONNECTION);
                })
                .subscribe();
    }

    private void getData(final boolean isFirst, final Group group) {
        Observable.just(model.loadButtons(group))
                .filter(result -> result != null)
                .subscribeOn(Schedulers.io())
                .timeout(15, TimeUnit.SECONDS)
                .doOnSubscribe(d -> {
                    if (!isFirst)
                        fragment.showLoadingView();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(result -> {
                    actions.clear();
                    actions.addAll(result);
                    fragment.setupLayouts(true);
                    notifyViewCreated(Globals.FragmentState.STATE_CONTENT);
                })
                .doOnError(error -> {
                    fragment.setupLayouts(false);
                    notifyViewCreated(Globals.FragmentState.STATE_NO_INTERNET_CONNECTION);
                })
                .subscribe();
    }

    public void onRetryBtnClick(Context context, Group group) {
        model.isNetworkAvailable(context)
                .doOnNext(result -> {
                    if (result)
                        getData(false, group);
                })
                .subscribe();
    }

    public void destroy(){
        fragment = null;
        model = null;
    }
}
