package com.fa.grubot.presenters;


import com.fa.grubot.App;
import com.fa.grubot.abstractions.ActionsFragmentBase;
import com.fa.grubot.fragments.ActionsFragment;
import com.fa.grubot.models.ActionsModel;
import com.fa.grubot.objects.dashboard.Action;
import com.fa.grubot.objects.dashboard.ActionAnnouncement;
import com.fa.grubot.objects.dashboard.ActionArticle;
import com.fa.grubot.objects.dashboard.ActionPoll;
import com.fa.grubot.objects.misc.VoteOption;
import com.fa.grubot.util.Consts;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class ActionsPresenter {
    private ActionsFragmentBase fragment;
    private ActionsModel model;
    private ArrayList<Action> actions = new ArrayList<>();

    private Query actionsQuery;
    private ListenerRegistration actionsRegistration;

    private int type;

    public ActionsPresenter(ActionsFragmentBase fragment){
        this.fragment = fragment;
        this.model = new ActionsModel();
    }

    public void notifyFragmentStarted(int type) {
        this.type = type;

        switch (type) {
            case ActionsFragment.TYPE_ANNOUNCEMENTS:
                actionsQuery = FirebaseFirestore.getInstance().collection("announcements").whereEqualTo("users." + App.INSTANCE.getCurrentUser().getTelegramUser().getId(), "new");
                break;
            case ActionsFragment.TYPE_ANNOUNCEMENTS_ARCHIVE:
                actionsQuery = FirebaseFirestore.getInstance().collection("announcements").whereEqualTo("users." + App.INSTANCE.getCurrentUser().getTelegramUser().getId(), "archive");
                break;
            case ActionsFragment.TYPE_POLLS:
                actionsQuery = FirebaseFirestore.getInstance().collection("votes").whereEqualTo("users." + App.INSTANCE.getCurrentUser().getTelegramUser().getId(), "new");
                break;
            case ActionsFragment.TYPE_POLLS_ARCHIVE:
                actionsQuery = FirebaseFirestore.getInstance().collection("votes").whereGreaterThan("users." + App.INSTANCE.getCurrentUser().getTelegramUser().getId(), 0);
                break;
            case ActionsFragment.TYPE_ARTICLES:
                actionsQuery = FirebaseFirestore.getInstance().collection("articles").whereEqualTo("users." + App.INSTANCE.getCurrentUser().getTelegramUser().getId(), "new");
                break;
        }

        setRegistration(type);
    }

    private void notifyViewCreated(int state) {
        fragment.showRequiredViews();

        switch (state) {
            case Consts.STATE_CONTENT:
                if (type == ActionsFragment.TYPE_ARTICLES)
                    fragment.setupToolbar();
                fragment.setupRecyclerView(actions);
                break;
            case Consts.STATE_NO_INTERNET_CONNECTION:
                fragment.setupRetryButton();
                break;
            case Consts.STATE_NO_DATA:
                break;
        }
    }

    @SuppressWarnings("unchecked")
    public void setRegistration(int type) {
        actionsRegistration = actionsQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (e == null) {
                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = dc.getDocument();
                    Action action;
                    try {
                        if (type == ActionsFragment.TYPE_ANNOUNCEMENTS || type == ActionsFragment.TYPE_ANNOUNCEMENTS_ARCHIVE) {
                            action = new ActionAnnouncement(
                                    doc.getId(),
                                    doc.get("group").toString(),
                                    doc.get("groupName").toString(),
                                    doc.get("author").toString(),
                                    doc.get("authorName").toString(),
                                    doc.get("desc").toString(),
                                    (Date) doc.get("date"),
                                    doc.get("text").toString(),
                                    (Map<String, String>) doc.get("users"),
                                    (long) doc.get("messageId"),
                                    doc.get("type").toString());
                        } else if (type == ActionsFragment.TYPE_ARTICLES) {
                            action = new ActionArticle(
                                    doc.getId(),
                                    doc.get("group").toString(),
                                    doc.get("groupName").toString(),
                                    doc.get("author").toString(),
                                    doc.get("authorName").toString(),
                                    doc.get("desc").toString(),
                                    (Date) doc.get("date"),
                                    doc.get("text").toString(),
                                    (Map<String, String>) doc.get("users"),
                                    (long) doc.get("messageId"),
                                    doc.get("type").toString());

                        } else if (type == ActionsFragment.TYPE_POLLS || type == ActionsFragment.TYPE_POLLS_ARCHIVE) {
                            ArrayList<VoteOption> voteOptions = new ArrayList<>();
                            HashMap<String, String> options = (HashMap<String, String>) doc.get("voteOptions");
                            SortedSet<String> keys = new TreeSet<>(options.keySet());

                            for (String key : keys) {
                                voteOptions.add(new VoteOption(options.get(key)));
                            }

                            action = new ActionPoll(
                                    doc.getId(),
                                    doc.get("group").toString(),
                                    doc.get("groupName").toString(),
                                    doc.get("author").toString(),
                                    doc.get("authorName").toString(),
                                    doc.get("desc").toString(),
                                    (Date) doc.get("date"),
                                    voteOptions,
                                    (Map<String, String>) doc.get("users"),
                                    (long) doc.get("messageId"),
                                    doc.get("type").toString());
                        } else {
                            action = null;
                        }


                        if (fragment != null) {
                            if (!fragment.isAdapterExists() && fragment.isListEmpty()) {
                                fragment.setupLayouts(true, true);
                                notifyViewCreated(Consts.STATE_CONTENT);
                            }

                            fragment.handleListUpdate(dc.getType(), dc.getNewIndex(), dc.getOldIndex(), action);
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                if (fragment != null && fragment.isListEmpty()) {
                    fragment.setupLayouts(true, false);
                    notifyViewCreated(Consts.STATE_NO_DATA);
                }
            } else {
                if (fragment != null) {
                    fragment.setupLayouts(false, false);
                    notifyViewCreated(Consts.STATE_NO_INTERNET_CONNECTION);
                }
            }
        });
    }

    public void addActionToArchive(Action action, int type) {
        if (type == ActionsFragment.TYPE_ANNOUNCEMENTS) {
            addAnnouncementToArchive((ActionAnnouncement) action);
        } else  {
            addVoteToArchive((ActionPoll) action);
        }
    }

    @SuppressWarnings("unchecked")
    private void addAnnouncementToArchive(ActionAnnouncement announcement) {
        FirebaseFirestore.getInstance().collection("announcements")
                .document(announcement.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, String> users = (Map<String, String>) documentSnapshot.get("users");

                    users.put(String.valueOf(App.INSTANCE.getCurrentUser().getTelegramUser().getId()), "archive");

                    FirebaseFirestore.getInstance().collection("announcements")
                            .document(announcement.getId())
                            .update("users", users)
                            .addOnSuccessListener(aVoid -> {
                                if (fragment != null)
                                    fragment.showArchiveSnackbar(announcement);
                            });
                });
    }

    @SuppressWarnings("unchecked")
    private void addVoteToArchive(ActionPoll vote) {
        FirebaseFirestore.getInstance().collection("votes")
                .document(vote.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, String> users = (Map<String, String>) documentSnapshot.get("users");

                    users.put(String.valueOf(App.INSTANCE.getCurrentUser().getTelegramUser().getId()), "-1");

                    FirebaseFirestore.getInstance().collection("votes")
                            .document(vote.getId())
                            .update("users", users)
                            .addOnSuccessListener(aVoid -> {
                                if (fragment != null)
                                    fragment.showArchiveSnackbar(vote);
                            });
                });
    }

    public void restoreActionFromArchive(Action action, int type) {
        if (type == ActionsFragment.TYPE_ANNOUNCEMENTS) {
            restoreAnnouncementFromArchive((ActionAnnouncement) action);
        } else {
            restorePollFromArchive((ActionPoll) action);
        }
    }

    @SuppressWarnings("unchecked")
    private void restoreAnnouncementFromArchive(ActionAnnouncement announcement) {
        FirebaseFirestore.getInstance().collection("announcements")
                .document(announcement.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, String> users = (Map<String, String>) documentSnapshot.get("users");

                    users.put(String.valueOf(App.INSTANCE.getCurrentUser().getTelegramUser().getId()), "new");

                    FirebaseFirestore.getInstance().collection("announcements")
                            .document(announcement.getId())
                            .update("users", users);
                });
    }

    @SuppressWarnings("unchecked")
    private void restorePollFromArchive(ActionPoll vote) {
        FirebaseFirestore.getInstance().collection("votes")
                .document(vote.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Map<String, String> users = (Map<String, String>) documentSnapshot.get("users");

                    users.put(String.valueOf(App.INSTANCE.getCurrentUser().getTelegramUser().getId()), "new");

                    FirebaseFirestore.getInstance().collection("votes")
                            .document(vote.getId())
                            .update("users", users);
                });
    }

    public void onRetryBtnClick(int type) {
        setRegistration(type);
    }

    public void removeRegistration() {
        if (actionsRegistration != null)
            actionsRegistration.remove();
    }

    public void destroy() {
        removeRegistration();
        fragment = null;
        model = null;
    }
}
