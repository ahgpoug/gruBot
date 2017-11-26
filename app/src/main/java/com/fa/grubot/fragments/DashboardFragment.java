package com.fa.grubot.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.fa.grubot.MainActivity;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.DashboardFragmentBase;
import com.fa.grubot.adapters.DashboardRecyclerAdapter;
import com.fa.grubot.objects.dashboard.DashboardItem;
import com.fa.grubot.presenters.DashboardPresenter;
import com.fa.grubot.util.Globals;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.annotations.Nullable;

public class DashboardFragment extends Fragment implements DashboardFragmentBase, Serializable {

    @Nullable @BindView(R.id.retryBtn) transient Button retryBtn;
    @Nullable @BindView(R.id.recycler) transient RecyclerView dashboardView;

    @Nullable @BindView(R.id.progressBar) transient ProgressBar progressBar;
    @Nullable @BindView(R.id.content) transient View content;
    @Nullable @BindView(R.id.noInternet) transient View noInternet;

    private transient Unbinder unbinder;
    private transient DashboardPresenter presenter;

    private int state;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        presenter = new DashboardPresenter(this);
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);

        presenter.notifyFragmentStarted(getActivity());
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
        }
    }
    public void showLoadingView() {
        content.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    public void setupLayouts(boolean isNetworkAvailable) {
        if (isNetworkAvailable)
            state = Globals.FragmentState.STATE_CONTENT;
        else
            state = Globals.FragmentState.STATE_NO_INTERNET_CONNECTION;
    }

    public void setupToolbar() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setTitle("Доска");

        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    public void setupRecyclerView(ArrayList<DashboardItem> items) {
        int spanCount = 1;

        if (getActivity().getResources().getConfiguration().orientation == 2)
            spanCount = 2;

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        dashboardView.setLayoutManager(layoutManager);
        dashboardView.setHasFixedSize(false);

        DashboardRecyclerAdapter groupsAdapter = new DashboardRecyclerAdapter(getActivity(), items);
        dashboardView.setAdapter(groupsAdapter);
        groupsAdapter.notifyDataSetChanged();
    }

    public void setupRetryButton() {
        retryBtn.setOnClickListener(view -> presenter.onRetryBtnClick(getActivity()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.destroy();
    }
}
