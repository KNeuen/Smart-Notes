package com.kneuen.smartnotes;

import static com.kneuen.smartnotes.MainActivity.KEY_NOTE_COUNT;
import static com.kneuen.smartnotes.MainActivity.PREFS_NAME;
import static com.kneuen.smartnotes.MainActivity.noteList;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;

import java.util.UUID;

public class NoteEditingActivity extends AppCompatActivity {
    Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editing);

        Intent intent = getIntent();
        String uuidString = intent.getStringExtra("UUID");

        // Find the right note
        for (Note curnote : noteList) {
            if (curnote.getId().toString().equals(uuidString)) {
                note = curnote;
            }
        }



        // Getting variables to edit
        EditText editTitle = findViewById(R.id.titleEdit);
        EditText editContent = findViewById(R.id.contentEdit);

        editTitle.setText(note.getTitle());
        editContent.setText(note.getContent());

        editTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                note.setTitle(String.valueOf(s));
                saveNotesToPreferences();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                note.setContent(String.valueOf(s));
                saveNotesToPreferences();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Initialize the bold button
        Button boldButton = findViewById(R.id.btn_bold);
        boldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                applyBoldStyle();
            }
        });

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
        }
        editor.apply();
    }

    private void applyBoldStyle() {
        EditText editContent = findViewById(R.id.contentEdit);
        int start = editContent.getSelectionStart();
        int end = editContent.getSelectionEnd();

        // Apply the bold style to the selected text
        SpannableString spannableString = new SpannableString(editContent.getText());
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        editContent.setText(spannableString);
        editContent.setSelection(start, end); // Keep the text selected
    }
}