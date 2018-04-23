package com.fa.grubot.adapters;

import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fa.grubot.R;
import com.fa.grubot.objects.misc.VoteOption;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PollRecyclerAdapter extends RecyclerView.Adapter<PollRecyclerAdapter.ViewHolder>{

    ArrayList<VoteOption> options;

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.voteOptionText) TextView voteOptionText;
        @BindView(R.id.removeOption) ImageView removeOption;
        @BindView(R.id.voteOptionText_layout) TextInputLayout voteOptionTextLayout;

        private ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            voteOptionText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    VoteOption option = new VoteOption();
                    option.setText(charSequence.toString());
                    options.set((int) voteOptionText.getTag(), option);
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            removeOption.setOnClickListener(view1 -> {
                int position = (int) voteOptionText.getTag();
                options.remove(position);
                notifyDataSetChanged();
            });
        }
    }

    public PollRecyclerAdapter(ArrayList<VoteOption> options) {
        this.options = options;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vote_option, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int pos) {
        int position = holder.getAdapterPosition();
        VoteOption option = options.get(position);

        holder.voteOptionText.setTag(position);
        holder.voteOptionText.setText(options.get(position).getText());
        holder.voteOptionTextLayout.setHint("Вариант " + (position + 1));
    }

    public ArrayList<VoteOption> getOptions() {
        return options;
    }

    public void insertOption(VoteOption option) {
        options.add(option);
        notifyItemInserted(options.size() - 1);
    }
    @Override
    public int getItemCount() {
        return options.size();
    }
}