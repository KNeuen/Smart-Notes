package com.kneuen.smartnotes;

import static com.kneuen.smartnotes.MainActivity.KEY_NOTE_COUNT;
import static com.kneuen.smartnotes.MainActivity.PREFS_NAME;
import static com.kneuen.smartnotes.MainActivity.noteList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.widget.EditText;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import java.util.regex.Matcher;

public class NoteEditingActivity extends AppCompatActivity {
    Note note;
    private EditText editContent;



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
        editContent = findViewById(R.id.contentEdit);

        editTitle.setText(note.getTitle());



        // Convert the saved HTML content back to a Spanned object for display
        Spanned formattedContent = Html.fromHtml(note.getContent(), Html.FROM_HTML_MODE_COMPACT);
        refreshContentText(createLinks(formattedContent));




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
                Spanned spannableString = new SpannableStringBuilder(s);
                note.setContent((Html.toHtml(spannableString, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)));
                saveNotesToPreferences();

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // In the event the enter or space key is pressed
        editContent.setOnKeyListener((v, actionId, event) -> {
            if (actionId == KeyEvent.KEYCODE_SPACE || actionId == KeyEvent.KEYCODE_ENTER) {
                refreshContentText(createLinks(editContent.getText()));
            }
            return false;
        });

    }

    // Used to check for links and refresh the content
    private void refreshContentText(Spanned spannableString) {

        int cursorPosition = editContent.getSelectionStart();
        editContent.setText(spannableString);
        editContent.setSelection(cursorPosition);
    }

    private Spanned createLinks(Spanned spannedString) {
        try {
            Matcher matcher = Patterns.WEB_URL.matcher(spannedString);
            SpannableStringBuilder editedSpanned = new SpannableStringBuilder(spannedString);

            int matchStart, matchEnd;

            while (matcher.find()) {
                matchStart = matcher.start(1);
                matchEnd = matcher.end();

                String url = spannedString.subSequence(matchStart, matchEnd).toString();
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;

                }
                ClickableSpan clickableSpan = getClickableSpan(url);
                editedSpanned.setSpan(clickableSpan, matchStart, matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            }


            spannedString = editedSpanned;
        } catch (Exception ignored){

        }


        return spannedString;
    }

    @NonNull
    private ClickableSpan getClickableSpan(String url) {
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);

            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.BLUE);
            }
        };
    }

    private void saveNotesToPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //Log.d("Saved", note.getContent());

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
        Spannable spannableString = new SpannableStringBuilder(editContent.getText());
        spannableString.setSpan(new StyleSpan(Typeface.BOLD),
                editContent.getSelectionStart(),
                editContent.getSelectionEnd(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

//        editContent.setText(spannableString);
        note.setContent(Html.toHtml(spannableString, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));

        saveNotesToPreferences();

        refreshContentText(spannableString);



        }
    public void buttonItalics(View view){
        Spannable spannableString = new SpannableStringBuilder(editContent.getText());
        spannableString.setSpan(new StyleSpan(Typeface.ITALIC),
                editContent.getSelectionStart(),
                editContent.getSelectionEnd(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        note.setContent(Html.toHtml(spannableString, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));
        saveNotesToPreferences();

        refreshContentText(spannableString);

    }
    public void buttonUnderline(View view){
        Spannable spannableString = new SpannableStringBuilder(editContent.getText());
        spannableString.setSpan(new UnderlineSpan(),
                editContent.getSelectionStart(),
                editContent.getSelectionEnd(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        note.setContent(Html.toHtml(spannableString, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));
        saveNotesToPreferences();

        refreshContentText(spannableString);
    }

    public void buttonNoFormat(View view){
        int start = editContent.getSelectionStart();
        int end = editContent.getSelectionEnd();
        Editable editable = editContent.getText();

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
        Spannable spannableString = new SpannableStringBuilder(editContent.getText());
        spannableString.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
                editContent.getSelectionStart(),
                editContent.getSelectionEnd(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        note.setContent(Html.toHtml(spannableString, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));
        saveNotesToPreferences();

        refreshContentText(spannableString);
    }

    public void buttonAlignmentCenter(View view){
        Spannable spannableString = new SpannableStringBuilder(editContent.getText());
        spannableString.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER),
                editContent.getSelectionStart(),
                editContent.getSelectionEnd(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        note.setContent(Html.toHtml(spannableString, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));
        saveNotesToPreferences();

        refreshContentText(spannableString);
    }

    public void buttonAlignmentRight(View view){
        Spannable spannableString = new SpannableStringBuilder(editContent.getText());
        spannableString.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                editContent.getSelectionStart(),
                editContent.getSelectionEnd(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        note.setContent(Html.toHtml(spannableString, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL));
        saveNotesToPreferences();

        refreshContentText(spannableString);
    }

}