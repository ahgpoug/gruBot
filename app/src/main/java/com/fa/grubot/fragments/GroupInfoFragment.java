package com.fa.grubot.fragments;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fa.grubot.App;
import com.fa.grubot.MainActivity;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.GroupInfoFragmentBase;
import com.fa.grubot.adapters.GroupInfoRecyclerAdapter;
import com.fa.grubot.adapters.VoteRecyclerAdapter;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.dashboard.Action;
import com.fa.grubot.objects.misc.VoteOption;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.presenters.GroupInfoPresenter;
import com.fa.grubot.util.Consts;
import com.fa.grubot.util.ImageLoader;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.innodroid.expandablerecycler.ExpandableRecyclerAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.annotations.Nullable;

public class GroupInfoFragment extends BaseFragment implements GroupInfoFragmentBase, Serializable {

    @Nullable @BindView(R.id.collapsingToolbar) Toolbar collapsingToolbar;
    @Nullable @BindView(R.id.app_bar) AppBarLayout appBarLayout;
    @Nullable @BindView(R.id.recycler) RecyclerView buttonsView;
    @Nullable @BindView(R.id.chatImage) ImageView groupImage;

    @Nullable @BindView(R.id.fam) FloatingActionMenu fam;
    @Nullable @BindView(R.id.fab_add_announcement) FloatingActionButton announcementFab;
    @Nullable @BindView(R.id.fab_add_vote) FloatingActionButton voteFab;
    @Nullable @BindView(R.id.retryBtn) Button retryBtn;

    @Nullable @BindView(R.id.progressBar) ProgressBar progressBar;
    @Nullable @BindView(R.id.content) View content;
    @Nullable @BindView(R.id.content_fam) View content_fam;
    @Nullable @BindView(R.id.noInternet) View noInternet;

    private GroupInfoRecyclerAdapter groupInfoAdapter;
    private GroupInfoPresenter presenter;
    private Unbinder unbinder;

    private int state;
    private int instance = 0;
    private Chat chat;

    public static GroupInfoFragment newInstance(int instance, Chat chat) {
        Bundle args = new Bundle();
        args.putInt("instance", instance);
        args.putSerializable("chat", chat);
        GroupInfoFragment fragment = new GroupInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        presenter = new GroupInfoPresenter(this);
        View v = inflater.inflate(R.layout.fragment_group_info, container, false);

        hideMainToolbar();
        setHasOptionsMenu(true);

        chat = (Chat) this.getArguments().getSerializable("chat");
        instance = this.getArguments().getInt("instance");
        unbinder = ButterKnife.bind(this, v);

        presenter.notifyFragmentStarted(chat);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.notifyFragmentStarted(chat);
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
        if (groupInfoAdapter != null)
            groupInfoAdapter.clearItems();
    }

    public void showRequiredViews() {
        progressBar.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);
        content.setVisibility(View.GONE);

        switch (state) {
            case Consts.STATE_CONTENT:
                appBarLayout.setExpanded(true);
                content.setVisibility(View.VISIBLE);
                content_fam.setVisibility(View.VISIBLE);
                break;
            case Consts.STATE_NO_INTERNET_CONNECTION:
                appBarLayout.setExpanded(false);
                noInternet.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void setupLayouts(boolean isNetworkAvailable) {
        if (isNetworkAvailable)
            state = Consts.STATE_CONTENT;
        else {
            groupInfoAdapter = null;
            state = Consts.STATE_NO_INTERNET_CONNECTION;
        }
    }

    private void hideMainToolbar() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);

        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    public void setupToolbar() {
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.GONE);
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        ((MainActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);

        ((AppCompatActivity) getActivity()).setSupportActionBar(collapsingToolbar);
        String title = chat.getName();

        ImageLoader imageLoader = new ImageLoader(this);
        if (chat.getImgURI() != null) {
            imageLoader.loadToolbarImage(groupImage, chat.getImgURI());
        } else {
            imageLoader.loadToolbarImage(groupImage, imageLoader.getUriOfDrawable(R.drawable.material_flat));
        }

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(title);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void setupFab(){
        fam.setClosedOnTouchOutside(true);
        announcementFab.setOnClickListener(view -> {
            groupInfoAdapter.collapseAll(); //TODO не баг, а фича. Если убрать все сломается и мне сейчас лень это фиксить, когда можно просто написать эту строку. Если кто-то это прочитает, то ёбните меня.
            new MaterialDialog.Builder(getActivity())
                    .title("Объявление")
                    .customView(R.layout.dialog_add_announcement, false)
                    .canceledOnTouchOutside(false)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .onPositive((dialog, which) -> {
                        EditText desc = (EditText) dialog.findViewById(R.id.announcementDesc);
                        EditText text = (EditText) dialog.findViewById(R.id.announcementText);

                        if (!desc.toString().isEmpty() && !text.toString().isEmpty()) {
                            MaterialDialog progress = new MaterialDialog.Builder(getActivity())
                                    .content("Пожалуйста, подождите")
                                    .progress(true, 0)
                                    .cancelable(false)
                                    .show();

                            HashMap<String, Object> announcement = new HashMap<>();
                            announcement.put("chat", chat.getId());
                            announcement.put("groupName", chat.getName());
                            announcement.put("author", App.INSTANCE.getCurrentUser().getTelegramUser().getId());
                            announcement.put("authorName", App.INSTANCE.getCurrentUser().getTelegramUser().getFirstName() + " " + App.INSTANCE.getCurrentUser().getTelegramUser().getLastName());
                            announcement.put("desc", desc.getText().toString());
                            announcement.put("date", new Date());
                            announcement.put("text", text.getText().toString());
                            HashMap<String, String> users = new HashMap<>();
                            for (Map.Entry<String, Boolean> user : chat.getUsers().entrySet())
                                users.put(user.getKey(), "new");
                            announcement.put("users", users);

                            FirebaseFirestore.getInstance().collection("announcements")
                                    .add(announcement)
                                    .addOnSuccessListener(aVoid -> {
                                        progress.dismiss();
                                        Toast.makeText(getActivity().getApplicationContext(), "Успешно добавлено", Toast.LENGTH_LONG).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        progress.dismiss();
                                        Toast.makeText(getActivity().getApplicationContext(), "Ошибка добавления", Toast.LENGTH_LONG).show();
                                    });
                        }

                        fam.close(true);
                    })
                    .show();
        });

        voteFab.setOnClickListener(view -> {
            groupInfoAdapter.collapseAll(); //TODO не баг, а фича. Если убрать все сломается и мне сейчас лень это фиксить, когда можно просто написать эту строку. Если кто-то это прочитает, то ёбните меня.

            MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity())
                    .title("Голосование")
                    .customView(R.layout.dialog_add_vote, false)
                    .canceledOnTouchOutside(false)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .autoDismiss(false)
                    .neutralText("+ вариант")
                    .onNegative((dialog, which) -> dialog.dismiss())
                    .build();
            RecyclerView voteRecycler = materialDialog.getView().findViewById(R.id.vote_recycler);
            EditText desc = (EditText) materialDialog.getView().findViewById(R.id.voteDesc);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

            VoteRecyclerAdapter voteAdapter = new VoteRecyclerAdapter(new ArrayList<>());
            voteRecycler.setLayoutManager(mLayoutManager);
            voteRecycler.setHasFixedSize(false);

            voteRecycler.setAdapter(voteAdapter);
            voteAdapter.notifyDataSetChanged();

            MaterialDialog.SingleButtonCallback neutralCallback = (dialog, which) -> {
                voteAdapter.insertOption(new VoteOption());
                voteRecycler.smoothScrollToPosition(voteAdapter.getItemCount() - 1);
            };

            MaterialDialog.SingleButtonCallback positiveCallback = (dialog, which) -> {
                boolean hasEmpty = false;
                ArrayList<VoteOption> options = voteAdapter.getOptions();

                for (VoteOption option : options) {
                    if (option.getText().isEmpty()) {
                        hasEmpty = true;
                        break;
                    }
                }
                if (desc.getText().toString().isEmpty())
                    Toast.makeText(getActivity(), "Описание должно быть заполнено", Toast.LENGTH_SHORT).show();
                else
                if (options.size() < 2)
                    Toast.makeText(getActivity(), "Должно быть не менее двух вариантов выбора", Toast.LENGTH_SHORT).show();
                else
                if (hasEmpty)
                    Toast.makeText(getActivity(), "Все варианты выбора должны быть заполнены", Toast.LENGTH_SHORT).show();
                else {
                    MaterialDialog progress = new MaterialDialog.Builder(getActivity())
                            .content("Пожалуйста, подождите")
                            .progress(true, 0)
                            .cancelable(false)
                            .show();

                    HashMap<String, Object> vote = new HashMap<>();
                    vote.put("chat", chat.getId());
                    vote.put("groupName", chat.getName());
                    vote.put("author", App.INSTANCE.getCurrentUser().getTelegramUser().getId());
                    vote.put("authorName", App.INSTANCE.getCurrentUser().getTelegramUser().getFirstName() + " " + App.INSTANCE.getCurrentUser().getTelegramUser().getLastName());
                    vote.put("desc", desc.getText().toString());
                    vote.put("date", new Date());

                    HashMap<String, String> users = new HashMap<>();
                    for (Map.Entry<String, Boolean> user : chat.getUsers().entrySet())
                        users.put(user.getKey(), "new");
                    vote.put("users", users);

                    HashMap<String, String> voteOptions = new HashMap<>();
                    for (int i = 0; i < options.size(); i++) {
                        voteOptions.put(String.valueOf(i), options.get(i).getText());
                    }
                    vote.put("voteOptions", voteOptions);

                    FirebaseFirestore.getInstance().collection("votes")
                            .add(vote)
                            .addOnSuccessListener(aVoid -> {
                                progress.dismiss();
                                Toast.makeText(getActivity().getApplicationContext(), "Успешно добавлено", Toast.LENGTH_LONG).show();
                            })
                            .addOnFailureListener(e -> {
                                progress.dismiss();
                                Toast.makeText(getActivity().getApplicationContext(), "Ошибка добавления", Toast.LENGTH_LONG).show();
                            });
                    dialog.dismiss();
                    fam.close(true);
                }
            };
            materialDialog.getBuilder().onNeutral(neutralCallback).onPositive(positiveCallback);

            materialDialog.show();
        });
    }

    public void setupRecyclerView(ArrayList<GroupInfoRecyclerAdapter.GroupInfoRecyclerItem> buttons){
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        buttonsView.setLayoutManager(mLayoutManager);
        buttonsView.setHasFixedSize(false);

        if (App.INSTANCE.areAnimationsEnabled())
            buttonsView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_from_bottom));

        groupInfoAdapter = new GroupInfoRecyclerAdapter(getActivity(), instance, fragmentNavigation, buttons, chat.getId());

        groupInfoAdapter.setMode(ExpandableRecyclerAdapter.MODE_ACCORDION);
        buttonsView.setAdapter(groupInfoAdapter);
        groupInfoAdapter.notifyDataSetChanged();
    }

    public void setupRetryButton(){
        retryBtn.setOnClickListener(view -> presenter.onRetryBtnClick());
    }

    public void handleActionsUpdate(DocumentChange.Type type, int newIndex, int oldIndex, Action action) {
        if (groupInfoAdapter != null) {
            switch (type) {
                case ADDED:
                    groupInfoAdapter.addActionItem(newIndex, action);
                    break;
                case MODIFIED:
                    //groupInfoAdapter.updateItem(oldIndex, newIndex, chat);
                    break;
                case REMOVED:
                    //groupInfoAdapter.removeItem(oldIndex);
                    break;
            }
        }
    }

    public void handleUsersUpdate(DocumentChange.Type type, int newIndex, int oldIndex, User user) {

    }

    public void handleUIUpdate(Chat chat) {

    }

    public boolean isAdapterExists() {
        return groupInfoAdapter != null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        groupInfoAdapter = null;
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        unbinder.unbind();
        presenter.destroy();
    }
}
