package com.fa.grubot.presenters;


import android.content.Context;

import com.fa.grubot.abstractions.ActionsFragmentBase;
import com.fa.grubot.models.ActionsModel;
import com.fa.grubot.objects.dashboard.Action;
import com.fa.grubot.util.Globals;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ActionsPresenter {
    private ActionsFragmentBase fragment;
    private ActionsModel model;
    private ArrayList<Action> actions = new ArrayList<>();

    public ActionsPresenter(ActionsFragmentBase fragment){
        this.fragment = fragment;
        this.model = new ActionsModel();
    }

    private void notifyViewCreated(int state) {
        fragment.showRequiredViews();

        switch (state) {
            case Globals.FragmentState.STATE_CONTENT:
                fragment.setupRecyclerView(actions);
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

    public void notifyFragmentStarted(Context context, int type) {
        model.isNetworkAvailable(context)
                .doOnNext(result -> {
                    if (result)
                        getData(true, type);
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

    private void getData(final boolean isFirst, final int type) {
        Observable.just(model.loadActions(type))
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
                    if (actions.isEmpty()) {
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

    public void onRefresh(Context context, int type) {
        model.isNetworkAvailable(context)
                .doOnNext(result -> {
                    if (result)
                        getData(false, type);
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

    public void onRetryBtnClick(Context context, int type) {
        model.isNetworkAvailable(context)
                .doOnNext(result -> {
                    if (result)
                        getData(false, type);
                })
                .subscribe();
    }

    public void destroy(){
        fragment = null;
        model = null;
    }
}
