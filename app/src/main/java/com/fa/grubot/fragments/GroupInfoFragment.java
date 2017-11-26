package com.fa.grubot.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.fa.grubot.objects.dashboard.ActionAnnouncement;
import com.fa.grubot.objects.dashboard.ActionVote;
import com.fa.grubot.objects.group.Group;
import com.fa.grubot.objects.misc.VoteOption;
import com.fa.grubot.presenters.GroupInfoPresenter;
import com.fa.grubot.util.Globals;
import com.fa.grubot.util.ImageLoader;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.innodroid.expandablerecycler.ExpandableRecyclerAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.annotations.Nullable;

public class GroupInfoFragment extends Fragment implements GroupInfoFragmentBase, Serializable {

    @Nullable @BindView(R.id.collapsingToolbar) transient Toolbar collapsingToolbar;
    @Nullable @BindView(R.id.app_bar) transient AppBarLayout appBarLayout;
    @Nullable @BindView(R.id.recycler) transient RecyclerView buttonsView;
    @Nullable @BindView(R.id.groupImage) transient ImageView groupImage;

    @Nullable @BindView(R.id.fam) transient FloatingActionMenu fam;
    @Nullable @BindView(R.id.fab_add_announcement) transient FloatingActionButton announcementFab;
    @Nullable @BindView(R.id.fab_add_vote) transient FloatingActionButton voteFab;
    @Nullable @BindView(R.id.retryBtn) transient Button retryBtn;

    @Nullable @BindView(R.id.progressBar) transient ProgressBar progressBar;
    @Nullable @BindView(R.id.content) transient View content;
    @Nullable @BindView(R.id.content_fam) transient View content_fam;
    @Nullable @BindView(R.id.noInternet) transient View noInternet;

    private transient GroupInfoRecyclerAdapter groupInfoAdapter;
    private transient GroupInfoPresenter presenter;
    private transient Unbinder unbinder;

    private int state;
    private Group group;

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
        group = (Group) this.getArguments().getSerializable("group");
        presenter.notifyFragmentStarted(getActivity(), group);
        unbinder = ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    public void showRequiredViews() {
        progressBar.setVisibility(View.GONE);

        switch (state) {
            case Globals.FragmentState.STATE_CONTENT:
                appBarLayout.setExpanded(true);
                content.setVisibility(View.VISIBLE);
                content_fam.setVisibility(View.VISIBLE);
                break;
            case Globals.FragmentState.STATE_NO_INTERNET_CONNECTION:
                appBarLayout.setExpanded(false);
                noInternet.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void showLoadingView() {
        content.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);
        appBarLayout.setExpanded(false);
        progressBar.setVisibility(View.VISIBLE);
    }


    public void setupLayouts(boolean isNetworkAvailable) {
        if (isNetworkAvailable)
            state = Globals.FragmentState.STATE_CONTENT;
        else
            state = Globals.FragmentState.STATE_NO_INTERNET_CONNECTION;
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
        String title = group.getName();

        ImageLoader imageLoader = new ImageLoader(this);
        if (group.getImgURL() != null) {
            imageLoader.loadToolbarImage(groupImage, group.getImgURL());
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

                        if (!desc.toString().isEmpty() && !text.toString().isEmpty()){
                            ActionAnnouncement actionAnnouncement = new ActionAnnouncement(1488, group, "Current User", desc.getText().toString(), new Date(), text.getText().toString());
                            App.INSTANCE.getDataHelper().addNewActionByType(ActionsFragment.TYPE_ANNOUNCEMENTS, actionAnnouncement);
                            groupInfoAdapter.insertItem(actionAnnouncement);
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

            VoteRecyclerAdapter voteAdapter = new VoteRecyclerAdapter(getActivity(), new ArrayList<VoteOption>(Collections.singletonList(new VoteOption())));
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

                for (VoteOption option : options){
                    if (option.getText().isEmpty()){
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
                    ActionVote actionVote = new ActionVote(1488, group, "Current user", desc.getText().toString(), new Date(), options);
                    App.INSTANCE.getDataHelper().addNewActionByType(ActionsFragment.TYPE_VOTES, actionVote);
                    groupInfoAdapter.insertItem(actionVote);
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


        groupInfoAdapter = new GroupInfoRecyclerAdapter(getActivity(), buttons, group.getId());

        groupInfoAdapter.setMode(ExpandableRecyclerAdapter.MODE_ACCORDION);
        buttonsView.setAdapter(groupInfoAdapter);
        groupInfoAdapter.notifyDataSetChanged();
    }

    public void setupRetryButton(){
        retryBtn.setOnClickListener(view -> presenter.onRetryBtnClick(getActivity(), group));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            getActivity().onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setVisibility(View.VISIBLE);
        unbinder.unbind();
        presenter.destroy();
    }
}
