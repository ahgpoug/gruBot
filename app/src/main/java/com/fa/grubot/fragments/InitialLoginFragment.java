package com.fa.grubot.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.fa.grubot.App;
import com.fa.grubot.MainActivity;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.InitialLoginFragmentBase;
import com.fa.grubot.objects.users.VkUser;
import com.fa.grubot.presenters.InitialLoginPresenter;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKServiceActivity;
import com.vk.sdk.api.VKError;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

public class InitialLoginFragment extends Fragment implements InitialLoginFragmentBase {
    @Nullable @BindView(R.id.vkImage) ImageView vkBtn;
    @Nullable @BindView(R.id.telegramImage) ImageView telegramBtn;

    private InitialLoginPresenter presenter;
    private Unbinder unbinder;

    public static InitialLoginFragment newInstance() {
        Bundle args = new Bundle();
        InitialLoginFragment fragment = new InitialLoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        presenter = new InitialLoginPresenter(this);
        View v = inflater.inflate(R.layout.fragment_initial_login, container, false);

        unbinder = ButterKnife.bind(this, v);

        presenter.notifyFragmentStarted();
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.destroy();
    }

    public void setupViews() {
        telegramBtn.setOnClickListener(v -> {
            Fragment telegramLoginFragment = TelegramLoginFragment.newInstance();

            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.addToBackStack(null);
            transaction.replace(R.id.content, telegramLoginFragment);
            transaction.commit();
        });

        vkBtn.setOnClickListener(v -> {
            callVkLogin();
        });
    }

    private void callVkLogin() {
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken vkAccessToken) {
                VkUser vkUser = new VkUser(vkAccessToken.accessToken);
                Toast.makeText(getActivity(), "Hello, " + vkUser.getFirstName(), Toast.LENGTH_LONG).show();
                App.INSTANCE.vkMessenger.setCurrentUserFromVk(vkAccessToken);
                getActivity().startActivity(new Intent(getActivity(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
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
