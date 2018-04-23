package com.fa.grubot.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.fa.grubot.R;
import com.fa.grubot.fragments.ProfileItemFragment;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.util.Consts;
import com.fa.grubot.util.Globals;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.ViewHolder>{

    private Context context;
    private ArrayList<User> users;

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.userImage) ImageView userImage;
        @BindView(R.id.userName) TextView userName;
        @BindView(R.id.userRole) TextView userRole;

        private ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public UsersRecyclerAdapter(Context context, ArrayList<User> users) {
        this.context = context;
        this.users = users;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int pos) {
        final int position = holder.getAdapterPosition();
        User user = users.get(position);

        String imgUri = user.getImgUrl();
        if (Globals.ImageMethods.isValidUri(imgUri))
            Glide.with(context).load(imgUri).apply(RequestOptions.circleCropTransform()).into(holder.userImage);
        else
            Glide.with(context).load("").apply(new RequestOptions().placeholder(Globals.ImageMethods.getRoundImage(context, imgUri))).into(holder.userImage);

        holder.userName.setText(user.getFullname());
        holder.userRole.setText(user.getChatRole());

        holder.userImage.getRootView().setOnClickListener(v -> {
            Fragment profileItemFragment = ProfileItemFragment.newInstance(Integer.valueOf(user.getId()), user.getUserType(), user, Consts.PROFILE_MODE_SINGLE);

            FragmentManager fm = ((AppCompatActivity) context).getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.addToBackStack(null);
            transaction.add(R.id.content, profileItemFragment);
            transaction.commit();
        });
    }

    @Override
    public int getItemCount() {
        return (users == null) ? 0 : users.size();
    }


    public ArrayList<User> getItems() {
        return users;
    }
}