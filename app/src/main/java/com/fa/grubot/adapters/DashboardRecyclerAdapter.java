package com.fa.grubot.adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fa.grubot.App;
import com.fa.grubot.R;
import com.fa.grubot.fragments.ActionsFragment;
import com.fa.grubot.fragments.ActionsTabFragment;
import com.fa.grubot.fragments.BaseFragment;
import com.fa.grubot.objects.dashboard.DashboardAnnouncement;
import com.fa.grubot.objects.dashboard.DashboardArticle;
import com.fa.grubot.objects.dashboard.DashboardItem;
import com.fa.grubot.objects.dashboard.DashboardPoll;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DashboardRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int TYPE_ANNOUNCEMENT = 796;
    private static final int TYPE_POLL = 468;
    private static final int TYPE_ARTICLE = 4698;

    private Context context;
    private int instance;
    private BaseFragment.FragmentNavigation fragmentNavigation;
    private ArrayList<DashboardItem> items;

    public DashboardRecyclerAdapter(Context context, int instance, BaseFragment.FragmentNavigation fragmentNavigation, ArrayList<DashboardItem> items) {
        this.context = context;
        this.instance = instance;
        this.fragmentNavigation = fragmentNavigation;
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
                if (App.INSTANCE.isBackstackEnabled())
                    fragmentNavigation.pushFragment(ActionsTabFragment.newInstance(instance + 1, ActionsFragment.TYPE_ANNOUNCEMENTS));
                else
                    fragmentNavigation.pushFragment(ActionsTabFragment.newInstance(0, ActionsFragment.TYPE_ANNOUNCEMENTS));
            });
        }
    }

    class PollViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.newVotesCount) TextView newVotesCount;
        @BindView(R.id.archiveVotesCount) TextView archiveVotesCount;
        @BindView(R.id.totalVotesCount) TextView totalVotesCount;
        @BindView(R.id.votesView) CardView votesView;

        private PollViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            votesView.setOnClickListener(view1 -> {
                if (App.INSTANCE.isBackstackEnabled())
                    fragmentNavigation.pushFragment(ActionsTabFragment.newInstance(instance + 1, ActionsFragment.TYPE_POLLS));
                else
                    fragmentNavigation.pushFragment(ActionsTabFragment.newInstance(0, ActionsFragment.TYPE_POLLS));
            });
        }
    }

    class ArticleViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.totalArticlesCount) TextView totalArticlesCount;
        @BindView(R.id.articlesView) CardView articlesView;

        private ArticleViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            articlesView.setOnClickListener(view1 -> {
                fragmentNavigation.pushFragment(ActionsFragment.newInstance(ActionsFragment.TYPE_ARTICLES));
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_ANNOUNCEMENT:
                return (new AnnouncementViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_announcement, parent, false)));
            case TYPE_POLL:
                return (new PollViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_vote, parent, false)));
            case TYPE_ARTICLE:
                return (new ArticleViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dashboard_article, parent, false)));
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
                announcementViewHolder.newAnnouncementsCount.setText("Новых: " + announcement.getNewCount());
                announcementViewHolder.archiveAnnouncementsCount.setText("В архиве: " + announcement.getArchiveCount());
                announcementViewHolder.totalAnnouncementsCount.setText("Всего: " + announcement.getTotalCount());
                break;
            case TYPE_POLL:
                PollViewHolder pollViewHolder = (PollViewHolder) holder;
                DashboardPoll vote = (DashboardPoll) item;
                pollViewHolder.newVotesCount.setText("Новых: " + vote.getNewCount());
                pollViewHolder.archiveVotesCount.setText("В архиве: " + vote.getArchiveCount());
                pollViewHolder.totalVotesCount.setText("Всего: " + vote.getTotalCount());
                break;
            case TYPE_ARTICLE:
                ArticleViewHolder articleViewHolder = (ArticleViewHolder) holder;
                DashboardArticle article = (DashboardArticle) item;
                articleViewHolder.totalArticlesCount.setText("Всего: " + article.getTotalCount());
                break;
        }
    }

    public void updateItem(int count, int type) {
        switch (type) {
            case ActionsFragment.TYPE_ANNOUNCEMENTS:
                items.get(0).updateNewCount(count);
                notifyItemChanged(0);
                break;
            case ActionsFragment.TYPE_ANNOUNCEMENTS_ARCHIVE:
                items.get(0).updateArchiveCount(count);
                notifyItemChanged(0);
                break;
            case ActionsFragment.TYPE_POLLS:
                items.get(1).updateNewCount(count);
                notifyItemChanged(1);
                break;
            case ActionsFragment.TYPE_POLLS_ARCHIVE:
                items.get(1).updateArchiveCount(count);
                notifyItemChanged(1);
                break;
            case ActionsFragment.TYPE_ARTICLES:
                items.get(2).updateNewCount(count);
                notifyItemChanged(2);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        DashboardItem item = items.get(position);
        if (item instanceof DashboardAnnouncement)
            return TYPE_ANNOUNCEMENT;
        else if (item instanceof DashboardPoll)
            return TYPE_POLL;
        else if (item instanceof DashboardArticle)
            return TYPE_ARTICLE;
        else
            return -1;
    }

    @Override
    public int getItemCount() {
        return (items == null) ? 0 : items.size();
    }

    public void clearItems() {
        items.clear();
        notifyDataSetChanged();
    }
}