package com.example.taskmanager;

public class NotificationItem {
    private String dateTime; // Date et heure de la notification
    private String content;  // Contenu de la notification

    public NotificationItem(String dateTime, String content) {
        this.dateTime = dateTime;
        this.content = content;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getContent() {
        return content;
    }
}
