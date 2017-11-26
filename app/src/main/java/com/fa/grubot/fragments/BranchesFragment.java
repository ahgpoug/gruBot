package com.fa.grubot.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.fa.grubot.App;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.BranchesFragmentBase;
import com.fa.grubot.adapters.BranchesAdapter;
import com.fa.grubot.objects.chat.BranchOfDiscussions;
import com.fa.grubot.presenters.BranchesPresenter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by ni.petrov on 18/11/2017.
 */

public class BranchesFragment extends Fragment implements BranchesFragmentBase {

    private Unbinder unbinder;

    private BranchesPresenter presenter;

    private int groupId;

    @BindView(R.id.branches_recycler)
    RecyclerView recyclerView;

    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.data_empty)
    TextView emptyDataText;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        presenter = new BranchesPresenter(this);

        View v = inflater.inflate(R.layout.fragment_branches, container, false);

        groupId = this.getArguments().getInt("groupId");

        unbinder = ButterKnife.bind(this, v);

        presenter.notifyViewCreated();

        return v;
    }

    @Override
    public void setupDataView(ArrayList<BranchOfDiscussions> data) {
        if (!data.isEmpty()) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyDataText.setVisibility(View.GONE);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(false);

            if (App.INSTANCE.areAnimationsEnabled())
                recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_from_right));

            BranchesAdapter adapter = new BranchesAdapter(data);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyDataText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getGroupId() {
        return groupId;
    }

    @Override
    public void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setColorSchemeResources(R.color.blue, R.color.purple, R.color.green, R.color.orange);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            presenter.refreshData();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        presenter.destroy();
    }

    @Override
    public void stopRefreshAnimation() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
