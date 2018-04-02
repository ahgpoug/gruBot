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
import com.fa.grubot.objects.users.CurrentUser;
import com.fa.grubot.objects.users.VkUser;
import com.fa.grubot.util.Consts;
import com.github.badoualy.telegram.tl.api.TLUser;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKServiceActivity;
import com.vk.sdk.api.VKError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class SettingsFragment extends PreferenceFragmentCompat implements Serializable {

    private CurrentUser currentUser = App.INSTANCE.getCurrentUser();

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
        App.INSTANCE.closeTelegramClient();
        super.onPause();
    }

    @Override
    public void onStop() {
        App.INSTANCE.closeTelegramClient();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        App.INSTANCE.closeTelegramClient();
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

        if (currentUser.hasVkUser()) {
            VkUser vkUser = currentUser.getVkUser();
            vkAccount.setSummary(vkUser.getFirstName() + " " + vkUser.getLastName());
        }

        vkAccount.setOnPreferenceClickListener(preference -> {
            if (currentUser.hasVkUser()) {
                new MaterialDialog.Builder(getActivity())
                        .title("Выйти из аккаунта")
                        .content("Вы уверены, что хотите выйти из аккаунта?")
                        .positiveText(android.R.string.yes)
                        .negativeText(android.R.string.cancel)
                        .onPositive((dialog, which) -> {
                            VKSdk.logout();
                            currentUser.resetVkUser();

                            if (!currentUser.hasTelegramUser()) {
                                Intent intent = new Intent(getContext(), SplashActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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


        if (currentUser.hasTelegramUser()) {
            TLUser telegramUser = currentUser.getTelegramUser();
            telegramAccount.setSummary(telegramUser.getFirstName() + " " + telegramUser.getLastName());
        }

        telegramAccount.setOnPreferenceClickListener(preference -> {
            if (currentUser.hasTelegramUser()) {
                new MaterialDialog.Builder(getActivity())
                        .title("Выйти из аккаунта")
                        .content("Вы уверены, что хотите выйти из аккаунта?")
                        .positiveText(android.R.string.yes)
                        .negativeText(android.R.string.cancel)
                        .onPositive((dialog, which) -> {
                                try {
                                    currentUser.resetTelegramUser();
                                    if (currentUser.hasVkUser()) {
                                        Toast.makeText(getContext(), "Выход выполнен", Toast.LENGTH_SHORT).show();
                                        telegramAccount.setSummary(R.string.no_account_connected);
                                    } else {
                                        startActivity(new Intent(getActivity(), LoginActivity.class)
                                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
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
            public void onResult(VKAccessToken res) {
                VkUser vkUser = new VkUser(res.accessToken);
                Toast.makeText(getActivity(), "Hello, " + vkUser.getFirstName(), Toast.LENGTH_LONG).show();
                res.saveTokenToFile(App.INSTANCE.getVkTokenFilePath());
                currentUser.setVkUser(vkUser);
                Preference vkAccount = findPreference("vkAccount");
                vkAccount.setSummary(vkUser.getFirstName() + " " + vkUser.getLastName());
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
