package com.kneuen.smartnotes;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Collections;
import java.util.Comparator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    static final String PREFS_NAME = "NotePrefs";
    static final String KEY_NOTE_COUNT = "NoteCount";
    private LinearLayout notesContainer;
    static List<Note> noteList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notesContainer = findViewById(R.id.notesContainer);
        Button saveButton = findViewById(R.id.saveButton);

        noteList = new ArrayList<>();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        loadNotesFromPreferences();
        displayNotes();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Make sure when Note list always updated
        refreshNoteViews();
    }

    private void displayNotes() {
        for (Note note : noteList) {
            createNoteView(note);
        }
    }

    private void loadNotesFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int noteCount = sharedPreferences.getInt(KEY_NOTE_COUNT, 0);

        for (int i = 0; i < noteCount; i++) {
            String title = sharedPreferences.getString("note_title_" + i, "");
            String content = sharedPreferences.getString("note_content_" + i, "");
            String uuid = sharedPreferences.getString("note_uuid" + i, UUID.randomUUID().toString());
            Long creationTime = sharedPreferences.getLong("note_creation_time" + i, System.currentTimeMillis());
            boolean isPinned = sharedPreferences.getBoolean("note_pinned_" + i, false);
            Note note = new Note();

            note.setTitle(title);
            note.setContent(content);
            note.setPinned(isPinned);
            note.setId(UUID.fromString(uuid));
            note.setCreationTime(creationTime);

            noteList.add(note);

        }
    }

    private void saveNote() {
        EditText titleEditText = findViewById(R.id.titleEditText);
        EditText contentEditText = findViewById(R.id.contentEditText);

        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();

        if (!title.isEmpty() && !content.isEmpty()) {
            Note note = new Note();
            note.setTitle(title);
            note.setContent(content);
            note.setCreationTime(System.currentTimeMillis());

            noteList.add(note);
            saveNotesToPreferences();

            createNoteView(note);
            clearInputFields();
        }
    }

    private void clearInputFields() {
        EditText titleEditText = findViewById(R.id.titleEditText);
        EditText contentEditText = findViewById(R.id.contentEditText);

        titleEditText.getText().clear();
        contentEditText.getText().clear();
    }

    private void createNoteView(final Note note) {
        View noteView = getLayoutInflater().inflate(R.layout.note_item, null);
        TextView titleTextView = noteView.findViewById(R.id.titleTextView);
        titleTextView.setText(note.getTitle());
        TextView dateTextView = noteView.findViewById(R.id.dateTextView);

        titleTextView.setText(note.getTitle());
        dateTextView.setText(note.getFormattedCreationTime());



        noteView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showNoteOptionsDialog(note);
                return true;
            }
        });

        noteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showEditDialog(note);
                goEditActivity(note);
            }
        });

        notesContainer.addView(noteView);
    }

//    private void showEditDialog(final Note note) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Edit Note");
//
//        View dialogView = getLayoutInflater().inflate(R.layout.note_edit, null);
//        final EditText editTitle = dialogView.findViewById(R.id.editTitle);
//        final EditText editContent = dialogView.findViewById(R.id.editContent);
//
//        editTitle.setText(note.getTitle());
//        editContent.setText(note.getContent());
//
//        builder.setView(dialogView)
//                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        note.setTitle(editTitle.getText().toString());
//                        note.setContent(editContent.getText().toString());
//                        saveNotesToPreferences();
//                        refreshNoteViews();
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
//
//        builder.create().show();
//    }

    private void goEditActivity(final Note note) {
        UUID uuid = note.getId();

        Intent intent = new Intent(getApplicationContext(), NoteEditingActivity.class);
        intent.putExtra("UUID", uuid.toString());

        startActivity(intent);

    }
    private void showNoteOptionsDialog(final Note note) {
        String[] options = note.isPinned() ? new String[]{"Delete", "Unpin"} : new String[]{"Delete", "Pin"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Note Options");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) { // Delete option
                    showDeleteDialog(note);
                } else if (which == 1) { // Pin/Unpin option
                    if (note.isPinned()) {
                        unpinNote(note);
                    } else {
                        pinNote(note);
                    }
                }
            }
        });
        builder.show();
    }

    private void pinNote(Note note) {
        note.setPinned(true);
        reorderNotes();
        saveNotesToPreferences();
        refreshNoteViews();
    }

    private void unpinNote(Note note) {
        note.setPinned(false);
        reorderNotes();
        saveNotesToPreferences();
        refreshNoteViews();
    }

    private void reorderNotes() {
        Collections.sort(noteList, new Comparator<Note>() {
            @Override
            public int compare(Note n1, Note n2) {
                if (!n1.isPinned() && !n2.isPinned()) {
                    return Long.compare(n1.getCreationTime(), n2.getCreationTime());
                }
                return 0;
            }
        });

        Collections.sort(noteList, new Comparator<Note>() {
            @Override
            public int compare(Note n1, Note n2) {
                if (n1.isPinned() && !n2.isPinned()) {
                    return -1;
                } else if (!n1.isPinned() && n2.isPinned()) {
                    return 1;
                }
                return 0;
            }
        });
    }



    private void showDeleteDialog(final Note note) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete this note.");
        builder.setMessage("Are you sure you want to delete this note?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNoteAndRefresh(note);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteNoteAndRefresh(Note note) {
        noteList.remove(note);
        saveNotesToPreferences();
        refreshNoteViews();
    }

    private void refreshNoteViews() {
        notesContainer.removeAllViews();
        displayNotes();
    }

    private void saveNotesToPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_NOTE_COUNT, noteList.size());
        for (int i = 0; i < noteList.size(); i ++) {
            Note note = noteList.get(i);
            editor.putString("note_title_" + i, note.getTitle());
            editor.putString("note_content_" + i, note.getContent());
            editor.putBoolean("note_pinned_" + i, note.isPinned());
            editor.putString("note_uuid" + i, note.getId().toString());
            editor.putLong("note_creation_time" + i, note.getCreationTime());
        }
        editor.apply();
    }

}