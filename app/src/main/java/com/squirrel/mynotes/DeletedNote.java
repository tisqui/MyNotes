package com.squirrel.mynotes;

/**
 * Created by squirrel on 12/13/15.
 */
public class DeletedNote {
    private int id;
    private String mTitle;
    private String mDescription;
    private String mDateTime;

    public DeletedNote(int id, String title, String description, String dateTime) {
        this.id = id;
        mTitle = title;
        mDescription = description;
        mDateTime = dateTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getDateTime() {
        return mDateTime;
    }

    public void setDateTime(String dateTime) {
        mDateTime = dateTime;
    }
}
