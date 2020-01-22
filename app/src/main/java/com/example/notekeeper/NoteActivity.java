package com.example.notekeeper;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.List;

public class NoteActivity extends AppCompatActivity {
    public static final String NOTE_POSITION = "com.example.notekeeper.NOTE_INFO";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.example.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.example.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.example.notekeeper.ORIGINAL_NOTE_TEXT";
    private NoteInfo mNote;
    private boolean mIsNewNote;
    public static final int POSITION_NOT_SET = -1;
    private Spinner mspinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean isCancelling;
    private String mOriginalNoteCoursesId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mspinnerCourses = findViewById(R.id.spinner_courses);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mspinnerCourses.setAdapter(adapterCourses);

        readDisplayStateValue();
        if (savedInstanceState == null){
            saveOrginalNoteValues();
        }else{
            restoreOriginalNoteValue(savedInstanceState);

        }
        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if (!mIsNewNote)
            displayNote(mspinnerCourses, mTextNoteTitle, mTextNoteText);

    }

    private void restoreOriginalNoteValue(Bundle savedInstanceState) {
        mOriginalNoteCoursesId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteCoursesId = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteCoursesId = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCoursesId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);

        super.onSaveInstanceState(outState);
    }

    private void saveOrginalNoteValues() {
        if (mIsNewNote)
            return;

        mOriginalNoteCoursesId = mNote.getCourse().getCourseId();
        mOriginalNoteTitle = mNote.getTitle();
        mOriginalNoteText = mNote.getText();

    }

    private void displayNote(Spinner spinner, EditText textNoteTitle, EditText textNoteText) {

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());
        spinner.setSelection(courseIndex);


        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isCancelling) {
            if (mIsNewNote) {
                DataManager.getInstance().removeNote(mNotePosition);
            } else {
                storePreviousNoteValues();
            }

        } else {
            saveNote();
        }
        saveNote();
    }

    private void storePreviousNoteValues() {

        CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCoursesId);
        mNote.setCourse(course);
        mNote.setTitle(mOriginalNoteTitle);
        mNote.setText(mOriginalNoteText);

    }

    private void saveNote() {
        mNote.setCourse((CourseInfo) mspinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());
    }

    public void readDisplayStateValue() {
        Intent intent = getIntent();
        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);
        mIsNewNote = position == POSITION_NOT_SET;
        if (mIsNewNote) {
            creatNewNote();


        } else {
            mNote = DataManager.getInstance().getNotes().get(position);
        }


    }

    private void creatNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
        mNote = dm.getNotes().get(mNotePosition);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            isCancelling = true;
            finish();

        }

        return super.onOptionsItemSelected(item);
    }

    private void sendEmail() {

        CourseInfo courseInfo = (CourseInfo) mspinnerCourses.getSelectedItem();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Check this out \"" + courseInfo.getTitle() + "  \"\" " + mTextNoteText.getText().toString();

        Intent intentExtra = new Intent(Intent.ACTION_SEND);
        intentExtra.setType("message/rfc2822");
        intentExtra.putExtra(Intent.EXTRA_SUBJECT, subject);
        intentExtra.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intentExtra);

    }
}
