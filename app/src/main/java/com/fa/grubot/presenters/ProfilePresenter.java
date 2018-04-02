package com.fa.grubot.presenters;


import com.fa.grubot.abstractions.ProfileFragmentBase;
import com.fa.grubot.models.ProfileModel;
import com.fa.grubot.objects.misc.ProfileItem;
import com.fa.grubot.objects.users.User;
import com.fa.grubot.util.Consts;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class ProfilePresenter {
    private ProfileFragmentBase fragment;
    private ProfileModel model;

    private ArrayList<ProfileItem> items = new ArrayList<>();
    private User localUser;

    private Query userQuery;
    private ListenerRegistration userRegistration;

    public ProfilePresenter(ProfileFragmentBase fragment) {
        this.fragment = fragment;
        this.model = new ProfileModel();
    }

    public void notifyFragmentStarted(String userId) {
        userQuery = FirebaseFirestore.getInstance().collection("users").whereEqualTo("userId", userId);
        setRegistration();
    }

    private void notifyViewCreated(int state) {
        fragment.showRequiredViews();

        switch (state) {
            case Consts.STATE_CONTENT:
                fragment.setupToolbar(localUser);
                fragment.setupRecyclerView(items, localUser);
                break;
            case Consts.STATE_NO_INTERNET_CONNECTION:
                fragment.setupRetryButton();
                break;
        }
    }

    @SuppressWarnings("unchecked")
    public void setRegistration() {
        userRegistration = userQuery.addSnapshotListener((documentSnapshots, e) -> {
            if (e == null) {
                for (DocumentChange dc : documentSnapshots.getDocumentChanges()) {
                    DocumentSnapshot doc = dc.getDocument();
                    User user = new User(doc.get("userId").toString(),
                            doc.get("username").toString(),
                            doc.get("fullname").toString(),
                            doc.get("desc").toString(),
                            doc.get("imgUrl").toString());

                    ArrayList<String> changes = new ArrayList<>();
                    if (localUser != null) {
                        if (!user.getUserName().equals(localUser.getUserName()))
                            changes.add("username");
                        if (!user.getFullname().equals(localUser.getFullname()))
                            changes.add("fullname");
                        if (!user.getPhoneNumber().equals(localUser.getPhoneNumber()))
                            changes.add("phoneNumber");
                        if (!user.getImgUrl().equals(localUser.getImgUrl()))
                            changes.add("avatar");
                    }

                    localUser = user;

                    if (fragment != null) {
                        if (!fragment.isAdapterExists()) {
                            fragment.setupLayouts(true);
                            notifyViewCreated(Consts.STATE_CONTENT);
                        }

                        fragment.handleProfileUpdate(user, changes);
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
        if (userRegistration != null)
            userRegistration.remove();
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
