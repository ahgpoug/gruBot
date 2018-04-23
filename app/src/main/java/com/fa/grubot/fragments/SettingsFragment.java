package com.fa.grubot.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fa.grubot.App;
import com.fa.grubot.LoginActivity;
import com.fa.grubot.MainActivity;
import com.fa.grubot.R;
import com.fa.grubot.SplashActivity;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.util.Consts;
import com.github.badoualy.telegram.tl.core.TLBool;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKServiceActivity;
import com.vk.sdk.api.VKError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SettingsFragment extends PreferenceFragmentCompat implements Serializable {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(android.R.color.white));
        setupToolbar();
        setupViews();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void setupToolbar() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setTitle("Настройки");

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    private void setupViews() {
        Preference vkAccount = findPreference("vkAccount");
        Preference telegramAccount = findPreference("telegramAccount");

        SwitchPreferenceCompat animationsSwitch = (SwitchPreferenceCompat) findPreference("animationsSwitch");
        SwitchPreferenceCompat backstackSwitch = (SwitchPreferenceCompat) findPreference("backstackSwitch");
        SwitchPreferenceCompat slidrSwitch = (SwitchPreferenceCompat) findPreference("slidrSwitch");

        if (App.INSTANCE.vkMessenger.isHasUser()) {
            User vkUser = App.INSTANCE.vkMessenger.getCurrentUser();
            vkAccount.setSummary(vkUser.getFullname());
        }

        vkAccount.setOnPreferenceClickListener(preference -> {
            if (App.INSTANCE.vkMessenger.isHasUser()) {
                User vkUser = App.INSTANCE.vkMessenger.getCurrentUser();
                new MaterialDialog.Builder(getActivity())
                        .title("Выйти из аккаунта")
                        .content("Вы уверены, что хотите выйти из аккаунта?")
                        .positiveText(android.R.string.yes)
                        .negativeText(android.R.string.cancel)
                        .onPositive((dialog, which) -> {
                            VKSdk.logout();
                            App.INSTANCE.vkMessenger.setCurrentUser(null);

                            if (!App.INSTANCE.telegramMessenger.isHasUser()) {
                                Intent intent = new Intent(getContext(), SplashActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else {
                                vkAccount.setSummary(R.string.no_account_connected);
                                Toast.makeText(getContext(), "Выход выполнен", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
                return false;
            } else {
                String[] scope = {
                        VKScope.FRIENDS,
                        VKScope.EMAIL,
                        VKScope.NOHTTPS,
                        VKScope.MESSAGES
                };

                Intent intent = new Intent(getActivity(), VKServiceActivity.class);
                intent.putExtra("arg1", "Authorization");
                ArrayList<String> scopes = new ArrayList<>(Arrays.asList(scope));
                intent.putStringArrayListExtra("arg2", scopes);
                intent.putExtra("arg4", VKSdk.isCustomInitialize());
                startActivityForResult(intent, VKServiceActivity.VKServiceType.Authorization.getOuterCode());
                return false;
            }
        });


        if (App.INSTANCE.telegramMessenger.isHasUser()) {
            User telegramUser = App.INSTANCE.telegramMessenger.getCurrentUser();
            telegramAccount.setSummary(telegramUser.getFullname());
        }

        telegramAccount.setOnPreferenceClickListener(preference -> {
            if (App.INSTANCE.telegramMessenger.isHasUser()) {
                new MaterialDialog.Builder(getActivity())
                        .title("Выйти из аккаунта")
                        .content("Вы уверены, что хотите выйти из аккаунта?")
                        .positiveText(android.R.string.yes)
                        .negativeText(android.R.string.cancel)
                        .onPositive((dialog, which) -> {
                            Observable<Object> logOutObs = App.INSTANCE.telegramMessenger.logOutObs().timeout(3, TimeUnit.SECONDS).onErrorResumeNext(Observable.empty()).defaultIfEmpty(false);

                            Observable.defer(() -> logOutObs)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnNext(returnObject -> {
                                        if (returnObject instanceof TLBool) {
                                            Toast.makeText(getContext(), "Выход выполнен", Toast.LENGTH_SHORT).show();
                                            if (App.INSTANCE.vkMessenger.isHasUser()) {
                                                telegramAccount.setSummary(R.string.no_account_connected);
                                            } else {
                                                startActivity(new Intent(getActivity(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                            }
                                        } else if (returnObject instanceof Exception) {
                                            Toast.makeText(getContext(), "Ошибка: " + ((Exception) returnObject).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .subscribe();
                        })
                        .show();
                return false;
            } else {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.putExtra("directLogin", Consts.Telegram);
                getActivity().startActivity(intent);
                return false;
            }
        });

        animationsSwitch.setOnPreferenceChangeListener((preference,o)->

    {
        App.INSTANCE.setAnimationsEnabled((boolean) o);
        return true;
    });

        backstackSwitch.setOnPreferenceChangeListener((preference,o)-> {
        App.INSTANCE.setBackstackEnabled((boolean) o);
        return true;
    });

        slidrSwitch.setOnPreferenceChangeListener((preference,o)-> {
        App.INSTANCE.setSlidrEnabled((boolean) o);
        return true;
    });
}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken vkAccessToken) {
                Preference vkAccount = findPreference("vkAccount");
                User user = App.INSTANCE.vkMessenger.setVkUser(vkAccessToken);
                vkAccount.setSummary(user.getFullname());
            }

            @Override
            public void onError(VKError error) {
                Toast.makeText(getActivity(), "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
