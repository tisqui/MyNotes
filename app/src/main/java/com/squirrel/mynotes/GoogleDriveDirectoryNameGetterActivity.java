package com.squirrel.mynotes;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.common.api.ResultCallback;

/**
 * Created by squirrel on 12/21/15.
 */
public class GoogleDriveDirectoryNameGetterActivity extends BaseGoogleDriveActivity {
    private final ResultCallback<DriveResource.MetadataResult> mMetadataCallback = new
            ResultCallback<DriveResource.MetadataResult>() {
                @Override
                public void onResult(DriveResource.MetadataResult metadataResult) {
                    if(!metadataResult.getStatus().isSuccess()) {
                        showMessage("Problem trying to fetch metadata");
                        return;
                    }
                    Metadata metadata = metadataResult.getMetadata();
                    AppSharedPreferences.storeGoogleDriveUploadFileName(getApplicationContext(), metadata.getTitle());
                    startActivity(new Intent(GoogleDriveDirectoryNameGetterActivity.this, NotesActivity.class));
                    finish();
                }
            };

    private final ResultCallback<DriveApi.DriveIdResult> mIdCallback = new
            ResultCallback<DriveApi.DriveIdResult>() {
                @Override
                public void onResult(DriveApi.DriveIdResult driveIdResult) {
                    if(!driveIdResult.getStatus().isSuccess()) {
                        showMessage("Cannot find driveId. Are you authorized to view this file");
                        return;
                    }
                    DriveFile file = Drive.DriveApi.getFile(getmGoogleApiClient(), driveIdResult.getDriveId());
                    file.getDriveId().encodeToString();
                    file.getMetadata(getmGoogleApiClient()).setResultCallback(mMetadataCallback);
                }
            };

    @Override
    public void onConnected(Bundle bundle) {
        try {
            Drive.DriveApi.fetchDriveId(getmGoogleApiClient(),
                    AppSharedPreferences.getGoogleDriveResourceId(getApplicationContext())).setResultCallback(mIdCallback);
            AppSharedPreferences.setPersonalNotesPreference(getApplicationContext(), Constants.GOOGLE_DRIVE_SELECTION);
            AppSharedPreferences.isGoogleDriveAuthenticated(getApplicationContext(), true);
            showMessage("Image location set in Google Drive");
        } catch(IllegalStateException e) {
            // this can happen when a newly created directory is selected
            showMessage("An error occured while selected the folder.  Sync issue? Please try again");
            startActivity(new Intent(GoogleDriveDirectoryNameGetterActivity.this, GoogleDriveSelectionActivity.class));
            finish();
        } catch(IllegalArgumentException e){
            showMessage("An error occured while selected the folder.  Sync issue? Please try again");
            startActivity(new Intent(GoogleDriveDirectoryNameGetterActivity.this, GoogleDriveSelectionActivity.class));
            finish();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
