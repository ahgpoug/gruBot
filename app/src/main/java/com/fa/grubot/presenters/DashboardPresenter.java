package com.fa.grubot.presenters;


import android.content.Context;

import com.fa.grubot.abstractions.DashboardFragmentBase;
import com.fa.grubot.models.DashboardModel;
import com.fa.grubot.objects.dashboard.DashboardItem;
import com.fa.grubot.util.Globals;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DashboardPresenter {
    private DashboardFragmentBase fragment;
    private DashboardModel model;

    private ArrayList<DashboardItem> items = new ArrayList<>();

    public DashboardPresenter(DashboardFragmentBase fragment){
        this.fragment = fragment;
        this.model = new DashboardModel();
    }

    private void notifyViewCreated(int state) {
        fragment.showRequiredViews();

        switch (state) {
            case Globals.FragmentState.STATE_CONTENT:
                fragment.setupToolbar();
                fragment.setupRecyclerView(items);
                break;
            case Globals.FragmentState.STATE_NO_INTERNET_CONNECTION:
                fragment.setupRetryButton();
                break;
        }
    }

    public void notifyFragmentStarted(Context context) {
        model.isNetworkAvailable(context)
                .doOnNext(result -> {
                    if (result)
                        getData(true);
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

    private void getData(final boolean isFirst) {
        Observable.just(model.getItems())
                .filter(result -> result != null)
                .subscribeOn(Schedulers.io())
                .timeout(15, TimeUnit.SECONDS)
                .doOnSubscribe(d -> {
                    if (!isFirst)
                        fragment.showLoadingView();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(result -> {
                    items.clear();
                    items.addAll(result);
                    fragment.setupLayouts(true);
                    notifyViewCreated(Globals.FragmentState.STATE_CONTENT);
                })
                .doOnError(error -> {
                    fragment.setupLayouts(false);
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
