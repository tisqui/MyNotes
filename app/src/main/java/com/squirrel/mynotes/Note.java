package com.squirrel.mynotes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by squirrel on 12/13/15.
 */
public class Note {
    private String mTitle;
    private String mDescription;
    private String mDate;
    private String mTime;
    private String mImagePath;
    private String mType;
    private int mId; //id of the note
    private boolean mHasNoImage = false;
    private int mStorageSelection; //storing it on GoodleDrive, local, Dropbox
    private Bitmap mBitmap; //for image notes

    public Note(String title, String description, String date, String time, int id, int storageSelection, String type) {
        mTitle = title;
        mDescription = description;
        mDate = date;
        mTime = time;
        mId = id;
        mStorageSelection = storageSelection;
        mType = type;
    }

    /**
     *     create the Note oblject from a string, which contains all the data, fields are separated by end of line
     *     Format:
     *     0 - mType
     *     1 - mId
     *     2 - mTitle
     *     3 - mDate
     *     4 - mTime
     *     5 - mImagePath
     *     6 - mStorageSelection
     *     7 - mType==NORMAL ? mDescription : list[0]
     */
    public Note(String note){
        String[] fields = note.split("\\$");
        mType = fields[0];
        mId = Integer.parseInt(fields[1]);
        mTitle = fields[2];
        mDate = fields[3];
        mTime = fields [4];
        mImagePath = fields[5];
        mStorageSelection= Integer.parseInt(fields[6]);
        if(mType == Constants.NORMAL){
            mDescription = fields[7];
            Note newNote =new  Note(mTitle, mDescription, mDate, mTime, mId, mStorageSelection, mType);
            newNote.setImagePath(mImagePath);
        } else {
            String list = "";
            for(int i = 7; i< fields.length; i++){
                list = list + fields[i];
            }
            mDescription = list;
        }
    }

    @Override
    public String toString() {
        return mTime + "$"
                + mId + "$"
                + mTitle + "$"
                + mDate + "$"
                + mTime + "$"
                + mImagePath + "$"
                + mStorageSelection + "$"
                + mDescription;
    }

    public void setBitmap(String path) {
        setImagePath(path);
        this.mBitmap = BitmapFactory.decodeFile(path);
    }

    public void setBitmap(Bitmap bitmap){
        this.mBitmap = bitmap;
    }

    public  Bitmap getBitmap(){
        return mBitmap;
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

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String imagePath) {
        mImagePath = imagePath;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public boolean isHasNoImage() {
        return mHasNoImage;
    }

    public void setHasNoImage(boolean hasNoImage) {
        mHasNoImage = hasNoImage;
    }

    public int getStorageSelection() {
        return mStorageSelection;
    }

    public void setStorageSelection(int storageSelection) {
        mStorageSelection = storageSelection;
    }
}
