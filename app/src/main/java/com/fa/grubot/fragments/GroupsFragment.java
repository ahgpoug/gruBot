package com.fa.grubot.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.fa.grubot.abstractions.GroupsFragmentBase;
import com.fa.grubot.adapters.GroupsRecyclerAdapter;
import com.fa.grubot.objects.group.Group;
import com.fa.grubot.presenters.GroupsPresenter;
import com.fa.grubot.util.Globals;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.annotations.Nullable;

public class GroupsFragment extends Fragment implements GroupsFragmentBase, Serializable {

    @Nullable @BindView(R.id.recycler) transient RecyclerView groupsView;
    @Nullable @BindView(R.id.swipeRefreshLayout) transient SwipeRefreshLayout swipeRefreshLayout;
    @Nullable @BindView(R.id.swipeRefreshLayoutNoData) transient SwipeRefreshLayout swipeRefreshLayoutNoData;
    @Nullable @BindView(R.id.retryBtn) transient Button retryBtn;

    @Nullable @BindView(R.id.progressBar) transient ProgressBar progressBar;
    @Nullable @BindView(R.id.content) transient View content;
    @Nullable @BindView(R.id.noInternet) transient View noInternet;
    @Nullable @BindView(R.id.noData) transient View noData;

    private transient Unbinder unbinder;
    private transient GroupsPresenter presenter;

    private int state;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        presenter = new GroupsPresenter(this);
        View v = inflater.inflate(R.layout.fragment_groups, container, false);

        presenter.notifyFragmentStarted(getActivity());
        setHasOptionsMenu(true);
        unbinder = ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    public void showRequiredViews() {
        progressBar.setVisibility(View.GONE);

        switch (state) {
            case Globals.FragmentState.STATE_CONTENT:
                content.setVisibility(View.VISIBLE);
                break;
            case Globals.FragmentState.STATE_NO_INTERNET_CONNECTION:
                noInternet.setVisibility(View.VISIBLE);
                break;
            case Globals.FragmentState.STATE_NO_DATA:
                noData.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void showLoadingView() {
        content.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);
        noData.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }


    public void setupLayouts(boolean isNetworkAvailable, boolean isHasData) {
        if (isNetworkAvailable) {
            if (isHasData)
                state = Globals.FragmentState.STATE_CONTENT;
            else
                state = Globals.FragmentState.STATE_NO_DATA;
        }
        else
            state = Globals.FragmentState.STATE_NO_INTERNET_CONNECTION;
    }

    public void setupToolbar() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setTitle("Чаты");

        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    public void setupSwipeRefreshLayout() {
        if (state == Globals.FragmentState.STATE_NO_DATA) {
            swipeRefreshLayoutNoData.setColorSchemeResources(R.color.blue, R.color.purple, R.color.green, R.color.orange);
            swipeRefreshLayoutNoData.setOnRefreshListener(() -> {
                presenter.onRefresh(getActivity());
                onItemsLoadComplete();
            });
        } else {
            swipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.purple, R.color.green, R.color.orange);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                presenter.onRefresh(getActivity());
                onItemsLoadComplete();
            });
        }
    }

    public void setupRecyclerView(ArrayList<Group> groups) {
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        groupsView.setLayoutManager(mLayoutManager);
        groupsView.setHasFixedSize(false);

        if (groupsView.getItemDecorationCount() == 0) {
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                    this.getActivity(),
                    mLayoutManager.getOrientation()
            );

            groupsView.addItemDecoration(dividerItemDecoration);
        }

        if (App.INSTANCE.areAnimationsEnabled())
            groupsView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_from_right));

        GroupsRecyclerAdapter groupsAdapter = new GroupsRecyclerAdapter(getActivity(), groups);
        groupsView.setAdapter(groupsAdapter);
        groupsAdapter.notifyDataSetChanged();
    }

    public void setupRetryButton() {
        retryBtn.setOnClickListener(view -> presenter.onRetryBtnClick(getActivity()));
    }

    private void onItemsLoadComplete() {
        if (state == Globals.FragmentState.STATE_NO_DATA)
            swipeRefreshLayoutNoData.setRefreshing(false);
        else
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.destroy();
    }
}
