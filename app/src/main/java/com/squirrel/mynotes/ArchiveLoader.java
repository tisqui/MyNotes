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
public class ArchiveLoader extends AsyncTaskLoader<List<ArchivedNote>> {
    private List<ArchivedNote> mArchives;
    private ContentResolver mContentResolver;
    private Cursor mCursor;

    public ArchiveLoader(Context context, ContentResolver cr) {
        super(context);
        this.mContentResolver = cr;
    }

    @Override
    public List<ArchivedNote> loadInBackground() {
        List<ArchivedNote> entries = new ArrayList<>();
        String[] projection = {BaseColumns._ID,
                ArchivesContract.ArchivesColumns.ARCHIVES_TITLE,
                ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION,
                ArchivesContract.ArchivesColumns.ARCHIVES_DATE_TIME,
                ArchivesContract.ArchivesColumns.ARCHIVES_CATEGORY,
                ArchivesContract.ArchivesColumns.ARCHIVES_TYPE
        };
        Uri uri = ArchivesContract.URI_TABLE;
        mCursor = mContentResolver.query(uri, projection, null, null, BaseColumns._ID + " DESC");
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                do {
                    String title = mCursor.getString(mCursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_TITLE));
                    String description = mCursor.getString(mCursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION));
                    String dateTime = mCursor.getString(mCursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_DATE_TIME));
                    String category = mCursor.getString(mCursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_CATEGORY));
                    String type = mCursor.getString(mCursor.getColumnIndex(ArchivesContract.ArchivesColumns.ARCHIVES_TYPE));
                    int _id = mCursor.getInt(mCursor.getColumnIndex(BaseColumns._ID));
                    ArchivedNote archive = new ArchivedNote( _id, title, description, dateTime, category, type);
                    entries.add(archive);

                } while (mCursor.moveToNext());
            }
        }
        return entries;
    }

    @Override
    public void deliverResult(List<ArchivedNote> archives) {
        if (isReset()) {
            if(archives != null) {
                releaseResources();
                return;
            }
        }
        List<ArchivedNote> oldArchives = mArchives;
        mArchives = archives;
        if(isStarted()) {
            super.deliverResult(archives);
        }
        if(oldArchives != null && oldArchives != archives) {
            releaseResources();
        }
    }

    @Override
    protected void onStartLoading() {
        if(mArchives != null) {
            deliverResult(mArchives);
        }
        if(takeContentChanged()) {
            forceLoad();
        } else if(mArchives == null) {
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
        if(mArchives != null) {
            releaseResources();
            mArchives = null;
        }
    }


    @Override
    public void onCanceled(List<ArchivedNote> archive) {
        super.onCanceled(archive);
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
