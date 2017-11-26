package com.fa.grubot.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fa.grubot.R;
import com.fa.grubot.objects.chat.BranchOfDiscussions;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by ni.petrov on 18/11/2017.
 */

public class BranchesAdapter extends RecyclerView.Adapter<BranchesAdapter.ViewHolder> {

    private ArrayList<BranchOfDiscussions> data;

    public BranchesAdapter(ArrayList<BranchOfDiscussions> data) {
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_branch, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.theme.setText(data.get(position).getTheme());
        holder.author.setText(String.valueOf(data.get(position).getAuthorsId()));
        holder.count.setText(String.valueOf(data.get(position).getMessagesCount()));
        holder.startDate.setText(getFormattedDate(data.get(position).getStartDate().getTimeInMillis()));
        holder.lastDate.setText(getFormattedDate(data.get(position).getLastDate().getTimeInMillis()));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.theme)
        TextView theme;

        @BindView(R.id.author)
        TextView author;

        @BindView(R.id.messages_count)
        TextView count;

        @BindView(R.id.start_date)
        TextView startDate;

        @BindView(R.id.last_date)
        TextView lastDate;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public String getFormattedDate(long smsTimeInMilis) {
        Calendar smsTime = Calendar.getInstance();
        smsTime.setTimeInMillis(smsTimeInMilis);

        Calendar now = Calendar.getInstance();

        final String timeFormatString = "HH:mm:ss";
        final String dateTimeFormatString = "EEEE, d MMMM, HH:mm:ss";
        final long HOURS = 60 * 60 * 60;
        if ((now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) &&
                (now.get(Calendar.HOUR) == smsTime.get(Calendar.HOUR)) &&
                (now.get(Calendar.MINUTE) - smsTime.get(Calendar.MINUTE) < 1)) {
            return "только что";
        } else if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return "Сегодня " + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return "Вчера " + DateFormat.format(timeFormatString, smsTime);
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, smsTime).toString();
        } else {
            return DateFormat.format("MMMM dd yyyy, HH:mm:ss", smsTime).toString();
        }
    }
}
