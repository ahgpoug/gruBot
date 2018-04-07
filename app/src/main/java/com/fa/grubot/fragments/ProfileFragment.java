package com.fa.grubot.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.fa.grubot.App;
import com.fa.grubot.MainActivity;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.ProfileFragmentBase;
import com.fa.grubot.adapters.ProfileRecyclerAdapter;
import com.fa.grubot.objects.misc.ProfileItem;
import com.fa.grubot.objects.users.CurrentUser;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.presenters.ProfilePresenter;
import com.fa.grubot.util.Consts;
import com.fa.grubot.util.ImageLoader;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.annotations.Nullable;

public class ProfileFragment extends BaseFragment implements ProfileFragmentBase, Serializable {

    @Nullable @BindView(R.id.collapsingToolbar) Toolbar collapsingToolbar;
    @Nullable @BindView(R.id.app_bar) AppBarLayout appBarLayout;
    @Nullable @BindView(R.id.toolbar_layout) CollapsingToolbarLayout toolbarLayout;
    @Nullable @BindView(R.id.retryBtn) Button retryBtn;
    @Nullable @BindView(R.id.userImage) ImageView userImage;
    @Nullable @BindView(R.id.recycler) RecyclerView itemsView;

    @Nullable @BindView(R.id.progressBar)  ProgressBar progressBar;
    @Nullable @BindView(R.id.content)  View content;
    @Nullable @BindView(R.id.noInternet)  View noInternet;

    private  Unbinder unbinder;
    private  ProfilePresenter presenter;

    private  ProfileRecyclerAdapter profileItemsAdapter;

    private int state;
    private int instance = 0;
    private String userId;
    private CurrentUser currentUser;

    public static ProfileFragment newInstance(int instance, CurrentUser currentUser, String userId) {
        Bundle args = new Bundle();
        args.putInt("instance", instance);
        args.putSerializable("currentUser", currentUser);
        args.putString("userId", userId);
        ProfileFragment fragment = new ProfileFragment();
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
        presenter = new ProfilePresenter(this);
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        hideMainToolbar();
        userId = this.getArguments().getString("userId");
        instance = this.getArguments().getInt("instance");

        setHasOptionsMenu(true);
        unbinder = ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.notifyFragmentStarted(userId);
    }

    @Override
    public void onPause() {
        super.onPause();
        terminateRegistration();
    }

    @Override
    public void onStop() {
        super.onStop();
        terminateRegistration();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    private void terminateRegistration() {
        presenter.removeRegistration();
        if (profileItemsAdapter != null)
            profileItemsAdapter.clearItems();
    }

    public void showRequiredViews() {
        progressBar.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);
        content.setVisibility(View.GONE);

        switch (state) {
            case Consts.STATE_CONTENT:
                appBarLayout.setExpanded(true);
                collapsingToolbar.setVisibility(View.VISIBLE);
                content.setVisibility(View.VISIBLE);
                break;
            case Consts.STATE_NO_INTERNET_CONNECTION:
                appBarLayout.setExpanded(false);
                noInternet.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void setupLayouts(boolean isNetworkAvailable) {
        if (isNetworkAvailable)
            state = Consts.STATE_CONTENT;
        else {
            state = Consts.STATE_NO_INTERNET_CONNECTION;
        }
    }

    private void hideMainToolbar() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);

        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    public void setupToolbar(User user) {
        ((AppCompatActivity) getActivity()).setSupportActionBar(collapsingToolbar);
        String title = user.getFullname();

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);

        ImageLoader imageLoader = new ImageLoader(this);
        if (user.getImgUrl() != null) {
            imageLoader.loadToolbarImage(userImage, user.getImgUrl());
        } else {
            imageLoader.loadToolbarImage(userImage, imageLoader.getUriOfDrawable(R.drawable.material_bg));
        }

        if (instance > 0) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    public void setupRecyclerView(ArrayList<ProfileItem> items, User user) {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        itemsView.setLayoutManager(mLayoutManager);
        itemsView.setHasFixedSize(false);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration (
                this.getActivity(),
                mLayoutManager.getOrientation()
        );
        itemsView.addItemDecoration(dividerItemDecoration);

        if (App.INSTANCE.areAnimationsEnabled())
            itemsView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_from_right));

        profileItemsAdapter = new ProfileRecyclerAdapter(getActivity(), items, user);
        itemsView.setAdapter(profileItemsAdapter);
        profileItemsAdapter.notifyDataSetChanged();
    }

    public void setupRetryButton(){
        retryBtn.setOnClickListener(view -> presenter.onRetryBtnClick());
    }

    public void handleProfileUpdate(User user, ArrayList<String> changes) {
        if (profileItemsAdapter != null) {
            if (changes.isEmpty()) {
                ArrayList<ProfileItem> items = new ArrayList<>();
                items.add(new ProfileItem(user.getFullname(), "Имя"));
                items.add(new ProfileItem(user.getUserName(), "Логин"));
                items.add(new ProfileItem(user.getPhoneNumber(), "Номер телефона"));
                items.add(new ProfileItem("", "Описание"));
                profileItemsAdapter.addProfileItems(items);
            } else {
                if (changes.contains("fullname") || changes.contains("avatar")) {
                    toolbarLayout.setTitle(user.getFullname());
                }
                if (changes.contains("avatar")) {
                    ImageLoader imageLoader = new ImageLoader(this);
                    if (user.getImgUrl() != null) {
                        imageLoader.loadToolbarImage(userImage, user.getImgUrl());
                    } else {
                        imageLoader.loadToolbarImage(userImage, imageLoader.getUriOfDrawable(R.drawable.material_bg));
                    }
                }
                if (changes.contains("fullname") || changes.contains("username") || changes.contains("desc") || changes.contains("phoneNumber")) {
                    profileItemsAdapter.updateProfileItems(changes, user);
                }
            }
        }
    }

    public boolean isAdapterExists() {
        return profileItemsAdapter != null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        profileItemsAdapter = null;
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        unbinder.unbind();
        presenter.destroy();
    }
}
