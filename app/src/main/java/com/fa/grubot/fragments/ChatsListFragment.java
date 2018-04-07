package com.fa.grubot.fragments;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;

import com.fa.grubot.App;
import com.fa.grubot.MainActivity;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.ChatsListFragmentBase;
import com.fa.grubot.adapters.ChatsListRecyclerAdapter;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.presenters.ChatsListPresenter;
import com.fa.grubot.util.Consts;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

public class ChatsListFragment extends BaseFragment implements ChatsListFragmentBase, Serializable {

    @Nullable @BindView(R.id.recycler) RecyclerView chatsView;
    @Nullable @BindView(R.id.retryBtn) Button retryBtn;

    @Nullable @BindView(R.id.progressBar) ProgressBar progressBar;
    @Nullable @BindView(R.id.content) View content;
    @Nullable @BindView(R.id.noInternet) View noInternet;
    @Nullable @BindView(R.id.noData) View noData;

    private Unbinder unbinder;
    private ChatsListPresenter presenter;
    private ChatsListRecyclerAdapter chatsListAdapter;

    private int state;
    private int instance = 0;

    public static ChatsListFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt("instance", instance);
        ChatsListFragment fragment = new ChatsListFragment();
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
        View v = inflater.inflate(R.layout.fragment_chats_list, container, false);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setRetainInstance(true);
        presenter = new ChatsListPresenter(this, getActivity());
        setHasOptionsMenu(true);
        unbinder = ButterKnife.bind(this, v);
        instance = this.getArguments().getInt("instance");

        return v;
    }

    @Override
    public void onResume() {
        try {
            presenter.notifyFragmentStarted();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        presenter.setUpdateCallback();
        super.onResume();
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
    public void onDestroy() {
        App.INSTANCE.closeTelegramClient();
        presenter.destroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    public void showRequiredViews() {
        progressBar.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);
        noData.setVisibility(View.GONE);
        content.setVisibility(View.GONE);

        switch (state) {
            case Consts.STATE_CONTENT:
                content.setVisibility(View.VISIBLE);
                break;
            case Consts.STATE_NO_INTERNET_CONNECTION:
                noInternet.setVisibility(View.VISIBLE);
                break;
            case Consts.STATE_NO_DATA:
                noData.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void setupLayouts(boolean isNetworkAvailable, boolean isHasData) {
        if (isNetworkAvailable) {
            if (isHasData)
                state = Consts.STATE_CONTENT;
            else {
                state = Consts.STATE_NO_DATA;
                chatsListAdapter = null;
            }
        }
        else {
            state = Consts.STATE_NO_INTERNET_CONNECTION;
            chatsListAdapter = null;
        }
    }

    public void setupToolbar() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setTitle("Чаты");

        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    public void setupRecyclerView(ArrayList<Chat> chats) {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        chatsView.setLayoutManager(mLayoutManager);
        chatsView.setHasFixedSize(false);

        if (chatsView.getItemDecorationCount() == 0) {
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                    this.getActivity(),
                    mLayoutManager.getOrientation()
            );

            chatsView.addItemDecoration(dividerItemDecoration);
        }

        if (App.INSTANCE.areAnimationsEnabled())
            chatsView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_from_right));
        else
            chatsView.setItemAnimator(null);


        chatsListAdapter = new ChatsListRecyclerAdapter(getActivity(), instance, fragmentNavigation, chats);
        chatsView.setAdapter(chatsListAdapter);
        chatsListAdapter.notifyDataSetChanged();
    }

    public void setupRetryButton() {
        retryBtn.setOnClickListener(view -> presenter.onRetryBtnClick());
    }

    public void updateChatsList(ArrayList<Chat> chats, boolean moveToTop) {
        if (isAdapterExists()) {
            chatsListAdapter.updateChatsList(chats);

            if (moveToTop)
                chatsView.scrollToPosition(0);
        }
    }

    public boolean isListEmpty() {
        return chatsListAdapter == null || chatsListAdapter.getItemCount() == 0;
    }

    public boolean isAdapterExists() {
        return chatsListAdapter != null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        chatsListAdapter = null;
        unbinder.unbind();
        presenter.destroy();
    }
}
