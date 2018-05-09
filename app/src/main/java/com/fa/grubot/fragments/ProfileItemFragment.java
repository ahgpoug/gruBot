package com.fa.grubot.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.fa.grubot.App;
import com.fa.grubot.ChatActivity;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.ProfileItemFragmentBase;
import com.fa.grubot.helpers.TelegramHelper;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.presenters.ProfilePresenter;
import com.fa.grubot.util.Consts;
import com.fa.grubot.util.ImageLoader;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ni.petrov on 03/04/2018.
 */

public class ProfileItemFragment extends Fragment implements ProfileItemFragmentBase {

    @BindView(R.id.image_profile)
    ImageView mImage;

    @BindView(R.id.name_profile)
    EditText mNameEditText;

    @BindView(R.id.name_profile_layout)
    TextInputLayout mNameEditTextLayout;

    @BindView(R.id.username_profile)
    EditText mUsernameEditText;

    @BindView(R.id.username_profile_layout)
    TextInputLayout mUsernameEditTextLayout;

    @BindView(R.id.phone_profile)
    EditText mPhoneEditText;

    @BindView(R.id.phone_profile_layout)
    TextInputLayout mPhoneEditTextLayout;

    @BindView(R.id.btn_send_message)
    Button mSendMessageButton;

    @BindView(R.id.progressBar_profile)
    View mProgressBar;

    @Nullable
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ProfilePresenter mPresenter;

    private int fragmentMode;

    private int userId;

    private User telegramUser;

    private String userType;

    public static ProfileItemFragment newInstance(int userId, String userType, User telegramUser,
            int fragmentMode) {
        Bundle args = new Bundle();
        args.putString("userType", userType);
        args.putInt("userId", userId);
        args.putSerializable("telegramUser", telegramUser);
        args.putInt("fragmentMode", fragmentMode);
        ProfileItemFragment fragment = new ProfileItemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        userType = getArguments().getString("userType");
        userId = getArguments().getInt("userId");
        telegramUser = (User) getArguments().getSerializable("telegramUser");
        fragmentMode = getArguments().getInt("fragmentMode");

        Log.d("debug", "Profile, user id: " + String.valueOf(userId));

        View v;
        if (fragmentMode == Consts.PROFILE_MODE_DUAL) {
            v = inflater.inflate(R.layout.content_profile, container, false);
        } else {
            v = inflater.inflate(R.layout.content_profile_single, container, false);
            setHasOptionsMenu(true);
        }

        ButterKnife.bind(this, v);
        mPresenter = new ProfilePresenter(this, getActivity());
        init();
        return v;
    }

    private void init() {
        showProgressBar(true);
        if (fragmentMode == Consts.PROFILE_MODE_DUAL) {
            if (checkIfCurrentTypeLoginned()) {
                if (userType.equals(Consts.VK)) {
                    mPresenter.requestVkUser(userId);
                } else if (userType.equals(Consts.Telegram)) {
                    if (telegramUser != null) {
                        showUser(telegramUser);
                    } else {
                        mPresenter.requestTelegramUser(userId);
                    }
                }
            }
        } else if (fragmentMode == Consts.PROFILE_MODE_SINGLE) {
            setupToolbar();
            if (userType.equals(Consts.Telegram)) {
                if (telegramUser != null) {
                    showUser(telegramUser);
                } else {
                    mPresenter.requestTelegramUser(userId);
                }
            } else {
                mPresenter.requestVkUser(userId);
            }
        }
    }

    private boolean checkIfCurrentTypeLoginned() {
        if ((userType.equals(Consts.VK) && !App.INSTANCE.getCurrentUser().hasVkUser()) || (
                userType.equals(Consts.Telegram) && !App.INSTANCE.getCurrentUser()
                        .hasTelegramUser())) {
            showNotLoggedInMessage();
            return false;
        }
        return true;
    }

    private void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
    }

    private void showNotLoggedInMessage() {
        mUsernameEditTextLayout.setVisibility(View.GONE);
        mSendMessageButton.setVisibility(View.GONE);
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
        mPhoneEditTextLayout.setVisibility(needProgressBar ? View.GONE : View.VISIBLE);
        mPhoneEditText.setVisibility(needProgressBar ? View.GONE : View.VISIBLE);

        mSendMessageButton.setVisibility(needProgressBar ? View.GONE : View.VISIBLE);
        mProgressBar.setVisibility(needProgressBar ? View.VISIBLE : View.GONE);
    }

    public void showUser(User user) {
        showProgressBar(false);
        (new ImageLoader(this)).loadImage(mImage, user.getImgUrl());
        mNameEditText.setText(user.getFullname());
        mNameEditTextLayout.setError("Имя пользователя");

        if (user.getUserName() != null) {
            mUsernameEditText.setText(user.getUserName());
            mUsernameEditTextLayout.setError("Логин");
        } else {
            mUsernameEditText.setVisibility(View.GONE);
            mUsernameEditTextLayout.setVisibility(View.GONE);
        }

        if (user.getPhoneNumber() != null) {
            mPhoneEditText.setText(user.getPhoneNumber());
            mPhoneEditTextLayout.setError("Номер телефона");
        } else {
            mPhoneEditText.setVisibility(View.GONE);
            mPhoneEditTextLayout.setVisibility(View.GONE);
        }

        if (fragmentMode == Consts.PROFILE_MODE_SINGLE) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(user.getFullname());
        }

        mSendMessageButton.setOnClickListener(v -> {
            if (userType.equals(Consts.Telegram)) {
                Observable.defer(() -> Observable.just(TelegramHelper.Chats
                        .getChat(App.INSTANCE.getNewTelegramClient(null), getActivity(), user,
                                userId))).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).doOnNext(chat -> {
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("chat", chat);
                    getActivity().startActivity(intent);
                }).subscribe();
            } else if (userType.equals(Consts.VK)) {
                Map<String, Boolean> users = new HashMap<String, Boolean>();
                users.put(telegramUser.getUserId(), false);
                Chat chat = new Chat(telegramUser.getUserId(), telegramUser.getName(), users,
                        telegramUser.getImgUrl(), "null", telegramUser.getUserType(),
                        1525877577710l, "null");
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("chat", chat);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (fragmentMode == Consts.PROFILE_MODE_SINGLE) {
            inflater.inflate(R.menu.menu_group_info, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.closeBtn:
                getActivity().onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
