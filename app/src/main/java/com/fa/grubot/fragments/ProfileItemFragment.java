package com.fa.grubot.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.fa.grubot.App;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.ProfileItemFragmentBase;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.presenters.ProfilePresenter;
import com.fa.grubot.util.Consts;
import com.fa.grubot.util.ImageLoader;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ni.petrov on 03/04/2018.
 */

public class ProfileItemFragment extends Fragment implements ProfileItemFragmentBase {
    @BindView(R.id.image_profile) ImageView mImage;

    @BindView(R.id.name_profile) EditText mNameEditText;
    @BindView(R.id.name_profile_layout) TextInputLayout mNameEditTextLayout;

    @BindView(R.id.username_profile) EditText mUsernameEditText;
    @BindView(R.id.username_profile_layout) TextInputLayout mUsernameEditTextLayout;

    @BindView(R.id.btn_send_message) Button mSendMessgaeButton;
    @BindView(R.id.progressBar_profile) View mProgressBar;

    private ProfilePresenter mPresenter;

    private int instance = 0;
    private int userId;
    private String userType;

    public static ProfileItemFragment newInstance(int instance, int userId, String userType) {
        Bundle args = new Bundle();
        args.putInt("instance", instance);
        args.putString("userType", userType);
        args.putInt("userId", userId);
        ProfileItemFragment fragment = new ProfileItemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_profile, container, false);
        ButterKnife.bind(this, v);

        userType = getArguments().getString("userType");
        userId = getArguments().getInt("userId");
        instance = getArguments().getInt("instance");

        mPresenter = new ProfilePresenter(this, getActivity());

        init();

        return v;
    }

    private void init() {
        if (checkIfCurrentTypeLoginned()){
            showProgressBar(true);
            if (userType.equals(Consts.VK)){
                mPresenter.requestVkUser(userId);
            } else if (userType.equals(Consts.Telegram)){
                mPresenter.requestTelegramUser(userId);
            }
        }
    }

    private boolean checkIfCurrentTypeLoginned() {
        if ((userType.equals(Consts.VK) && !App.INSTANCE.getCurrentUser().hasVkUser()) ||
                (userType.equals(Consts.Telegram) && !App.INSTANCE.getCurrentUser().hasTelegramUser())){
            showNotLoggedInMessage();
            return false;
        }
        return true;
    }

    private void showNotLoggedInMessage() {
        mUsernameEditTextLayout.setVisibility(View.GONE);
        mSendMessgaeButton.setVisibility(View.GONE);
        mImage.setVisibility(View.INVISIBLE);
        mNameEditText.setText("Вход не выполнен");
        mNameEditTextLayout.setError(null);
    }

    private void showProgressBar(boolean needProgressBar) {
        mImage.setVisibility(needProgressBar ? View.GONE : View.VISIBLE);
        mNameEditTextLayout.setVisibility(needProgressBar ? View.GONE : View.VISIBLE);
        mNameEditText.setVisibility(needProgressBar ? View.GONE : View.VISIBLE);
        mUsernameEditTextLayout.setVisibility(needProgressBar ? View.GONE : View.VISIBLE);
        mUsernameEditText.setVisibility(needProgressBar ? View.GONE : View.VISIBLE);
        mSendMessgaeButton.setVisibility(needProgressBar ? View.GONE : View.VISIBLE);

        mProgressBar.setVisibility(needProgressBar ? View.VISIBLE : View.GONE);
    }

    public void showUser(User user) {
        showProgressBar(false);
        (new ImageLoader(this)).loadImage(mImage, user.getImgUrl());
        mNameEditText.setText(user.getFullname());
        mNameEditTextLayout.setError("Имя пользователя");
        mUsernameEditText.setText(user.getUserName());
        mUsernameEditTextLayout.setError("Логин");
    }
}
