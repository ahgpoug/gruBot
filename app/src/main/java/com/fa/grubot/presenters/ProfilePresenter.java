package com.fa.grubot.presenters;


import android.content.Context;

import com.fa.grubot.abstractions.ProfileFragmentBase;
import com.fa.grubot.models.ProfileModel;
import com.fa.grubot.objects.group.User;
import com.fa.grubot.objects.misc.ProfileItem;
import com.fa.grubot.util.Globals;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ProfilePresenter {
    private ProfileFragmentBase fragment;
    private ProfileModel model;

    private ArrayList<ProfileItem> items = new ArrayList<>();

    public ProfilePresenter(ProfileFragmentBase fragment){
        this.fragment = fragment;
        this.model = new ProfileModel();
    }

    public void notifyViewCreated(int state) {
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

    public void notifyFragmentStarted(Context context, User user) {
        model.isNetworkAvailable(context)
                .doOnNext(result -> {
                    if (result)
                        getData(true, user);
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

    private void getData(final boolean isFirst, final User user) {
        Observable.just(model.getItems(user))
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

    public void onRetryBtnClick(Context context, User user) {
        model.isNetworkAvailable(context)
                .doOnNext(result -> {
                    if (result)
                        getData(false, user);
                })
                .subscribe();
    }
    public void destroy(){
        fragment = null;
        model = null;
    }
}
