package com.kneuen.smartnotes;

import android.util.Log;

import java.util.UUID;

public class Note {
    private UUID id;
    private String title;
    private String content;

    private boolean isPinned;

    private long creationTime;

    public Note() {
        this.id = UUID.randomUUID();
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
    }


    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
}
