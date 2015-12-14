package com.squirrel.mynotes;

/**
 * Created by squirrel on 12/13/15.
 */
public class ArchivedNote {
    private int mId;
    private String mTitle;
    private String mDescription;
    private String mDateTime;
    private String mCategory;
    private String mType;

    public ArchivedNote(int id, String title, String description, String dateTime, String category, String type) {
        mId = id;
        mTitle = title;
        mDescription = description;
        mDateTime = dateTime;
        mCategory = category;
        mType = type;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
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

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }
}
