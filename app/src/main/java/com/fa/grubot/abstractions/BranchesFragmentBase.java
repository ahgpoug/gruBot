package com.fa.grubot.abstractions;

import com.fa.grubot.objects.chat.BranchOfDiscussions;

import java.util.ArrayList;

/**
 * Created by ni.petrov on 18/11/2017.
 */

public interface BranchesFragmentBase {

    void setupDataView(ArrayList<BranchOfDiscussions> data);

    int getGroupId();

    void setupSwipeRefreshLayout();

    void stopRefreshAnimation();
}
