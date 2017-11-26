package com.fa.grubot.models;

import android.util.Log;

import com.fa.grubot.App;
import com.fa.grubot.objects.chat.BranchOfDiscussions;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by ni.petrov on 18/11/2017.
 */

public class BranchesModel {


    public BranchesModel() {
    }

    public ArrayList<BranchOfDiscussions> loadBranches(int idOfGroup) {
        Log.d(TAG, "loadBranches: " + idOfGroup);
        return App.INSTANCE.getDataHelper().getBranches(idOfGroup);
    }
}
