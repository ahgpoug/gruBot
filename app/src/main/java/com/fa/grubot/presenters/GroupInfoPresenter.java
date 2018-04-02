package com.fa.grubot.presenters;


import com.fa.grubot.abstractions.GroupInfoFragmentBase;
import com.fa.grubot.adapters.GroupInfoRecyclerAdapter;
import com.fa.grubot.models.GroupInfoModel;
import com.fa.grubot.objects.chat.Chat;
import com.fa.grubot.objects.dashboard.ActionAnnouncement;
import com.fa.grubot.objects.dashboard.ActionVote;
import com.fa.grubot.objects.misc.GroupInfoButton;
import com.fa.grubot.objects.misc.VoteOption;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.util.Consts;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class GroupInfoPresenter {
    private GroupInfoFragmentBase fragment;
    private GroupInfoModel model;
    private ArrayList<GroupInfoRecyclerAdapter.GroupInfoRecyclerItem> buttons = new ArrayList<>(Arrays.asList(
            new GroupInfoRecyclerAdapter.GroupInfoRecyclerItem(new GroupInfoButton(1, "Чат", new ArrayList<>())),
            new GroupInfoRecyclerAdapter.GroupInfoRecyclerItem(new GroupInfoButton(2, "Ветки осбуждений", new ArrayList<>())),
            new GroupInfoRecyclerAdapter.GroupInfoRecyclerItem(new GroupInfoButton(3, "Объявления", new ArrayList<>())),
            new GroupInfoRecyclerAdapter.GroupInfoRecyclerItem(new GroupInfoButton(4, "Голосования", new ArrayList<>())),
            new GroupInfoRecyclerAdapter.GroupInfoRecyclerItem(new GroupInfoButton(5, "Участники", new ArrayList<>()))
    ));

    private Chat localChat;

    private Query groupQuery;
    private Query usersQuery;
    private Query announcementsQuery;
    private Query votesQuery;

    private ListenerRegistration groupRegistration;
    private ListenerRegistration usersRegistration;
    private ListenerRegistration announcementsRegistration;
    private ListenerRegistration votesRegistration;

    public GroupInfoPresenter(GroupInfoFragmentBase fragment) {
        this.fragment = fragment;
        this.model = new GroupInfoModel();
    }

    public void notifyFragmentStarted(Chat chat) {
        String groupId = chat.getId();
        groupQuery = FirebaseFirestore.getInstance().collection("groups").whereEqualTo("chatId", groupId);
        usersQuery = FirebaseFirestore.getInstance().collection("users").whereEqualTo("groups." + groupId, true);
        announcementsQuery = FirebaseFirestore.getInstance().collection("announcements").whereEqualTo("chat", groupId);
        votesQuery = FirebaseFirestore.getInstance().collection("votes").whereEqualTo("chat", groupId);

        setRegistration();
    }

    private void notifyViewCreated(int state){
        fragment.showRequiredViews();

        switch (state) {
            case Consts.STATE_CONTENT:
                fragment.setupToolbar();
                fragment.setupFab();
                fragment.setupRecyclerView(buttons);
                break;
            case Consts.STATE_NO_INTERNET_CONNECTION:
                fragment.setupRetryButton();
                break;
        }
    }

    @SuppressWarnings("unchecked")
    public void setRegistration() {
        groupRegistration = groupQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (e == null) {
                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = dc.getDocument();
                    //Chat chat = new Chat(doc.get("chatId").toString(), doc.get("name").toString(), (Map<String, Boolean>) doc.get("users"), doc.get("imgUrl").toString());

                    if (fragment != null) {
                        if (!fragment.isAdapterExists()) {
                            fragment.setupLayouts(true);
                            notifyViewCreated(Consts.STATE_CONTENT);
                        }

                        fragment.handleUIUpdate(null);
                    }
                }
            } else {
                if (fragment != null) {
                    fragment.setupLayouts(false);
                    notifyViewCreated(Consts.STATE_NO_INTERNET_CONNECTION);
                }
            }
        });

        announcementsRegistration = announcementsQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (e == null) {
                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = dc.getDocument();
                    ActionAnnouncement announcement = new ActionAnnouncement(
                                doc.getId(),
                                doc.get("group").toString(),
                                doc.get("groupName").toString(),
                                doc.get("author").toString(),
                                doc.get("authorName").toString(),
                                doc.get("desc").toString(),
                                (Date) doc.get("date"),
                                doc.get("text").toString(),
                                (Map<String, String>) doc.get("users"));

                    if (fragment != null) {
                        if (!fragment.isAdapterExists()) {
                            fragment.setupLayouts(true);
                            notifyViewCreated(Consts.STATE_CONTENT);
                        }

                        fragment.handleActionsUpdate(dc.getType(), dc.getNewIndex(), dc.getOldIndex(), announcement);
                    }
                }
            } else {
                if (fragment != null) {
                    fragment.setupLayouts(false);
                    notifyViewCreated(Consts.STATE_NO_INTERNET_CONNECTION);
                }
            }
        });

        votesRegistration = votesQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (e == null) {
                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = dc.getDocument();
                        ArrayList<VoteOption> voteOptions = new ArrayList<>();
                        for (Map.Entry<String, String> option : ((Map<String, String>) doc.get("voteOptions")).entrySet())
                            voteOptions.add(new VoteOption(option.getValue()));

                        ActionVote vote = new ActionVote(
                                doc.getId(),
                                doc.get("group").toString(),
                                doc.get("groupName").toString(),
                                doc.get("author").toString(),
                                doc.get("authorName").toString(),
                                doc.get("desc").toString(),
                                (Date) doc.get("date"),
                                voteOptions,
                                (Map<String, String>) doc.get("users"));

                    if (fragment != null) {
                        if (!fragment.isAdapterExists()) {
                            fragment.setupLayouts(true);
                            notifyViewCreated(Consts.STATE_CONTENT);
                        }

                        fragment.handleActionsUpdate(dc.getType(), dc.getNewIndex(), dc.getOldIndex(), vote);
                    }
                }
            } else {
                if (fragment != null) {
                    fragment.setupLayouts(false);
                    notifyViewCreated(Consts.STATE_NO_INTERNET_CONNECTION);
                }
            }
        });

        usersRegistration = usersQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (e == null) {
                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = dc.getDocument();
                    User user = new User( doc.get("userId").toString(),
                            doc.get("username").toString(),
                            doc.get("fullname").toString(),
                            doc.get("desc").toString(),
                            doc.get("imgUrl").toString());

                    if (fragment != null) {
                        if (!fragment.isAdapterExists()) {
                            fragment.setupLayouts(true);
                            notifyViewCreated(Consts.STATE_CONTENT);
                        }

                        fragment.handleUsersUpdate(dc.getType(), dc.getNewIndex(), dc.getOldIndex(), user);
                    }
                }
            } else {
                if (fragment != null) {
                    fragment.setupLayouts(false);
                    notifyViewCreated(Consts.STATE_NO_INTERNET_CONNECTION);
                }
            }
        });
    }

    public void removeRegistration() {
        if (groupRegistration != null)
            groupRegistration.remove();
        if (usersRegistration != null)
            usersRegistration.remove();
        if (announcementsRegistration != null)
            announcementsRegistration.remove();
        if (votesRegistration != null)
            votesRegistration.remove();
    }

    public void onRetryBtnClick() {
        setRegistration();
    }

    public void destroy() {
        removeRegistration();
        fragment = null;
        model = null;
    }
}
