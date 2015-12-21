package com.squirrel.mynotes;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by squirrel on 12/20/15.
 */
public class DropboxDirectoryListenerAsync extends AsyncTask<Void, Long, Boolean> {

    private Context mContext;
    private DropboxAPI<?> mDropboxAPI;
    private List<String> mDirectories = new ArrayList<>();
    private String mErrorMessage;
    private String mCurrentDirectory;
    private OnLoadFinished mListener;

    public interface OnLoadFinished{
        void onLoadFinished(List<String> values);
    }


    @Override
    protected Boolean doInBackground(Void... params) {
        try{
            mErrorMessage = null;
            DropboxAPI.Entry directoryEntry = mDropboxAPI.metadata(mCurrentDirectory, 1000, null, true, null);
            //check if there is nothing in the directory or there is no such a directory
            if(!directoryEntry.isDir || directoryEntry.contents == null) {
                mErrorMessage = "This is not a directory or directory is empty.";
                return false;
            }
            //found at least one directory
            for (DropboxAPI.Entry entry : directoryEntry.contents) {
                if(entry.isDir) {
                    mDirectories.add(entry.fileName());
                }
            }
        } catch(DropboxUnlinkedException e) {
            mErrorMessage = "Dropbox authentification error!";
        } catch(DropboxPartialFileException e) {
            mErrorMessage = "Download was canceled";
        } catch(DropboxServerException e) {
            mErrorMessage = "Network error! Try again.";
        } catch(DropboxParseException e) {
            mErrorMessage = "Dropbox parse exception :( Try again.";
        } catch(DropboxException e) {
            mErrorMessage = "Unknown Dropbox error :( Try again later.";
        }
        if(mErrorMessage != null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if(aBoolean){
            mListener.onLoadFinished(mDirectories);
        } else {
            showToast(mErrorMessage);
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }

}
