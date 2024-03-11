package com.kneuen.smartnotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Html;
import android.text.Spanned;

import java.util.Collections;
import java.util.Comparator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    static final String PREFS_NAME = "NotePrefs";
    static final String KEY_NOTE_COUNT = "NoteCount";
    private LinearLayout notesContainer;
    static List<Note> noteList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AuthenticationCall();

        notesContainer = findViewById(R.id.notesContainer);
        Button saveButton = findViewById(R.id.saveButton);

        noteList = new ArrayList<>();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        Button newNoteButton = findViewById(R.id.newNoteButton);

        newNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNote();
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
            String htmlContent = sharedPreferences.getString("note_content_" + i, "");
            Spanned content = Html.fromHtml(htmlContent);
            String uuid = sharedPreferences.getString("note_uuid" + i, UUID.randomUUID().toString());
            Long creationTime = sharedPreferences.getLong("note_creation_time" + i, System.currentTimeMillis());
            boolean isPinned = sharedPreferences.getBoolean("note_pinned_" + i, false);
            Note note = new Note();

            note.setTitle(title);
            note.setContent(htmlContent);
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

    private void addNote() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Note");

        View dialogView = getLayoutInflater().inflate(R.layout.new_note, null);
        final EditText editTitle = dialogView.findViewById(R.id.editTitle);

        // New Note Object
        Note note = new Note();

        builder.setView(dialogView)
                .setPositiveButton("Save", (dialog, id) -> {
                    note.setTitle(editTitle.getText().toString());
                    note.setContent("");
                    note.setCreationTime(System.currentTimeMillis());

                    noteList.add(note);
                    saveNotesToPreferences();
                    goEditActivity(note);
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        builder.create().show();
    }

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

    private void AuthenticationCall() {

        int allowedAuthenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK |
                BiometricManager.Authenticators.DEVICE_CREDENTIAL;

        // Methods to notify user in case of these scenarios Optional
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate(allowedAuthenticators)) {
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
//                notifyUser("No Biometric Assigned");
                break;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
            case BiometricManager.BIOMETRIC_SUCCESS:
                break;
        }




        // Authentication Protocols
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Please Verify")
                .setDescription("User Authentication Required")
                .setAllowedAuthenticators(allowedAuthenticators)
                .build();

        getPrompt().authenticate(promptInfo);
    }

    // Function calling to verify identity
    private BiometricPrompt getPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt.AuthenticationCallback callback = new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                notifyUser(errString.toString());
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                // Action to accomplish
                notifyUser("Authentication Succeeded");
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                notifyUser("Authentication Failed");
            }
        };

        return new BiometricPrompt(this, executor, callback);
    }

    // Toast to notify User
    private void notifyUser(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}