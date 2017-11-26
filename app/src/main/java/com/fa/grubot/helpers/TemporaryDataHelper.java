package com.fa.grubot.helpers;

import com.fa.grubot.fragments.ActionsFragment;
import com.fa.grubot.objects.chat.BranchOfDiscussions;
import com.fa.grubot.objects.dashboard.Action;
import com.fa.grubot.objects.dashboard.ActionAnnouncement;
import com.fa.grubot.objects.dashboard.ActionVote;
import com.fa.grubot.objects.group.Group;
import com.fa.grubot.objects.group.User;
import com.fa.grubot.objects.misc.VoteOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class TemporaryDataHelper {
    private final String placeholder = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna " +
            "aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. " +
            "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint " +
            "occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum";
    private final ArrayList<VoteOption> options = new ArrayList<>(Arrays.asList(new VoteOption("Первый вариант"), new VoteOption("Второй вариант"), new VoteOption("Третий вариант")));

    private ArrayList<Action> announcementsList = new ArrayList<>();
    private ArrayList<Action> votesList = new ArrayList<>();

    private ArrayList<Action> announcementsArchiveList = new ArrayList<>();
    private ArrayList<Action> votesArchiveList = new ArrayList<>();

    private ArrayList<User> usersList = new ArrayList<>();
    private ArrayList<Group> groupsList = new ArrayList<>();

    public TemporaryDataHelper() {
        setUsers();
        setGroups();
        setActions();
        setBranchesOfGroups();
    }

    public ArrayList<Action> getActionsByType(int type) {
        switch (type) {
            case ActionsFragment.TYPE_ANNOUNCEMENTS:
                return announcementsList;
            case ActionsFragment.TYPE_VOTES:
                return votesList;
            case ActionsFragment.TYPE_ANNOUNCEMENTS_ARCHIVE:
                return announcementsArchiveList;
            case ActionsFragment.TYPE_VOTES_ARCHIVE:
                return votesArchiveList;
        }
        return null;
    }

    public ArrayList<Action> getActionsByGroupAndType(int type, Group group) {
        ArrayList<Action> actions = new ArrayList<>();

        if (type == ActionsFragment.TYPE_ANNOUNCEMENTS) {
            for (Action action : announcementsList) {
                if (action.getGroup().equals(group))
                    actions.add(action);
            }
            return actions;
        } else {
            for (Action action : votesList) {
                if (action.getGroup().equals(group))
                    actions.add(action);
            }
            return actions;
        }
    }

    public void addNewActionByType(int type, Action action) {
        if (type == ActionsFragment.TYPE_ANNOUNCEMENTS) {
            announcementsList.add(action);
        } else {
            votesList.add(action);
        }
    }

    public void addActionToArchive(int type, Action action) {
        if (type == ActionsFragment.TYPE_ANNOUNCEMENTS) {
            announcementsList.remove(action);
            announcementsArchiveList.add(action);
        } else {
            votesList.remove(action);
            votesArchiveList.add(action);
        }
    }

    public void restoreActionFromArchive(int type, Action action, int position) {
        if (type == ActionsFragment.TYPE_ANNOUNCEMENTS) {
            announcementsArchiveList.remove(action);
            announcementsList.add(position, action);
        } else {
            votesArchiveList.remove(action);
            votesList.add(position, action);
        }
    }

    public int getAnnouncemtsCount() {
        return announcementsList.size();
    }

    public int getVotesCount() {
        return votesList.size();
    }

    public ArrayList<User> getUsersByGroup(Group group) {
        return group.getUsers();
    }

    public ArrayList<Group> getGroups() {
        return groupsList;
    }

    private void setActions() {
        announcementsList.add(new ActionAnnouncement(1, groupsList.get(0), "Комлев Антон", "Собрание", new Date(),  placeholder));
        announcementsList.add(new ActionAnnouncement(1, groupsList.get(0), "Комлев Антон", "Выходные дни", new Date(), placeholder));
        announcementsList.add(new ActionAnnouncement(3, groupsList.get(2), "Чехов А. П.", "Поездка", new Date(), placeholder));
        announcementsList.add(new ActionAnnouncement(3, groupsList.get(2), "Чехов А. П.", "Объявление", new Date(), placeholder));
        announcementsList.add(new ActionAnnouncement(3, groupsList.get(2), "Чехов А. П.", "Собрание", new Date(), placeholder));

        votesList.add(new ActionVote(1, groupsList.get(0), "Комлев Антон", "Новый год", new Date(), options));
        votesList.add(new ActionVote(2, groupsList.get(1), "Махин Семен", "Сбор денег", new Date(), options));
        votesList.add(new ActionVote(2, groupsList.get(1), "Махин Семен", "Удовлетворенность чем-то", new Date(), options));
        votesList.add(new ActionVote(2, groupsList.get(1), "Махин Семен", "Активность", new Date(), options));
    }

    private void setUsers() {
        usersList.add(new User(1, "pussyStealer", "Антон Комлев", "7(903)869-14-82", "Кружка", null));
        usersList.add(new User(2, "actuallyStalin", "Петров Николай", "7(903)322-14-88", "OHHHHHHHHHHHHHHHHHHHHHHHH", null));
        usersList.add(new User(3, "dip", "Прахов Владислав", "7(903)869-22-77", "123", null));
        usersList.add(new User(4, "a_goat", "Елена Головач", "7(903)334-33-32", "TIPA OPISANIE", "https://upload.wikimedia.org/wikipedia/commons/f/ff/Domestic_goat_kid_in_capeweed.jpg"));
        usersList.add(new User(5, "deus_vult", "Конрад Цёлльнер фон Ротенштайн", "7(903)132-81-39", "TIPA OPISANIE EWE RAZ", "https://upload.wikimedia.org/wikipedia/commons/f/fa/Wg_zoellner.gif"));
        usersList.add(new User(6, "just_a_painter", "Адольф Гитлер", "7(903)132-11-11", "TIPA TOJE", "https://upload.wikimedia.org/wikipedia/commons/a/ab/Bundesarchiv_Bild_183-H1216-0500-002%2C_Adolf_Hitler.jpg"));
        usersList.add(new User(7, "semen", "Махин Семен", "7(903)322-14-88", "4343", null));

    }

    private void setGroups() {
        groupsList.add(new Group(1, "ПИ4-1", new ArrayList<>(Arrays.asList(usersList.get(0), usersList.get(2), usersList.get(1))), "https://2static3.fjcdn.com/comments/Fun+fact+the+flat+topped+great+helm+is+a+piece+_3cb2af934364bbe51707d55061d6aacb.jpg"));
        groupsList.add(new Group(2, "ПИ4-2", new ArrayList<>(Arrays.asList(usersList.get(6), usersList.get(3))),null));
        groupsList.add(new Group(3, "ГРУППА НАМБА ВАН НА РУСИ", new ArrayList<>(Arrays.asList(usersList.get(5), usersList.get(4), usersList.get(3))),null));
    }

    private void setBranchesOfGroups() {
        // gr1

        //gr2


        //gr3 has no branches


    }

    public ArrayList<BranchOfDiscussions> getBranches(int idOfGroup) {
        ArrayList<BranchOfDiscussions> branchesOfGroup = new ArrayList<>();
        switch (idOfGroup) {
            case 1:
                Calendar past = Calendar.getInstance();
                past.set(2017, 11, 15, 21, 15);
                branchesOfGroup.add(new BranchOfDiscussions(1, 2, "Заблевали пол", past, Calendar.getInstance(), 15));
                branchesOfGroup.add(new BranchOfDiscussions(2, 5, "Драка детей", Calendar.getInstance(), Calendar.getInstance(), 20));
                branchesOfGroup.add(new BranchOfDiscussions(3, 34, "Украли сменку", Calendar.getInstance(), Calendar.getInstance(), 25));
                break;
            case 2:
                branchesOfGroup.add(new BranchOfDiscussions(4, 4, "Золотая медаль никому не нужна", Calendar.getInstance(), Calendar.getInstance(), 14));
                branchesOfGroup.add(new BranchOfDiscussions(5, 6, "Учитель педофил", Calendar.getInstance(), Calendar.getInstance(), 88));
                branchesOfGroup.add(new BranchOfDiscussions(6, 19, "Кто такой FACE?", Calendar.getInstance(), Calendar.getInstance(), 11));
                break;
            case 3:
                break;
        }
        return branchesOfGroup;
    }
}
