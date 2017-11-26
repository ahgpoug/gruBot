package com.fa.grubot.adapters;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fa.grubot.MainActivity;
import com.fa.grubot.R;
import com.fa.grubot.fragments.ActionsFragment;
import com.fa.grubot.fragments.ActionsTabFragment;
import com.fa.grubot.objects.dashboard.DashboardAnnouncement;
import com.fa.grubot.objects.dashboard.DashboardItem;
import com.fa.grubot.objects.dashboard.DashboardVote;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DashboardRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int TYPE_ANNOUNCEMENT = 796;
    private static final int TYPE_VOTE = 468;

    private Context context;
    private ArrayList<DashboardItem> items;

    public DashboardRecyclerAdapter(Context context, ArrayList<DashboardItem> items) {
        this.context = context;
        this.items = items;
    }

    class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.newAnnouncementsCount) TextView newAnnouncementsCount;
        @BindView(R.id.archiveAnnouncementsCount) TextView archiveAnnouncementsCount;
        @BindView(R.id.totalAnnouncementsCount) TextView totalAnnouncementsCount;
        @BindView(R.id.announcementsView) CardView announcementsView;

        private AnnouncementViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            announcementsView.setOnClickListener(view1 -> {
                Fragment fragment = new ActionsTabFragment();
                Bundle args = new Bundle();
                args.putInt("type", ActionsFragment.TYPE_ANNOUNCEMENTS);
                fragment.setArguments(args);
                ((MainActivity)context).pushFragments(MainActivity.TAB_DASHBOARD, fragment,true);
            });
        }
    }

    class VoteViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.newVotesCount) TextView newVotesCount;
        @BindView(R.id.archiveVotesCount) TextView archiveVotesCount;
        @BindView(R.id.totalVotesCount) TextView totalVotesCount;
        @BindView(R.id.votesView) CardView votesView;

        private VoteViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            votesView.setOnClickListener(view1 -> {
                Fragment fragment = new ActionsTabFragment();
                Bundle args = new Bundle();
                args.putInt("type", ActionsFragment.TYPE_VOTES);
                fragment.setArguments(args);
                ((MainActivity)context).pushFragments(MainActivity.TAB_DASHBOARD, fragment,true);
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ANNOUNCEMENT:
                return (new AnnouncementViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_announcement, parent, false)));
            case TYPE_VOTE:
                return (new VoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_vote, parent, false)));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int pos) {
        int position = holder.getAdapterPosition();
        DashboardItem item = items.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_ANNOUNCEMENT:
                AnnouncementViewHolder announcementViewHolder = (AnnouncementViewHolder) holder;
                DashboardAnnouncement announcement = (DashboardAnnouncement) item;
                announcementViewHolder.newAnnouncementsCount.setText("Новых: " + announcement.getNewAnnouncementsCount());
                announcementViewHolder.archiveAnnouncementsCount.setText("В архиве: " + announcement.getArchiveAnnouncementsCount());
                announcementViewHolder.totalAnnouncementsCount.setText("Всего: " + announcement.getTotalAnnouncementsCount());
                break;
            case TYPE_VOTE:
                VoteViewHolder voteViewHolder = (VoteViewHolder) holder;
                DashboardVote vote = (DashboardVote) item;
                voteViewHolder.newVotesCount.setText("Новых: " + vote.getNewVotesCount());
                voteViewHolder.archiveVotesCount.setText("В архиве: " + vote.getArchiveVotesCount());
                voteViewHolder.totalVotesCount.setText("Всего: " + vote.getTotalVotesCount());
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        DashboardItem item = items.get(position);
        if (item instanceof DashboardAnnouncement)
            return TYPE_ANNOUNCEMENT;
        else
            return TYPE_VOTE;
    }

    @Override
    public int getItemCount() {
        return (items == null) ? 0 : items.size();
    }
}