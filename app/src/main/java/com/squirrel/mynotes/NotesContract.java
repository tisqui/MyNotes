package com.squirrel.mynotes;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by squirrel on 12/14/15.
 */
public class NotesContract {
    interface NotesColumns {
        String NOTE_ID = "_ID";
        String NOTE_TITLE = "note_title";
        String NOTE_DESCRIPTION = "note_description";
        String NOTE_DATE = "note_date";
        String NOTE_TIME = "note_time";
        String NOTE_IMAGE = "note_image";
        String NOTE_TYPE = "note_type";
        String NOTE_IMAGE_STORAGE_SELECTION = "note_image_storage_selection";
    }
    public static final String CONTENT_AUTHORITY = "com.squirrel.mynotes.provider";

    public static final Uri URI_BASE = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_NOTES = "notes";
    public static final Uri URI_TABLE = URI_BASE.buildUpon().appendEncodedPath(PATH_NOTES).build();

    public static class Notes implements NotesColumns, BaseColumns {
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + ".notes";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + ".notes";

        public static Uri buildNoteUri(String noteId){
            return URI_BASE.buildUpon().appendEncodedPath(noteId).build();
        }

        public static String getNoteId(Uri uri){
            return uri.getPathSegments().get(1);
        }
    }

}
