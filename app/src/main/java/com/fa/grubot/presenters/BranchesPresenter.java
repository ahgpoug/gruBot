package com.fa.grubot.presenters;

import com.fa.grubot.abstractions.BranchesFragmentBase;
import com.fa.grubot.models.BranchesModel;

/**
 * Created by ni.petrov on 18/11/2017.
 */

public class BranchesPresenter {
    private BranchesFragmentBase fragment;

    private BranchesModel model;

    public BranchesPresenter(BranchesFragmentBase fragment) {
        this.fragment = fragment;
        model = new BranchesModel();
    }

    public void destroy() {
        fragment = null;
        model = null;
    }

    public void notifyViewCreated(){
        refreshData();
        fragment.setupSwipeRefreshLayout();
    }

    public void refreshData() {
        fragment.setupDataView(model.loadBranches(fragment.getGroupId()));
        fragment.stopRefreshAnimation();
    }
}
