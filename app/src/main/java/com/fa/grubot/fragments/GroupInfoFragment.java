package com.fa.grubot.fragments;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fa.grubot.App;
import com.fa.grubot.R;
import com.fa.grubot.abstractions.GroupInfoFragmentBase;
import com.fa.grubot.adapters.ActionsRecyclerAdapter;
import com.fa.grubot.adapters.PollRecyclerAdapter;
import com.fa.grubot.adapters.UsersRecyclerAdapter;
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

import net.cachapa.expandablelayout.ExpandableLayout;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import io.reactivex.annotations.Nullable;

public class GroupInfoFragment extends BaseFragment implements GroupInfoFragmentBase, Serializable {

    @Nullable @BindView(R.id.collapsingToolbar) Toolbar collapsingToolbar;
    @Nullable @BindView(R.id.app_bar) AppBarLayout appBarLayout;
    @Nullable @BindView(R.id.chatImage) ImageView groupImage;

    @Nullable @BindView(R.id.announcementsRecycler) RecyclerView announcementsRecycler;
    @Nullable @BindView(R.id.pollsRecycler) RecyclerView pollsRecycler;
    @Nullable @BindView(R.id.articlesRecycler) RecyclerView articlesRecycler;
    @Nullable @BindView(R.id.participantsRecycler) RecyclerView participantsRecycler;

    @Nullable @BindView(R.id.announcementsBtn) TextView announcementsBtn;
    @Nullable @BindView(R.id.pollsBtn) TextView pollsBtn;
    @Nullable @BindView(R.id.articlesBtn) TextView articlesBtn;
    @Nullable @BindView(R.id.participantsBtn) TextView participantsBtn;

    @Nullable @BindView(R.id.announcementsLayout) ExpandableLayout announcementsLayout;
    @Nullable @BindView(R.id.pollsLayout) ExpandableLayout pollsLayout;
    @Nullable @BindView(R.id.articlesLayout) ExpandableLayout articlesLayout;
    @Nullable @BindView(R.id.participantsLayout) ExpandableLayout participantsLayout;

    @Nullable @BindView(R.id.fam) FloatingActionMenu fam;
    @Nullable @BindView(R.id.fab_add_announcement) FloatingActionButton announcementFab;
    @Nullable @BindView(R.id.fab_add_article) FloatingActionButton articleFab;
    @Nullable @BindView(R.id.fab_add_vote) FloatingActionButton voteFab;
    @Nullable @BindView(R.id.retryBtn) Button retryBtn;

    @Nullable @BindView(R.id.progressBar) ProgressBar progressBar;
    @Nullable @BindView(R.id.content) View content;
    @Nullable @BindView(R.id.content_fam) View content_fam;
    @Nullable @BindView(R.id.noInternet) View noInternet;

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
        View v = inflater.inflate(R.layout.fragment_group_info, container, false);
        setHasOptionsMenu(true);

        chat = (Chat) this.getArguments().getSerializable("chat");
        instance = this.getArguments().getInt("instance");
        unbinder = ButterKnife.bind(this, v);

        presenter = new GroupInfoPresenter(this, getActivity(), chat);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isOneOfTheAdaptersExists())
            presenter.notifyFragmentStarted(chat);
        else
            presenter.setRegistration();
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

        if (getAdapterByRecyclerType(Consts.TYPE_ANNOUNCEMENT) != null)
            ((ActionsRecyclerAdapter) getAdapterByRecyclerType(Consts.TYPE_ANNOUNCEMENT)).clearItems();
        if (getAdapterByRecyclerType(Consts.TYPE_POLL) != null)
            ((ActionsRecyclerAdapter)  getAdapterByRecyclerType(Consts.TYPE_POLL)).clearItems();
        if (getAdapterByRecyclerType(Consts.TYPE_ARTICLE) != null)
            ((ActionsRecyclerAdapter) getAdapterByRecyclerType(Consts.TYPE_ARTICLE)).clearItems();
    }

    private void animateViewAppearance(View view) {
        view.setAlpha(0.0f);
        view.animate()
                .translationY(view.getHeight())
                .alpha(1.0f)
                .setListener(null);
    }

    @Override
    public void showRequiredViews() {
        progressBar.setVisibility(View.GONE);
        noInternet.setVisibility(View.GONE);
        content.setVisibility(View.GONE);

        switch (state) {
            case Consts.STATE_CONTENT:
                appBarLayout.setExpanded(true);
                content.setVisibility(View.VISIBLE);
                content_fam.setVisibility(View.VISIBLE);

                animateViewAppearance(content);
                animateViewAppearance(content_fam);
                break;
            case Consts.STATE_NO_INTERNET_CONNECTION:
                appBarLayout.setExpanded(false);
                noInternet.setVisibility(View.VISIBLE);
                animateViewAppearance(noInternet);
                break;
        }
    }

    @Override
    public void setupLayouts(boolean isNetworkAvailable) {
        if (isNetworkAvailable)
            state = Consts.STATE_CONTENT;
        else {
            announcementsRecycler.setAdapter(null);
            pollsRecycler.setAdapter(null);
            articlesRecycler.setAdapter(null);
            participantsRecycler.setAdapter(null);
            state = Consts.STATE_NO_INTERNET_CONNECTION;
        }
    }

    @Override
    public void setupToolbar() {
        AppCompatActivity activity = (AppCompatActivity)getActivity();

        activity.setSupportActionBar(collapsingToolbar);
        String title = chat.getName();

        ImageLoader imageLoader = new ImageLoader(this);
        if (chat.getImgURI() != null) {
            imageLoader.loadToolbarImage(groupImage, chat.getImgURI());
        } else {
            imageLoader.loadToolbarImage(groupImage, imageLoader.getUriOfDrawable(R.drawable.material_flat));
        }

        activity.getSupportActionBar().setTitle(title);

        collapsingToolbar.bringToFront();
    }

    @Override
    public void setupButtonClickListeners() {
        announcementsBtn.setOnClickListener(v -> {
            performExpansionByType(Consts.TYPE_ANNOUNCEMENT);
        });

        pollsBtn.setOnClickListener(v -> {
            performExpansionByType(Consts.TYPE_POLL);
        });

        articlesBtn.setOnClickListener(v -> {
            performExpansionByType(Consts.TYPE_ARTICLE);
        });

        participantsBtn.setOnClickListener(v -> {
            performExpansionByType(Consts.TYPE_USER);
        });
    }

    public void setupFab(){
        fam.setClosedOnTouchOutside(true);
        announcementFab.setOnClickListener(view -> {
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

                            String announcementText = text.getText().toString();
                            announcementText = announcementText.replace("\n", "").replace("\r", "").replace("!", "");
                            String message = "!" + desc.getText().toString() + "!\n" + announcementText;
                            presenter.sendTelegramMessage(progress, message);
                        }

                        fam.close(true);
                    })
                    .show();
        });

        articleFab.setOnClickListener(view -> {
            new MaterialDialog.Builder(getActivity())
                    .title("Статья")
                    .customView(R.layout.dialog_add_article, false)
                    .canceledOnTouchOutside(false)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .onPositive((dialog, which) -> {
                        EditText desc = (EditText) dialog.findViewById(R.id.articleDesc);
                        EditText text = (EditText) dialog.findViewById(R.id.articleText);

                        if (!desc.toString().isEmpty() && !text.toString().isEmpty()) {
                            MaterialDialog progress = new MaterialDialog.Builder(getActivity())
                                    .content("Пожалуйста, подождите")
                                    .progress(true, 0)
                                    .cancelable(false)
                                    .show();

                            String articleText = text.getText().toString();
                            articleText = articleText.replace("\n", "").replace("\r", "").replace("*", "");
                            String message = "*" + desc.getText().toString() + "*\n" + articleText;
                            presenter.sendTelegramMessage(progress, message);
                        }

                        fam.close(true);
                    })
                    .show();
        });

        voteFab.setOnClickListener(view -> {
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
            EditText desc = materialDialog.getView().findViewById(R.id.voteDesc);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

            PollRecyclerAdapter voteAdapter = new PollRecyclerAdapter(new ArrayList<>());
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

                    String message = "?" + desc.getText().toString() + "?";
                    StringBuilder builder = new StringBuilder(message);
                    for (int i = 0; i < options.size(); i++) {
                        builder.append("\n").append(i + 1).append(". ").append(options.get(i).getText().replace("?", ""));
                    }
                    message = builder.toString();
                    materialDialog.dismiss();
                    presenter.sendTelegramMessage(progress, message);
                    fam.close(true);
                }
            };
            materialDialog.getBuilder().onNeutral(neutralCallback).onPositive(positiveCallback);

            materialDialog.show();
        });
    }

    @Override
    public void setupActionsRecyclerView(String dataType) {
        RecyclerView recyclerView = getRecyclerViewByType(dataType);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);

        if (App.INSTANCE.areAnimationsEnabled())
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_from_bottom));

        recyclerView.setAdapter(new ActionsRecyclerAdapter(getActivity(), new ArrayList<>()));
    }

    @Override
    public void handleDataUpdate(String dataType, DocumentChange.Type type, int newIndex, int oldIndex, Action action) {
        ActionsRecyclerAdapter adapter = (ActionsRecyclerAdapter) getAdapterByRecyclerType(dataType);
        if (adapter != null) {
            switch (type) {
                case ADDED:
                    adapter.addItem(newIndex, action);
                    break;
                case MODIFIED:
                    adapter.updateItem(oldIndex, newIndex, action);
                    break;
                case REMOVED:
                    adapter.removeItem(oldIndex);
                    break;
            }
        }
    }

    @Override
    public void addParticipants(ArrayList<User> participants) {
        RecyclerView recyclerView = getRecyclerViewByType(Consts.TYPE_USER);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);

        if (App.INSTANCE.areAnimationsEnabled())
            recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_from_bottom));

        recyclerView.setAdapter(new UsersRecyclerAdapter(getActivity(), participants));
    }

    @Override
    public void setParticipantsCount(int count) {
        participantsBtn.setText("Участников: " + count);
    }

    @Override
    public void hideGroupActions(boolean isInList) {
        announcementsBtn.setVisibility(isInList ? View.VISIBLE : View.GONE);
        pollsBtn.setVisibility(isInList ? View.VISIBLE : View.GONE);
        articlesBtn.setVisibility(isInList ? View.VISIBLE : View.GONE);
        fam.setVisibility(isInList ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setupRetryButton() {
        retryBtn.setOnClickListener(view -> presenter.onRetryBtnClick());
    }

    @Override
    public boolean isOneOfTheAdaptersExists() {
        return getAdapterByRecyclerType(Consts.TYPE_ANNOUNCEMENT) != null
                || getAdapterByRecyclerType(Consts.TYPE_POLL) != null
                || getAdapterByRecyclerType(Consts.TYPE_ARTICLE) != null
                || getAdapterByRecyclerType(Consts.TYPE_USER) != null;
    }

    public void performExpansionByType(String dataType) {
        ExpandableLayout expandableLayout = getExpandableLayoutByType(dataType);

        if (expandableLayout != null) {
            if (expandableLayout.isExpanded()) {
                expandableLayout.collapse();
            } else {
                expandableLayout.expand();
            }
        }
    }

    private ExpandableLayout getExpandableLayoutByType(String dataType) {
        switch (dataType) {
            case Consts.TYPE_ANNOUNCEMENT:
                return announcementsLayout;
            case Consts.TYPE_POLL:
                return pollsLayout;
            case Consts.TYPE_ARTICLE:
                return articlesLayout;
            case Consts.TYPE_USER:
                return participantsLayout;
            default:
                return null;
        }
    }

    private RecyclerView.Adapter<?> getAdapterByRecyclerType(String dataType) {
        switch (dataType) {
            case Consts.TYPE_ANNOUNCEMENT:
                return (ActionsRecyclerAdapter) announcementsRecycler.getAdapter();
            case Consts.TYPE_POLL:
                return (ActionsRecyclerAdapter) pollsRecycler.getAdapter();
            case Consts.TYPE_ARTICLE:
                return (ActionsRecyclerAdapter) articlesRecycler.getAdapter();
            case Consts.TYPE_USER:
                return (UsersRecyclerAdapter) participantsRecycler.getAdapter();
            default:
                return null;
        }
    }

    private RecyclerView getRecyclerViewByType(String dataType) {
        switch (dataType) {
            case Consts.TYPE_ANNOUNCEMENT:
                return announcementsRecycler;
            case Consts.TYPE_POLL:
                return pollsRecycler;
            case Consts.TYPE_ARTICLE:
                return articlesRecycler;
            case Consts.TYPE_USER:
                return participantsRecycler;
            default:
                return null;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_group_info, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.closeBtn:
                getActivity().onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        announcementsRecycler.setAdapter(null);
        pollsRecycler.setAdapter(null);
        articlesRecycler.setAdapter(null);
        participantsRecycler.setAdapter(null);
        unbinder.unbind();
        presenter.destroy();
    }
}
