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
public class TrashLoader extends AsyncTaskLoader<List<DeletedNote>> {
    private List<DeletedNote> mDeletedNotes;
    private ContentResolver mContentResolver;
    private Cursor mCursor;

    public TrashLoader(Context context, ContentResolver cr) {
        super(context);
        this.mContentResolver = cr;
    }

    @Override
    public List<DeletedNote> loadInBackground() {
        List<DeletedNote> entries = new ArrayList<>();
        String[] projection = {BaseColumns._ID,
                TrashContract.TrashColumns.TRASH_TITLE,
                TrashContract.TrashColumns.TRASH_DESCRIPTION,
                TrashContract.TrashColumns.TRASH_DATE_TIME
        };
        Uri uri = TrashContract.URI_TABLE;
        mCursor = mContentResolver.query(uri, projection, null, null, BaseColumns._ID + " DESC");
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    String title = mCursor.getString(mCursor.getColumnIndex(TrashContract.TrashColumns.TRASH_TITLE));
                    String description = mCursor.getString(mCursor.getColumnIndex(TrashContract.TrashColumns.TRASH_DESCRIPTION));
                    String dateTime = mCursor.getString(mCursor.getColumnIndex(TrashContract.TrashColumns.TRASH_DATE_TIME));
                    int _id = mCursor.getInt(mCursor.getColumnIndex(BaseColumns._ID));
                    DeletedNote trashNote = new DeletedNote( _id, title, description, dateTime);
                    entries.add(trashNote);

                } while (mCursor.moveToNext());
            }
        }
        return entries;
    }

    @Override
    public void deliverResult(List<DeletedNote> trash) {
        if (isReset()) {
            if(trash != null) {
                releaseResources();
                return;
            }
        }
        List<DeletedNote> oldDeletedNotes = mDeletedNotes;
        mDeletedNotes = trash;
        if(isStarted()) {
            super.deliverResult(trash);
        }
        if(oldDeletedNotes != null && oldDeletedNotes != trash) {
            releaseResources();
        }
    }

    @Override
    protected void onStartLoading() {
        if(mDeletedNotes != null) {
            deliverResult(mDeletedNotes);
        }
        if(takeContentChanged()) {
            forceLoad();
        } else if(mDeletedNotes == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        if(mDeletedNotes != null) {
            releaseResources();
            mDeletedNotes = null;
        }
    }


    @Override
    public void onCanceled(List<DeletedNote> trash) {
        super.onCanceled(trash);
        releaseResources();
    }

    @Override
    public void forceLoad() {
        super.forceLoad();
    }

    private void releaseResources() {
        mCursor.close();
    }
}
