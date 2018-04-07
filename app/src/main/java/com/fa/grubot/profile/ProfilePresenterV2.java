package com.fa.grubot.profile;

import android.util.Log;

import com.fa.grubot.objects.pojos.VkUserResponseWithPhoto;

import org.json.JSONObject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ni.petrov on 04/04/2018.
 */

public class ProfilePresenterV2 {
    private ProfileItemFragment view;

    private ProfileModelV2 model;


    public ProfilePresenterV2(ProfileItemFragment view) {
        this.view = view;
        model = new ProfileModelV2(this);
    }

    public void askForVkStuff() {
        Observable.just(true).map(x -> {
            Log.d("PROFILE", "profile asks for model");
            return model.askForMyInfo();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vkUserObs -> {
                    vkUserObs.subscribe(vkusr -> view.drawVkUser(vkusr));
                });
    }
}
