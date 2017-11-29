package com.fa.grubot.fragments;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.fa.grubot.App;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.ActionsFragmentBase;
import com.fa.grubot.adapters.ActionsRecyclerAdapter;
import com.fa.grubot.helpers.RecyclerItemTouchHelper;
import com.fa.grubot.objects.dashboard.Action;
import com.fa.grubot.presenters.ActionsPresenter;
import com.fa.grubot.util.Globals;
import com.google.firebase.firestore.DocumentChange;

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
    @Nullable @BindView(R.id.retryBtn) transient Button retryBtn;

    @Nullable @BindView(R.id.root) transient FrameLayout root;

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
        Log.e("mytag", "onCreate");
        presenter = new ActionsPresenter(this);
        setRetainInstance(true);
        View v = inflater.inflate(R.layout.fragment_actions, container, false);

        actionsAdapter = null;
        type = this.getArguments().getInt("type");
        presenter.notifyFragmentStarted(type);
        setHasOptionsMenu(true);
        unbinder = ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.removeRegistration();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.removeRegistration();
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
        retryBtn.setOnClickListener(view -> presenter.onRetryBtnClick(type));
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ActionsRecyclerAdapter.ViewHolder) {
            final Action deletedItem = actions.get(viewHolder.getAdapterPosition());
            presenter.addActionToArchive(deletedItem, type);
        }
    }

    public void showArchiveSnackbar(Action action) {
        Snackbar snackbar;

        if (type == TYPE_ANNOUNCEMENTS)
            snackbar = Snackbar.make(root, "Объявление отправлено в архив", Snackbar.LENGTH_LONG);
        else
            snackbar = Snackbar.make(root, "Голосование отправлено в архив", Snackbar.LENGTH_LONG);

        snackbar.setAction(android.R.string.cancel, view -> {
            presenter.restoreActionFromArchive(action, type);
        });
        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.show();
    }

    public void handleListUpdate(DocumentChange.Type type, int newIndex, int oldIndex, Action action) {
        if (actionsAdapter != null) {
            switch (type) {
                case ADDED:
                    actionsAdapter.addItem(newIndex, action);
                    break;
                case MODIFIED:
                    actionsAdapter.updateItem(oldIndex, newIndex, action);
                    break;
                case REMOVED:
                    actionsAdapter.removeItem(oldIndex);
                    break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.destroy();
    }
}
