package com.kneuen.smartnotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Html;
import android.text.Spanned;
import android.graphics.Color;

import android.widget.DatePicker;
//import android.widget.TimePicker;
import java.util.Calendar;
import android.provider.CalendarContract;
import android.provider.AlarmClock;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    static final String PREFS_NAME = "NotePrefs";
    static final String KEY_NOTE_COUNT = "NoteCount";
    private LinearLayout notesContainer;
    static List<Note> noteList;
    private List<Tag> tagList;

    private Gson gson;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AuthenticationCall();

        notesContainer = findViewById(R.id.notesContainer);

        noteList = new ArrayList<>();
        tagList = new ArrayList<>();

        gson = new Gson();


        Button newNoteButton = findViewById(R.id.newNoteButton);

        newNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNote();
            }
        });

        Button newTagButton = findViewById(R.id.newTagButton);
        newTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTag();
            }
        });

        Button deleteTagButton = findViewById(R.id.deleteTagButton);

        deleteTagButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Spinner tagSpinner = findViewById(R.id.tagSpinner);

               String item = tagSpinner.getSelectedItem().toString();

               if (item.equals("All Notes")) {
                   notifyUser("Cannot Delete All Notes");
               }
               else {
                   for (Tag tag : tagList) {
                       if (Objects.equals(tag.getName(), item)) {
                           showTagDeleteDialog(tag);
                       }
                   }
               }


           }
       });

        loadFromPreferences();
        displayNotes(noteList);
        setTagSpinner();
    }

    private void deleteTagAndRefresh(Tag tag) {
        for (Note note : tag.getNoteIdList()) {
            note.deleteTag();
        }
        tagList.remove(tag);
        saveToPreferences();
        refreshNoteViews();
    }


    private void addTag() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Tag");

        View dialogView = getLayoutInflater().inflate(R.layout.new_tag, null);
        final EditText editTitle = dialogView.findViewById(R.id.editTitle);

        // Dropdown for color selection
        Spinner colorSpinner = dialogView.findViewById(R.id.colorSpinner);
        String[] colors = {"Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet"};
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, colors);
        colorSpinner.setAdapter(colorAdapter);

        // New Note Object
        Tag tag = new Tag();

        builder.setView(dialogView)
                .setPositiveButton("Save", (dialog, id) -> {
                    String name = editTitle.getText().toString();
                    String selectedColor = colorSpinner.getSelectedItem().toString();
                    int allowed = 1;
                    for (Tag curtag : tagList) {
                        if (Objects.equals(curtag.getName(), name) || name.equals("All Notes")) {
                            notifyUser("Tag name already exists");
                            allowed = 0;
                        }
                    }
                    if (allowed == 1) {
                        tag.setName(name);
                        tag.setColor(selectedColor); // Set the color of the tag
                        tagList.add(tag);
                        setTagSpinner();
                    }
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.cancel());

        builder.create().show();

        saveToPreferences();
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Make sure when Note list always updated
        refreshNoteViews();
    }

    private void displayNotes(List<Note> curNoteList) {
        for (Note note : curNoteList) {
            createNoteView(note);
        }
    }

    private void setTagSpinner() {
        Spinner tagSpinner = findViewById(R.id.tagSpinner);
        List<String> tagNames = new ArrayList<>();

        tagNames.add("All Notes");

        for (Tag tag : tagList) {
            tagNames.add(tag.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                tagNames
        );



        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String item = adapterView.getItemAtPosition(position).toString();
                if (position == 0) {
                    notesContainer.removeAllViews();
                    displayNotes(noteList);
                }
                else {
                    for (Tag tag : tagList) {
                        if(Objects.equals(tag.getName(), item)) {
                            notesContainer.removeAllViews();
                            displayNotes(tag.getNoteIdList());
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                notesContainer.removeAllViews();
                displayNotes(noteList);
            }
        });

        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        tagSpinner.setAdapter(adapter);

    }


    private void loadFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int noteCount = sharedPreferences.getInt(KEY_NOTE_COUNT, 0);

        for (int i = 0; i < noteCount; i++) {
            String title = sharedPreferences.getString("note_title_" + i, "");
            String htmlContent = sharedPreferences.getString("note_content_" + i, "");
            Spanned content = Html.fromHtml(htmlContent);
            String tag = sharedPreferences.getString("note_tag" + i, "");
            String uuid = sharedPreferences.getString("note_uuid" + i, UUID.randomUUID().toString());
            long creationTime = sharedPreferences.getLong("note_creation_time" + i, System.currentTimeMillis());
            boolean isPinned = sharedPreferences.getBoolean("note_pinned_" + i, false);
            Note note = new Note();

            note.setTitle(title);
            note.setContent(htmlContent);
            note.setPinned(isPinned);
            note.setId(UUID.fromString(uuid));
            note.setCreationTime(creationTime);
            note.setTag(tag);

            noteList.add(note);

        }

        int tagCount = sharedPreferences.getInt("TagCount", 0);
        Type type = new TypeToken<ArrayList<Note>>() {}.getType();

        for (int i = 0; i < tagCount; i++) {


            String id = sharedPreferences.getString("tag_id" + i, "");
            String name = sharedPreferences.getString("tag_name" + i, "");
            //Log.d("why", name);
            String color = sharedPreferences.getString("tag_color" + i, ""); //Retrieve Color

            String json_list = sharedPreferences.getString("tag_note_list" + i, "");
            ArrayList<Note> noteList = gson.fromJson(json_list, type);

            Tag tag = new Tag();
            tag.setId(id);
            tag.setName(name);
            tag.setColor(color); // Set the color
            tag.setNoteIdList(noteList);

            tagList.add(tag);
        }

    }


    private void createNoteView(final Note note) {
        View noteView = getLayoutInflater().inflate(R.layout.note_item, null);
        TextView titleTextView = noteView.findViewById(R.id.titleTextView);
        titleTextView.setText(note.getTitle());
        TextView dateTextView = noteView.findViewById(R.id.dateTextView);
        TextView tagView = noteView.findViewById(R.id.tagView);

        titleTextView.setText(note.getTitle());
        dateTextView.setText(note.getFormattedCreationTime());

        for (Tag tag : tagList) {
            if (Objects.equals(tag.getId(), note.getTag())) {
                String tagString = "Tag: " + tag.getName();
                tagView.setText(tagString);
                tagView.setBackgroundColor(getColorCode(tag.getColor())); // Set the background color
            }
        }

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

        final Button setDateButton = dialogView.findViewById(R.id.setDateButton);
        //final Button setTimeButton = dialogView.findViewById(R.id.setTimeButton);
        final DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        //final TimePicker timePicker = dialogView.findViewById(R.id.timePicker);

        setDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Toggle DatePicker visibility
                if (datePicker.getVisibility() == View.GONE) {
                    datePicker.setVisibility(View.VISIBLE);
                } else {
                    datePicker.setVisibility(View.GONE);
                }
            }
        });

        //setTimeButton.setOnClickListener(new View.OnClickListener() {
            //@Override
            //public void onClick(View v) {
                // Toggle TimePicker visibility
                //if (timePicker.getVisibility() == View.GONE) {
                    //timePicker.setVisibility(View.VISIBLE);
                    //timePicker.setVisibility(View.VISIBLE);
                //} else {
                    //timePicker.setVisibility(View.GONE);
                //}
            //}
        //});

        // New Note Object
        Note note = new Note();

// Set the creation time to current time by default
        note.setCreationTime(System.currentTimeMillis());

        builder.setView(dialogView)
                .setPositiveButton("Save", (dialog, id) -> {
                    note.setTitle(editTitle.getText().toString());
                    note.setContent("");

                    // After saving the note, set a calendar event
                    addEventToCalendar(note);

                    if (datePicker.getVisibility() == View.VISIBLE) {
                        // Get the date and time from the DatePicker and TimePicker
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.YEAR, datePicker.getYear());
                        calendar.set(Calendar.MONTH, datePicker.getMonth());
                        calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());

                        //if (timePicker.getVisibility() == View.VISIBLE) {
                            //calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                            //calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                        //}

                        // Override the creation time with the selected date and time
                        note.setCreationTime(calendar.getTimeInMillis());
                    }

                    //boolean dateSet = datePicker.getVisibility() == View.VISIBLE;
                    //boolean timeSet = timePicker.getVisibility() == View.VISIBLE;

                    //if (dateSet) {
                        // Update the calendar with the selected date
                    //    calendar.set(Calendar.YEAR, datePicker.getYear());
                    //    calendar.set(Calendar.MONTH, datePicker.getMonth());
                    //    calendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                    //}

                    //if (timeSet) {
                        // Update the calendar with the selected time
                        //calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                        //calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                    //} else if (dateSet) {
                        // If date is set but time is not, reset time to midnight
                        //calendar.set(Calendar.HOUR_OF_DAY, 0);
                        //calendar.set(Calendar.MINUTE, 0);
                    //}

                    // Set the note's creation time
                    //note.setCreationTime(calendar.getTimeInMillis());

                    noteList.add(note);
                    saveToPreferences();
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

    private void showTagDeleteDialog(final Tag tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete this tag.");
        builder.setMessage("Are you sure you want to delete this tag?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteTagAndRefresh(tag);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showNoteOptionsDialog(final Note note) {
        String[] options = note.isPinned() ?
                new String[]{"Delete", "Unpin", "Change Tag", "Change Date"} :
                new String[]{"Delete", "Pin", "Change Tag", "Change Date"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Note Options");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Delete
                        showDeleteDialog(note);
                        break;
                    case 1: // Pin/Unpin
                        if (note.isPinned()) {
                            unpinNote(note);
                        } else {
                            pinNote(note);
                        }
                        break;
                    case 2: // Change Tag
                        selectTag(note);
                        break;
                    case 3: // Change Date
                        changeNoteDate(note);
                        break;
                }
            }
        });

        builder.show();
    }

    private int selectedItemIndex = -1;

    private void selectTag(final Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tags");
        List<String> tagNames = new ArrayList<>();
        Tag previousTag = new Tag();

        for (Tag tag : tagList) {
            tagNames.add(tag.getName());
            if (Objects.equals(tag.getId(), note.getTag())) {
                previousTag = tag;
            }
        }
        String[] stringArray = tagNames.toArray(new String[0]);
        Tag finalPreviousTag = previousTag;

        builder
                .setPositiveButton("Change Tag", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (selectedItemIndex != -1) {
                            Tag tag = tagList.get(selectedItemIndex);
                            finalPreviousTag.deleteNote(note);

                            note.setTag(tag.getId());
                            tag.addNote(note);
                            refreshNoteViews();
                            saveToPreferences();
                        }

                        else {
                            notifyUser("Invalid Selection");
                        }
                    }
                })
                .setNegativeButton("Reset Tag", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        note.deleteTag();
                        refreshNoteViews();
                        saveToPreferences();
                    }
                })

                .setSingleChoiceItems(stringArray, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedItemIndex = i;
                    }
        });

        builder.show();
    }

    private void pinNote(Note note) {
        note.setPinned(true);
        reorderNotes();
        saveToPreferences();
        refreshNoteViews();
    }

    private void unpinNote(Note note) {
        note.setPinned(false);
        reorderNotes();
        saveToPreferences();
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
        saveToPreferences();
        refreshNoteViews();
    }

    private void refreshNoteViews() {
        notesContainer.removeAllViews();
        displayNotes(noteList);
        setTagSpinner();
    }

    private void saveToPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt(KEY_NOTE_COUNT, noteList.size());
        for (int i = 0; i < noteList.size(); i ++) {
            Note note = noteList.get(i);
            editor.putString("note_title_" + i, note.getTitle());
            editor.putString("note_content_" + i, note.getContent());
            editor.putBoolean("note_pinned_" + i, note.isPinned());
            editor.putString("note_uuid" + i, note.getId().toString());
            editor.putString("note_tag" + i, note.getTag());
            editor.putLong("note_creation_time" + i, note.getCreationTime());
        }

        editor.putInt("TagCount", tagList.size());
        for (int i = 0; i < tagList.size(); i ++) {
            Tag tag = tagList.get(i);
            editor.putString("tag_id" + i, tag.getId());
            editor.putString("tag_name" + i, tag.getName());
            editor.putString("tag_color" + i, tag.getColor()); // Save the color
            editor.putString("tag_note_list" + i, gson.toJson(tag.getNoteIdList()));

        }
        editor.apply();
    }

    private int getColorCode(String colorName) {
        switch (colorName.toLowerCase()) {
            case "red":
                return Color.parseColor("#FFCDD2"); // Light Red
            case "orange":
                return Color.parseColor("#FFE0B2"); // Light Orange
            case "yellow":
                return Color.parseColor("#FFF9C4"); // Light Yellow
            case "green":
                return Color.parseColor("#C8E6C9"); // Light Green
            case "blue":
                return Color.parseColor("#BBDEFB"); // Light Blue
            case "indigo":
                return Color.parseColor("#C5CAE9"); // Light Indigo
            case "violet":
                return Color.parseColor("#E1BEE7"); // Light Violet
            default:
                return Color.TRANSPARENT; // Default color if none matched
        }
    }

    private void addEventToCalendar(Note note) {
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTimeInMillis(note.getReminderDate());

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, note.getTitle())
                .putExtra(CalendarContract.Events.DESCRIPTION, note.getContent());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            notifyUser("Calendar app is not available");
        }
    }

    private void setAlarm(Note note) {
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.setTimeInMillis(note.getReminderDate());

        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM)
                .putExtra(AlarmClock.EXTRA_HOUR, alarmTime.get(Calendar.HOUR_OF_DAY))
                .putExtra(AlarmClock.EXTRA_MINUTES, alarmTime.get(Calendar.MINUTE))
                .putExtra(AlarmClock.EXTRA_MESSAGE, note.getTitle());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            notifyUser("Alarm app is not available");
        }
    }

    private void changeNoteDate(Note note) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        // Update the note's creation time
                        note.setCreationTime(calendar.getTimeInMillis());
                        saveToPreferences();
                        refreshNoteViews();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
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