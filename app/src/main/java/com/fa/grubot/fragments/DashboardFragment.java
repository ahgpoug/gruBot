package com.fa.grubot.fragments;

import android.content.res.Configuration;
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
import com.fa.grubot.util.Consts;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

public class DashboardFragment extends BaseFragment implements DashboardFragmentBase, Serializable {

    @Nullable @BindView(R.id.retryBtn) Button retryBtn;
    @Nullable @BindView(R.id.recycler) RecyclerView dashboardView;

    @Nullable @BindView(R.id.progressBar) ProgressBar progressBar;
    @Nullable @BindView(R.id.content) View content;
    @Nullable @BindView(R.id.noInternet) View noInternet;

    private Unbinder unbinder;
    private DashboardPresenter presenter;
    private DashboardRecyclerAdapter dashboardAdapter;

    private int state;
    private int instance = 0;

    public static DashboardFragment newInstance(int instance) {
        Bundle args = new Bundle();
        args.putInt("instance", instance);
        DashboardFragment fragment = new DashboardFragment();
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
        presenter = new DashboardPresenter(this);
        View v = inflater.inflate(R.layout.fragment_dashboard, container, false);

        instance = this.getArguments().getInt("instance");
        unbinder = ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        dashboardAdapter = null;
        presenter.notifyFragmentStarted();
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
        if (dashboardAdapter != null)
            dashboardAdapter.clearItems();
    }

    public void showRequiredViews() {
        progressBar.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);
        content.setVisibility(View.GONE);

        switch (state) {
            case Consts.STATE_CONTENT:
                content.setVisibility(View.VISIBLE);
                break;
            case Consts.STATE_NO_INTERNET_CONNECTION:
                noInternet.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void setupLayouts(boolean isNetworkAvailable) {
        if (isNetworkAvailable)
            state = Consts.STATE_CONTENT;
        else {
            state = Consts.STATE_NO_INTERNET_CONNECTION;
            dashboardAdapter = null;
        }
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

        dashboardAdapter = new DashboardRecyclerAdapter(getActivity(), instance, fragmentNavigation, items);
        dashboardView.setAdapter(dashboardAdapter);
        dashboardAdapter.notifyDataSetChanged();
    }

    public void setupRetryButton() {
        retryBtn.setOnClickListener(view -> presenter.onRetryBtnClick());
    }

    public void handleListUpdate(int count, int type) {
        if (dashboardAdapter != null) {
            dashboardAdapter.updateItem(count, type);
        }
    }

    public boolean isAdapterExists() {
        return dashboardAdapter != null;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dashboardAdapter = null;
        unbinder.unbind();
        presenter.destroy();
    }
}
