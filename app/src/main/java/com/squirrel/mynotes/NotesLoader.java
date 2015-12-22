package com.squirrel.mynotes;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by squirrel on 12/16/15.
 */
public class NotesLoader extends AsyncTaskLoader<List<Note>> {

    private List<Note> mListOfNotes;
    private ContentResolver mContentResolver;
    private Cursor mCursor;
    private int mType; //reminder or note

    public NotesLoader(Context context, ContentResolver contentResolver, int type) {
        super(context);
        mContentResolver = contentResolver;
        mType = type;
    }

    @Override
    public List<Note> loadInBackground() {
        List<Note> notes = new ArrayList<Note>();
        String[] projection = {
                BaseColumns._ID,
                NotesContract.NotesColumns.NOTE_TITLE,
                NotesContract.NotesColumns.NOTE_DESCRIPTION,
                NotesContract.NotesColumns.NOTE_TYPE,
                NotesContract.NotesColumns.NOTE_DATE,
                NotesContract.NotesColumns.NOTE_TIME,
                NotesContract.NotesColumns.NOTE_IMAGE,
                NotesContract.NotesColumns.NOTE_IMAGE_STORAGE_SELECTION
        };
        Uri uri = NotesContract.URI_TABLE;
        mCursor = mContentResolver.query(uri, projection, null, null, BaseColumns._ID + " DESC");

        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    String date = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTE_DATE));
                    String time = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTE_TIME));
                    String type = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTE_TYPE));
                    String title = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTE_TITLE));
                    String description = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTE_DESCRIPTION));
                    String imagePath = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTE_IMAGE));
                    int imageStorageSelection = mCursor.getInt(mCursor.getColumnIndex(NotesContract.NotesColumns.NOTE_IMAGE_STORAGE_SELECTION));
                    int _id = mCursor.getInt(mCursor.getColumnIndex(BaseColumns._ID));

                    if (mType == BaseActivity.NOTES) {
                        if (time == Constants.TIME_NOT_SET) {
                            time = "";
                            Note note = new Note(title, description, date, time, _id, imageStorageSelection, type);
                            if (!imagePath.equals(Constants.IMAGE_NOT_SET)) {
                                if (imageStorageSelection == Constants.DEVICE_SELECTION) {
                                    note.setBitmap(imagePath);
                                } else {
                                    //dropbox, gdrive image
                                    note.setImagePath(imagePath);
                                }
                            } else {
                                //there was no image
                                note.setImagePath(Constants.IMAGE_NOT_SET);
                            }

                            notes.add(note);
                        }

                    } else if (mType == BaseActivity.REMINDERS) {
                        if (!time.equals(Constants.TIME_NOT_SET)) {
                            Note note = new Note(title, description, date, time, _id, imageStorageSelection, type);
                            if (!imagePath.equals(Constants.IMAGE_NOT_SET)) {
                                if (imageStorageSelection == Constants.DEVICE_SELECTION) {
                                    note.setBitmap(imagePath);
                                } else {
                                    //dropbox, gdrive image
                                    note.setImagePath(imagePath);
                                }
                            } else {
                                //there was no image
                                note.setImagePath(Constants.IMAGE_NOT_SET);
                            }

                            notes.add(note);
                        }

                    } else {
                        throw new IllegalArgumentException("Invalid type");
                    }

                } while (mCursor.moveToNext());
            }
        }

        return notes;
    }

    @Override
    public void deliverResult(List<Note> data) {
        if (isReset()) {
            if (data != null) {
                releaseResources();
                return;
            }
        }
        List<Note> oldNotes = mListOfNotes;
        mListOfNotes = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
        if (oldNotes != null && oldNotes != data) {
            releaseResources();
        }
    }

    @Override
    protected void onStartLoading() {
        if (mListOfNotes != null) {
            deliverResult(mListOfNotes);
        }
        if (takeContentChanged()) {
            forceLoad();
        } else if (mListOfNotes == null) {
            forceLoad();
        }
    }

    @Override
    public void onCanceled(List<Note> data) {
        super.onCanceled(data);
        releaseResources();
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if (mListOfNotes != null) {
            releaseResources();
            mListOfNotes = null;
        }
    }

    private void releaseResources() {
        mCursor.close();
    }

}
