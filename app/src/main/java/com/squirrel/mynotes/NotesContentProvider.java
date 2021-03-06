package com.squirrel.mynotes;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by squirrel on 12/14/15.
 */
public class NotesContentProvider extends ContentProvider {
    protected AppDatabase mAppDatabase;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int NOTES = 1;
    private static final int NOTES_ID = 2;
    private static final int ARCHIVE = 3;
    private static final int ARCHIVE_ID = 4;
    private static final int TRASH = 5;
    private static final int TRASH_ID = 6;

    private static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        String authority = NotesContract.CONTENT_AUTHORITY;
        uriMatcher.addURI(authority, "notes", NOTES);
        uriMatcher.addURI(authority, "notes/*", NOTES_ID);

        authority = ArchivesContract.CONTENT_AUTHORITY;
        uriMatcher.addURI(authority, "archives", ARCHIVE);
        uriMatcher.addURI(authority, "archives/*", ARCHIVE_ID);

        authority = TrashContract.CONTENT_AUTHORITY;
        uriMatcher.addURI(authority, "trash", TRASH);
        uriMatcher.addURI(authority, "trash/*", TRASH_ID);

        return uriMatcher;
    }

    public void deleteDB(){
        mAppDatabase.close();
        AppDatabase.deleteDatabase(getContext());
        mAppDatabase = new AppDatabase(getContext());
    }

    @Override
    public boolean onCreate() {
        mAppDatabase = new AppDatabase(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mAppDatabase.getReadableDatabase();
        int match = sUriMatcher.match(uri);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch(match) {
            case NOTES:
                queryBuilder.setTables(AppDatabase.Tables.Notes);
                break;
            case NOTES_ID:
                queryBuilder.setTables(AppDatabase.Tables.Notes);
                String noteId = NotesContract.Notes.getNoteId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + "=" + noteId);
                break;
            case ARCHIVE:
                queryBuilder.setTables(AppDatabase.Tables.Archives);
                break;
            case ARCHIVE_ID:
                queryBuilder.setTables(AppDatabase.Tables.Archives);
                String archiveId = ArchivesContract.Archives.getArchiveId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + "=" + archiveId);
                break;
            case TRASH:
                queryBuilder.setTables(AppDatabase.Tables.Trash);
                break;
            case TRASH_ID:
                queryBuilder.setTables(AppDatabase.Tables.Trash);
                String trashId = TrashContract.Trash.getTrashId(uri);
                queryBuilder.appendWhere(BaseColumns._ID + "=" + trashId);
                break;

            default:
                throw new IllegalArgumentException("Unknown Uri - " + uri);
        }
        return queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch(match){
            case NOTES:
                return NotesContract.Notes.CONTENT_TYPE;
            case NOTES_ID:
                return NotesContract.Notes.CONTENT_ITEM_TYPE;
            case ARCHIVE:
                return ArchivesContract.Archives.CONTENT_TYPE;
            case ARCHIVE_ID:
                return ArchivesContract.Archives.CONTENT_ITEM_TYPE;
            case TRASH:
                return TrashContract.Trash.CONTENT_TYPE;
            case TRASH_ID:
                return TrashContract.Trash.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Uri - " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase database = mAppDatabase.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case NOTES:
                long noteRecordId = database.insertOrThrow(AppDatabase.Tables.Notes, null, values);
                return NotesContract.Notes.buildNoteUri(String.valueOf(noteRecordId));

            case ARCHIVE:
                long archiveRecordId = database.insertOrThrow(AppDatabase.Tables.Archives, null, values);
                return ArchivesContract.Archives.buildArchiveUri(String.valueOf(archiveRecordId));

            case TRASH:
                long trashRecordId = database.insertOrThrow(AppDatabase.Tables.Trash, null, values);
                return ArchivesContract.Archives.buildArchiveUri(String.valueOf(trashRecordId));

            default:
                throw new IllegalArgumentException("Unknown Uri - " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if(uri.equals(NotesContract.URI_BASE)){
            deleteDB();
            return 0;
        }
        final SQLiteDatabase database = mAppDatabase.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch(match){
            case NOTES_ID:
                String noteId = NotesContract.Notes.getNoteId(uri);
                String selectionCriteria = BaseColumns._ID + "=" + noteId +
                        (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return database.delete(AppDatabase.Tables.Notes, selectionCriteria, selectionArgs);
            case ARCHIVE_ID:
                String archiveId = ArchivesContract.Archives.getArchiveId(uri);
                String archiveSelectionCriteria = BaseColumns._ID + "=" + archiveId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return database.delete(AppDatabase.Tables.Archives, archiveSelectionCriteria, selectionArgs);

            case TRASH_ID:
                String trashId = TrashContract.Trash.getTrashId(uri);
                String trashSelectionCriteria = BaseColumns._ID + "=" + trashId
                        + (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return database.delete(AppDatabase.Tables.Trash, trashSelectionCriteria, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase database = mAppDatabase.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String selectionCriteria = selection;

        switch(match){
            case NOTES:
                return database.update(AppDatabase.Tables.Notes, values, selection, selectionArgs);
            case NOTES_ID:
                String noteId = NotesContract.Notes.getNoteId(uri);
                selectionCriteria = BaseColumns._ID + "=" + noteId +
                        (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return database.update(AppDatabase.Tables.Notes, values, selectionCriteria, selectionArgs);
            case ARCHIVE:
                return database.update(AppDatabase.Tables.Archives, values, selection, selectionArgs);
            case ARCHIVE_ID:
                String archiveId = ArchivesContract.Archives.getArchiveId(uri);
                selectionCriteria = BaseColumns._ID + "=" + archiveId +
                        (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return database.update(AppDatabase.Tables.Archives, values, selectionCriteria, selectionArgs);
            case TRASH:
                return database.update(AppDatabase.Tables.Trash, values, selection, selectionArgs);
            case TRASH_ID:
                String trashId = TrashContract.Trash.getTrashId(uri);
                selectionCriteria = BaseColumns._ID + "=" + trashId +
                        (!TextUtils.isEmpty(selection) ? " AND ( " + selection + ")" : "");
                return database.update(AppDatabase.Tables.Trash, values, selectionCriteria, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }
}
