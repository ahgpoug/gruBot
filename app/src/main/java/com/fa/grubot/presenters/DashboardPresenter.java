package com.fa.grubot.presenters;


import com.fa.grubot.App;
import com.fa.grubot.abstractions.DashboardFragmentBase;
import com.fa.grubot.fragments.ActionsFragment;
import com.fa.grubot.models.DashboardModel;
import com.fa.grubot.objects.dashboard.DashboardAnnouncement;
import com.fa.grubot.objects.dashboard.DashboardItem;
import com.fa.grubot.objects.dashboard.DashboardVote;
import com.fa.grubot.util.Consts;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;

public class DashboardPresenter {
    private DashboardFragmentBase fragment;
    private DashboardModel model;

    private Query archiveAnnouncementsQuery;
    private Query announcementsQuery;
    private Query votesQuery;
    private Query archiveVotesQuery;

    private Query announcementsQueryVk;
    private Query archiveAnnouncementsQueryVk;
    private Query votesQueryVk;
    private Query archiveVotesQueryVk;

    private ListenerRegistration announcementsRegistrationVk;
    private ListenerRegistration archiveAnnouncementsRegistrationVk;
    private ListenerRegistration votesRegistrationVk;
    private ListenerRegistration archiveVotesRegistrationVk;

    private ListenerRegistration announcementsRegistration;
    private ListenerRegistration archiveAnnouncementsRegistration;
    private ListenerRegistration votesRegistration;
    private ListenerRegistration archiveVotesRegistration;

    public DashboardPresenter(DashboardFragmentBase fragment){
        this.fragment = fragment;
        this.model = new DashboardModel();

        initializeQueriesAndListeners();
    }

    private void initializeQueriesAndListeners() {
        if (App.INSTANCE.getCurrentUser().hasTelegramUser()) {
            archiveAnnouncementsQuery = FirebaseFirestore.getInstance().collection("announcements").whereEqualTo("users." + App.INSTANCE.getCurrentUser().getTelegramUser().getId(), "archive");
            announcementsQuery = FirebaseFirestore.getInstance().collection("announcements").whereEqualTo("users." + App.INSTANCE.getCurrentUser().getTelegramUser().getId(), "new");
            votesQuery = FirebaseFirestore.getInstance().collection("votes").whereEqualTo("users." + App.INSTANCE.getCurrentUser().getTelegramUser().getId(), "new");
            archiveVotesQuery = FirebaseFirestore.getInstance().collection("votes").whereEqualTo("users." + App.INSTANCE.getCurrentUser().getTelegramUser().getId(), "archive");
        }

        if (App.INSTANCE.getCurrentUser().hasVkUser()) {
            announcementsQueryVk = FirebaseFirestore.getInstance().collection("announcements").whereEqualTo("users." + App.INSTANCE.getCurrentUser().getVkUser().getId(), "new");
            archiveAnnouncementsQueryVk = FirebaseFirestore.getInstance().collection("announcements").whereEqualTo("users." + App.INSTANCE.getCurrentUser().getVkUser().getId(), "archive");
            votesQueryVk = FirebaseFirestore.getInstance().collection("votes").whereEqualTo("users." + App.INSTANCE.getCurrentUser().getVkUser().getId(), "new");
            archiveVotesQueryVk = FirebaseFirestore.getInstance().collection("votes").whereEqualTo("users." + App.INSTANCE.getCurrentUser().getVkUser().getId(), "archive");
        }


    }

    public void notifyFragmentStarted() {
        fragment.setupToolbar();
        if (App.INSTANCE.getCurrentUser().hasTelegramUser()) {
            setRegistration();
        }
        if(App.INSTANCE.getCurrentUser().hasVkUser()){
            setRegistrationVk();
        }
    }

    private void notifyViewCreated(int state) {
        fragment.showRequiredViews();

        switch (state) {
            case Consts.STATE_CONTENT:
                ArrayList<DashboardItem> items = new ArrayList<>(Arrays.asList(
                        new DashboardAnnouncement(0, 0),
                        new DashboardVote(0, 0)));

                fragment.setupRecyclerView(items);
                break;
            case Consts.STATE_NO_INTERNET_CONNECTION:
                fragment.setupRetryButton();
                break;
        }
    }

    @SuppressWarnings("unchecked")
    public void setRegistration() {
        announcementsRegistration = announcementsQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (e == null) {
                if (fragment != null && documentSnapshots.isEmpty() && !fragment.isAdapterExists())  {
                    fragment.setupLayouts(true);
                    notifyViewCreated(Consts.STATE_CONTENT);
                }

                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    int count = 0;

                    switch (dc.getType()) {
                        case ADDED:
                            count++;
                            break;
                        case REMOVED:
                            count--;
                            break;
                    }

                    if (fragment != null) {
                        if (!fragment.isAdapterExists()) {
                            fragment.setupLayouts(true);
                            notifyViewCreated(Consts.STATE_CONTENT);
                        }

                        fragment.handleListUpdate(count, ActionsFragment.TYPE_ANNOUNCEMENTS);
                    }
                }
            } else {
                if (fragment != null) {
                    fragment.setupLayouts(false);
                    notifyViewCreated(Consts.STATE_NO_INTERNET_CONNECTION);
                }
            }
        });

        archiveAnnouncementsRegistration = archiveAnnouncementsQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (e == null) {
                if (fragment != null && documentSnapshots.isEmpty() && !fragment.isAdapterExists())  {
                    fragment.setupLayouts(true);
                    notifyViewCreated(Consts.STATE_CONTENT);
                }

                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    int count = 0;

                    switch (dc.getType()) {
                        case ADDED:
                            count++;
                            break;
                        case REMOVED:
                            count--;
                            break;
                    }

                    if (fragment != null) {
                        if (!fragment.isAdapterExists()) {
                            fragment.setupLayouts(true);
                            notifyViewCreated(Consts.STATE_CONTENT);
                        }

                        fragment.handleListUpdate(count, ActionsFragment.TYPE_ANNOUNCEMENTS_ARCHIVE);
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
                if (fragment != null && documentSnapshots.isEmpty() && !fragment.isAdapterExists())  {
                    fragment.setupLayouts(true);
                    notifyViewCreated(Consts.STATE_CONTENT);
                }

                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    int count = 0;

                    switch (dc.getType()) {
                        case ADDED:
                            count++;
                            break;
                        case REMOVED:
                            count--;
                            break;
                    }

                    if (fragment != null) {
                        if (!fragment.isAdapterExists()) {
                            fragment.setupLayouts(true);
                            notifyViewCreated(Consts.STATE_CONTENT);
                        }

                        fragment.handleListUpdate(count, ActionsFragment.TYPE_VOTES);
                    }
                }
            } else {
                if (fragment != null) {
                    fragment.setupLayouts(false);
                    notifyViewCreated(Consts.STATE_NO_INTERNET_CONNECTION);
                }
            }
        });

        archiveVotesRegistration = archiveVotesQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (e == null) {
                if (fragment != null && documentSnapshots.isEmpty() && !fragment.isAdapterExists())  {
                    fragment.setupLayouts(true);
                    notifyViewCreated(Consts.STATE_CONTENT);
                }

                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    int count = 0;

                    switch (dc.getType()) {
                        case ADDED:
                            count++;
                            break;
                        case REMOVED:
                            count--;
                            break;
                    }

                    if (fragment != null) {
                        if (!fragment.isAdapterExists()) {
                            fragment.setupLayouts(true);
                            notifyViewCreated(Consts.STATE_CONTENT);
                        }

                        fragment.handleListUpdate(count, ActionsFragment.TYPE_VOTES_ARCHIVE);
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

    private void setRegistrationVk() {
        announcementsRegistrationVk = announcementsQueryVk.addSnapshotListener((documentSnapshots, e) -> {
            if (e == null) {
                if (fragment != null && documentSnapshots.isEmpty() && !fragment.isAdapterExists()) {
                    fragment.setupLayouts(true);
                    notifyViewCreated(Consts.STATE_CONTENT);
                }

                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    int count = 0;

                    switch (dc.getType()) {
                        case ADDED:
                            count++;
                            break;
                        case REMOVED:
                            count--;
                            break;
                    }

                    if (fragment != null) {
                        if (!fragment.isAdapterExists()) {
                            fragment.setupLayouts(true);
                            notifyViewCreated(Consts.STATE_CONTENT);
                        }

                        fragment.handleListUpdate(count, ActionsFragment.TYPE_ANNOUNCEMENTS);
                    }
                }
            } else {
                if (fragment != null) {
                    fragment.setupLayouts(false);
                    notifyViewCreated(Consts.STATE_NO_INTERNET_CONNECTION);
                }
            }
        });

        archiveAnnouncementsRegistrationVk = archiveAnnouncementsQueryVk.addSnapshotListener((documentSnapshots, e) -> {
            if (e == null) {
                if (fragment != null && documentSnapshots.isEmpty() && !fragment.isAdapterExists()) {
                    fragment.setupLayouts(true);
                    notifyViewCreated(Consts.STATE_CONTENT);
                }

                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    int count = 0;

                    switch (dc.getType()) {
                        case ADDED:
                            count++;
                            break;
                        case REMOVED:
                            count--;
                            break;
                    }

                    if (fragment != null) {
                        if (!fragment.isAdapterExists()) {
                            fragment.setupLayouts(true);
                            notifyViewCreated(Consts.STATE_CONTENT);
                        }

                        fragment.handleListUpdate(count, ActionsFragment.TYPE_ANNOUNCEMENTS_ARCHIVE);
                    }
                }
            } else {
                if (fragment != null) {
                    fragment.setupLayouts(false);
                    notifyViewCreated(Consts.STATE_NO_INTERNET_CONNECTION);
                }
            }
        });

        votesRegistrationVk = votesQueryVk.addSnapshotListener((documentSnapshots, e) -> {
            if (e == null) {
                if (fragment != null && documentSnapshots.isEmpty() && !fragment.isAdapterExists()) {
                    fragment.setupLayouts(true);
                    notifyViewCreated(Consts.STATE_CONTENT);
                }

                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    int count = 0;

                    switch (dc.getType()) {
                        case ADDED:
                            count++;
                            break;
                        case REMOVED:
                            count--;
                            break;
                    }

                    if (fragment != null) {
                        if (!fragment.isAdapterExists()) {
                            fragment.setupLayouts(true);
                            notifyViewCreated(Consts.STATE_CONTENT);
                        }

                        fragment.handleListUpdate(count, ActionsFragment.TYPE_VOTES);
                    }
                }
            } else {
                if (fragment != null) {
                    fragment.setupLayouts(false);
                    notifyViewCreated(Consts.STATE_NO_INTERNET_CONNECTION);
                }
            }
        });

        archiveVotesRegistrationVk = archiveVotesQueryVk.addSnapshotListener((documentSnapshots, e) -> {
            if (e == null) {
                if (fragment != null && documentSnapshots.isEmpty() && !fragment.isAdapterExists()) {
                    fragment.setupLayouts(true);
                    notifyViewCreated(Consts.STATE_CONTENT);
                }

                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    int count = 0;

                    switch (dc.getType()) {
                        case ADDED:
                            count++;
                            break;
                        case REMOVED:
                            count--;
                            break;
                    }

                    if (fragment != null) {
                        if (!fragment.isAdapterExists()) {
                            fragment.setupLayouts(true);
                            notifyViewCreated(Consts.STATE_CONTENT);
                        }

                        fragment.handleListUpdate(count, ActionsFragment.TYPE_VOTES_ARCHIVE);
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

    public void onRetryBtnClick() {
        setRegistration();
        setRegistrationVk();
    }

    public void removeRegistration() {
        if (announcementsRegistration != null)
            announcementsRegistration.remove();
        if (archiveAnnouncementsRegistration != null)
            archiveAnnouncementsRegistration.remove();
        if (votesRegistration != null)
            votesRegistration.remove();
        if (archiveVotesRegistration != null)
            archiveVotesRegistration.remove();
        if (announcementsRegistrationVk != null)
            announcementsRegistrationVk.remove();
        if (archiveAnnouncementsRegistrationVk != null)
            archiveAnnouncementsRegistrationVk.remove();
        if (votesRegistrationVk != null)
            votesRegistrationVk.remove();
        if (archiveVotesRegistrationVk != null)
            archiveVotesRegistrationVk.remove();
    }

    public void destroy() {
        removeRegistration();
        fragment = null;
        model = null;
    }
}
