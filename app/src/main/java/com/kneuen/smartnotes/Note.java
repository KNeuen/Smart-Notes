package com.kneuen.smartnotes;

import android.util.Log;

import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Note {
    private UUID id;
    private String title;
    private String content;

    private boolean isPinned;

    private long creationTime;
    private String tag;

    private long reminderDate;

    public Note() {
        this.id = UUID.randomUUID();
        this.tag = null;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    // Getter and setter for isPinned
    public boolean isPinned() {
        return isPinned;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public Note(UUID id, String title, String content) {

        this.id = id;
        this.title = title;
        this.content = content;
        this.tag = null;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public String getFormattedCreationTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        return formatter.format(new Date(creationTime));
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
    public void deleteTag() {
        this.tag = null;
    }

    public String getTag() {
        return this.tag;
    }


    public long getReminderDate() {
        return reminderDate;
    }

    public void setReminderDate(long reminderDate) {
        this.reminderDate = reminderDate;
    }

}
