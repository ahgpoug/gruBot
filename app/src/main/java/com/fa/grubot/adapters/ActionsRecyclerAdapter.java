package com.fa.grubot.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fa.grubot.R;
import com.fa.grubot.objects.dashboard.Action;
import com.fa.grubot.objects.dashboard.ActionAnnouncement;
import com.fa.grubot.objects.dashboard.ActionVote;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ActionsRecyclerAdapter extends RecyclerView.Adapter<ActionsRecyclerAdapter.ViewHolder>{

    private Context context;
    private final ArrayList<Action> entries;

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.actionTypeText) TextView actionTypeText;
        @BindView(R.id.actionDate) TextView actionDate;
        @BindView(R.id.actionGroup) TextView actionGroup;
        @BindView(R.id.actionAuthor) TextView actionAuthor;
        @BindView(R.id.actionDesc) TextView actionDesc;

        @BindView(R.id.view_background) RelativeLayout viewBackground;
        public @BindView(R.id.view_foreground) RelativeLayout viewForeground;


        private ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public ActionsRecyclerAdapter(Context context, ArrayList<Action> entries) {
        this.context = context;
        this.entries = entries;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_action, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int pos) {
        final int position = holder.getAdapterPosition();
        Action action = entries.get(position);

        holder.actionDate.setText(action.getDate());
        holder.actionAuthor.setText(action.getAuthor());
        holder.actionDesc.setText(action.getDesc());
        holder.actionGroup.setText(action.getGroup().getName());
        holder.viewForeground.setBackgroundColor(getColorFromDashboardEntry(action));

        if (action instanceof ActionAnnouncement) {
            holder.actionTypeText.setText("Объявление");
            holder.viewForeground.setOnClickListener(v -> {
                new MaterialDialog.Builder(context)
                        .title(action.getGroup().getName() + ": " + action.getDesc())
                        .content(((ActionAnnouncement) action).getText())
                        .positiveText(android.R.string.ok)
                        .show();
            });
        } else {
            holder.actionTypeText.setText("Голосование");
            holder.viewForeground.setOnClickListener(v -> {
                new MaterialDialog.Builder(context)
                        .title(action.getGroup().getName() + ": " + action.getDesc())
                        .items(((ActionVote) action).getOptions())
                        .itemsCallbackSingleChoice(-1, (MaterialDialog.ListCallbackSingleChoice) (dialog, view, which, text) -> {
                            /**
                             * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                             * returning false here won't allow the newly selected radio button to actually be selected.
                             **/
                            return true;
                        })
                        .positiveText(android.R.string.ok)
                        .show();
            });
        }
    }

    public void removeItem(int position) {
        entries.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Action entry, int position) {
        entries.add(position, entry);
        notifyItemInserted(position);
    }

    @Override
    public int getItemCount() {
        return (entries == null) ? 0 : entries.size();
    }

    private int getColorFromDashboardEntry(Action entry){
        if (entry instanceof ActionAnnouncement)
            return context.getResources().getColor(R.color.colorAnnouncement);
        else
            return context.getResources().getColor(R.color.colorVote);
    }
}