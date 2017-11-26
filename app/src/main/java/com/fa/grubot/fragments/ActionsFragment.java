package com.fa.grubot.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;

import com.fa.grubot.App;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.ActionsFragmentBase;
import com.fa.grubot.adapters.ActionsRecyclerAdapter;
import com.fa.grubot.helpers.RecyclerItemTouchHelper;
import com.fa.grubot.objects.dashboard.Action;
import com.fa.grubot.objects.dashboard.ActionAnnouncement;
import com.fa.grubot.presenters.ActionsPresenter;
import com.fa.grubot.util.Globals;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.annotations.Nullable;

public class ActionsFragment extends Fragment implements ActionsFragmentBase, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, Serializable {
    public static final int TYPE_ANNOUNCEMENTS = 389;
    public static final int TYPE_VOTES = 827;
    public static final int TYPE_ANNOUNCEMENTS_ARCHIVE = 390;
    public static final int TYPE_VOTES_ARCHIVE = 828;

    @Nullable @BindView(R.id.recycler) transient  RecyclerView actionsView;
    @Nullable @BindView(R.id.swipeRefreshLayout) transient SwipeRefreshLayout swipeRefreshLayout;
    @Nullable @BindView(R.id.swipeRefreshLayoutNoData) transient SwipeRefreshLayout swipeRefreshLayoutNoData;
    @Nullable @BindView(R.id.retryBtn) transient Button retryBtn;

    @Nullable @BindView(R.id.progressBar) transient ProgressBar progressBar;
    @Nullable @BindView(R.id.content) transient View content;
    @Nullable @BindView(R.id.noInternet) transient View noInternet;
    @Nullable @BindView(R.id.noData) transient View noData;

    private transient Unbinder unbinder;
    private transient ActionsPresenter presenter;
    private ArrayList<Action> actions;
    private transient ActionsRecyclerAdapter actionsAdapter;

    private int state;
    private int type;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        presenter = new ActionsPresenter(this);
        View v = inflater.inflate(R.layout.fragment_actions, container, false);

        type = this.getArguments().getInt("type");
        setHasOptionsMenu(true);
        unbinder = ButterKnife.bind(this, v);
        presenter.notifyFragmentStarted(getActivity(), type);

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.destroyDrawingCache();
            swipeRefreshLayout.clearAnimation();
        } else {
            swipeRefreshLayoutNoData.setRefreshing(false);
            swipeRefreshLayoutNoData.destroyDrawingCache();
            swipeRefreshLayoutNoData.clearAnimation();
        }
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

    public void setupLayouts(boolean isNetworkAvailable, boolean isHasData){
        if (isNetworkAvailable) {
            if (isHasData)
                state = Globals.FragmentState.STATE_CONTENT;
            else
                state = Globals.FragmentState.STATE_NO_DATA;
        }
        else
            state = Globals.FragmentState.STATE_NO_INTERNET_CONNECTION;
    }

    public void setupSwipeRefreshLayout() {
        if (state == Globals.FragmentState.STATE_NO_DATA) {
            swipeRefreshLayoutNoData.setColorSchemeResources(R.color.blue, R.color.purple, R.color.green, R.color.orange);
            swipeRefreshLayoutNoData.setOnRefreshListener(() -> {
                presenter.onRefresh(getActivity(), type);
                onItemsLoadComplete();
            });
        } else {
            swipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.purple, R.color.green, R.color.orange);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                presenter.onRefresh(getActivity(), type);
                onItemsLoadComplete();
            });
        }
    }

    public void setupRecyclerView(ArrayList<Action> newActions) {
        int spanCount = 1;

        if (getActivity().getResources().getConfiguration().orientation == 2)
            spanCount = 2;

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);

        actionsView.setLayoutManager(layoutManager);
        actionsView.setItemAnimator(new DefaultItemAnimator());
        actionsView.setHasFixedSize(false);

        if (type == TYPE_ANNOUNCEMENTS || type == TYPE_VOTES) {
            ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
            new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(actionsView);
        }

        if (App.INSTANCE.areAnimationsEnabled())
            actionsView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_from_bottom));

        this.actions = newActions;
        actionsAdapter = new ActionsRecyclerAdapter(getActivity(), newActions);
        actionsView.setAdapter(actionsAdapter);
        actionsAdapter.notifyDataSetChanged();
    }

    public void setupRetryButton() {
        retryBtn.setOnClickListener(view -> presenter.onRetryBtnClick(getActivity(), type));
    }

    private void onItemsLoadComplete() {
        if (state == Globals.FragmentState.STATE_NO_DATA)
            swipeRefreshLayoutNoData.setRefreshing(false);
        else
            swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ActionsRecyclerAdapter.ViewHolder) {
            final Action deletedItem = actions.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            App.INSTANCE.getDataHelper().addActionToArchive(type, deletedItem);
            actionsAdapter.removeItem(viewHolder.getAdapterPosition());

            Snackbar snackbar;
            if (deletedItem instanceof ActionAnnouncement) {
                snackbar = Snackbar.make(swipeRefreshLayout, "Объявление отправлено в архив", Snackbar.LENGTH_LONG);
            } else {
                snackbar = Snackbar.make(swipeRefreshLayout, "Голосование отправлено в архив", Snackbar.LENGTH_LONG);
            }

            snackbar.setAction(android.R.string.cancel, view -> {
                App.INSTANCE.getDataHelper().restoreActionFromArchive(type, deletedItem, deletedIndex);
                actionsAdapter.restoreItem(deletedItem, deletedIndex);
                actionsView.smoothScrollToPosition(deletedIndex);
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.destroy();
    }
}
