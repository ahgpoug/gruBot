package com.fa.grubot.objects.dashboard;

public class DashboardAnnouncement extends DashboardItem {
    private int newAnnouncementsCount;
    private int archiveAnnouncementsCount;
    private int totalAnnouncementsCount;

    public DashboardAnnouncement(int newAnnouncementsCount, int archiveAnnouncementsCount, int totalAnnouncementsCount) {
        super();
        this.newAnnouncementsCount = newAnnouncementsCount;
        this.archiveAnnouncementsCount = archiveAnnouncementsCount;
        this.totalAnnouncementsCount = totalAnnouncementsCount;
    }

    public int getNewAnnouncementsCount() {
        return newAnnouncementsCount;
    }

    public int getArchiveAnnouncementsCount() {
        return archiveAnnouncementsCount;
    }

    public int getTotalAnnouncementsCount() {
        return totalAnnouncementsCount;
    }
}
