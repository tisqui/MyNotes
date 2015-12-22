package com.squirrel.mynotes;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

/**
 * Created by squirrel on 12/21/15.
 */
public class DropboxDirectoryCreatorAsync extends AsyncTask<Void, Long, Boolean> {

    private DropboxAPI<?> mDropboxAPI;
    private String mPath;
    private Context mContext;
    private OnDirectoryCreatedFinished mListener;
    private String mMessage;
    private String mName;

    public interface OnDirectoryCreatedFinished{
        void onDirectoryCreatedFinished(String directoryName);
    }

    public DropboxDirectoryCreatorAsync(Context context, DropboxAPI<?> dropboxAPI, String directoryName,
                                        String path,
                                        OnDirectoryCreatedFinished listener) {
        mDropboxAPI = dropboxAPI;
        mPath = path;
        mName = directoryName;
        mContext = context;
        mListener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try{
            mDropboxAPI.createFolder(mPath);
            mMessage = Constants.FOLDER_CREATED;

        }catch (DropboxException e){
            mMessage = Constants.FOLDER_CREATE_ERROR;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        if(aBoolean){
            mListener.onDirectoryCreatedFinished(mName);
            Toast.makeText(mContext.getApplicationContext(), mMessage, Toast.LENGTH_LONG).show();
        }
    }

}
