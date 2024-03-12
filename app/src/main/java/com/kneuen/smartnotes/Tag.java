package com.kneuen.smartnotes;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Tag {
    private String id;

    private String name;

    private List<Note> noteIdList;

    public Tag() {
        this.id = UUID.randomUUID().toString();
        this.noteIdList = new ArrayList<>();
    }

    public Tag(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.noteIdList = new ArrayList<>();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public List<Note> getNoteIdList() {
        return this.noteIdList;
    }

    public void setNoteIdList(ArrayList<Note> noteIdList) {
        this.noteIdList = noteIdList;
    }

    public void addNote(Note note) {
        noteIdList.add(note);
    }

    public void deleteNote(Note note) {
        noteIdList.removeIf(noteId -> Objects.equals(noteId, note.getId().toString()));
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }




}
