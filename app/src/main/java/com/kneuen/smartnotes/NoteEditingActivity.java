package com.kneuen.smartnotes;

import static com.kneuen.smartnotes.MainActivity.KEY_NOTE_COUNT;
import static com.kneuen.smartnotes.MainActivity.PREFS_NAME;
import static com.kneuen.smartnotes.MainActivity.noteList;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.EditText;

import java.util.UUID;

public class NoteEditingActivity extends AppCompatActivity {
    Note note;
    private EditText editText;


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

        editText = editContent;

        // Convert the saved HTML content back to a Spanned object for display
        Spanned formattedContent = Html.fromHtml(note.getContent());
        editContent.setText(formattedContent);

        editText = editContent;



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

    }

    private void saveNotesToPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_NOTE_COUNT, noteList.size());
        for (int i = 0; i < noteList.size(); i++) {
            Note note = noteList.get(i);
            editor.putString("note_title_" + i, note.getTitle());
            editor.putString("note_content_" + i, note.getContent());
            editor.putBoolean("note_pinned_" + i, note.isPinned());
            editor.putString("note_uuid" + i, note.getId().toString());
        }
        editor.apply();
    }

    public void buttonBold(View view) {
        Spannable spannableString = new SpannableStringBuilder(editText.getText());
        spannableString.setSpan(new StyleSpan(Typeface.BOLD),
                editText.getSelectionStart(),
                editText.getSelectionEnd(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        editText.setText(spannableString);
        note.setContent(Html.toHtml(spannableString));
        saveNotesToPreferences();


//        String htmlString = Html.toHtml(spannableString);
//        Log.d("html", htmlString);
//
//        String testing = "testtest";
//        Spanned test = Html.fromHtml(htmlString);

///        editText.setText(spannableString)
        }
    public void buttonItalics(View view){
        Spannable spannableString = new SpannableStringBuilder(editText.getText());
        spannableString.setSpan(new StyleSpan(Typeface.ITALIC),
                editText.getSelectionStart(),
                editText.getSelectionEnd(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        editText.setText(spannableString);
        note.setContent(Html.toHtml(spannableString));
        saveNotesToPreferences();

    }
    public void buttonUnderline(View view){
        Spannable spannableString = new SpannableStringBuilder(editText.getText());
        spannableString.setSpan(new UnderlineSpan(),
                editText.getSelectionStart(),
                editText.getSelectionEnd(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        editText.setText(spannableString);
        note.setContent(Html.toHtml(spannableString));
        saveNotesToPreferences();
    }

    public void buttonNoFormat(View view){
        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        Editable editable = editText.getText();

        // Remove all spans within the selected range
        StyleSpan[] styleSpans = editable.getSpans(start, end, StyleSpan.class);
        for (StyleSpan span : styleSpans) {
            editable.removeSpan(span);
        }

        UnderlineSpan[] underlineSpans = editable.getSpans(start, end, UnderlineSpan.class);
        for (UnderlineSpan span : underlineSpans) {
            editable.removeSpan(span);
        }

        note.setContent(Html.toHtml(editable));
        saveNotesToPreferences();
    }


    public void buttonAlignmentLeft(View view){
        editText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        Spannable spannableString = new SpannableStringBuilder(editText.getText());
        editText.setText(spannableString);
    }

    public void buttonAlignmentCenter(View view){
        editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        Spannable spannableString = new SpannableStringBuilder(editText.getText());
        editText.setText(spannableString);
    }

    public void buttonAlignmentRight(View view){
        editText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        Spannable spannableString = new SpannableStringBuilder(editText.getText());
        editText.setText(spannableString);
    }

}