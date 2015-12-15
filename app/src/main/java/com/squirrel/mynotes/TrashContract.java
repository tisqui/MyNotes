package com.squirrel.mynotes;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by squirrel on 12/14/15.
 */
public class TrashContract {
    interface DeletedColumns {
        String DELETED_ID = "_ID";
        String DELETED_TITLE = "deleted_title";
        String DELETED_DESCRIPTION = "deleted_description";
        String DELETED_DATE_TIME = "deleted_date_time";
    }

    public static final String CONTENT_AUTHORITY = "com.squirrel.mynotes.provider";
    public static final Uri URI_BASE = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_DELETED = "trash";
    public static final Uri URI_TABLE = URI_BASE.buildUpon().appendEncodedPath(PATH_DELETED).build();


    public static class Trash implements DeletedColumns, BaseColumns {

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + ".trash";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + ".trash";

        public static Uri buildDeletedUri(String deletedId) {
            return URI_TABLE.buildUpon().appendEncodedPath(deletedId).build();
        }

        public static String getDeletedId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
