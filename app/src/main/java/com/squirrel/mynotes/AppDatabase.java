package com.squirrel.mynotes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by squirrel on 12/14/15.
 * Creating DB, Tables and update the Tables
 */
public class AppDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mynotes.db";
    private static final int DATABASE_VERSION = 1;

    interface Tables {
        String Notes = "notes";
        String Archives = "archives";
        String Deleted = "deleted";
    }

    public AppDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create Notes table
        db.execSQL("CREATE TABLE " + Tables.Notes + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NotesContract.NotesColumns.NOTE_TITLE + " TEXT NOT NULL,"
                + NotesContract.NotesColumns.NOTE_DESCRIPTION + " TEXT NOT NULL,"
                + NotesContract.NotesColumns.NOTE_TIME + " TEXT NOT NULL,"
                + NotesContract.NotesColumns.NOTE_TYPE + " TEXT NOT NULL,"
                + NotesContract.NotesColumns.NOTE_IMAGE_STORAGE_SELECTION + " INTEGER NOT NULL,"
                + NotesContract.NotesColumns.NOTE_IMAGE + " TEXT NOT NULL,"
                + NotesContract.NotesColumns.NOTE_DATE + " TEXT NOT NULL)");
        //create Archives table
        db.execSQL("CREATE TABLE " + Tables.Archives + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ArchivesContract.ArchivesColumns.ARCHIVES_TITLE + " TEXT NOT NULL,"
                + ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION + " TEXT NOT NULL,"
                + ArchivesContract.ArchivesColumns.ARCHIVES_CATEGORY + " TEXT NOT NULL,"
                + ArchivesContract.ArchivesColumns.ARCHIVES_TYPE + " TEXT NOT NULL,"
                + ArchivesContract.ArchivesColumns.ARCHIVES_DATE_TIME+ " TEXT NOT NULL)");
        //create Deleted table
        db.execSQL("CREATE TABLE " + Tables.Deleted + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TrashContract.DeletedColumns.DELETED_TITLE + " TEXT NOT NULL,"
                + TrashContract.DeletedColumns.DELETED_DESCRIPTION + " TEXT NOT NULL,"
                + TrashContract.DeletedColumns.DELETED_DATE_TIME+ " TEXT NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        int version = oldVersion;
        if(version == 1) {
            version = 2;
        }

        if(version != DATABASE_VERSION) {
            db.execSQL("DROP TABLE IF EXISTS " + Tables.Notes);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.Archives);
            db.execSQL("DROP TABLE IF EXISTS " + Tables.Deleted);
            onCreate(db);
        }

    }

    public static void deleteDatabase(Context context){
        context.deleteDatabase(DATABASE_NAME);
    }

    public void emptyTrash(){
        //delete the Deleted table
        getWritableDatabase().delete(Tables.Deleted, null,null);
    }
}
