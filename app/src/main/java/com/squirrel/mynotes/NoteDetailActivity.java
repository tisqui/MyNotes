package com.squirrel.mynotes;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.CardView;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;

public class NoteDetailActivity extends BaseActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // Constants
    public static final int NORMAL = 1;
    public static final int LIST = 2;
    public static final int CAMERA_REQUEST = 1888;
    public static final int TAKE_GALLERY_CODE = 1;
    private static int sMonth, sYear, sHour, sDay, sMinute, sSecond;
    private static TextView sDateTextView, sTimeTextView;
    private static boolean sIsInAuth;
    private static String sTmpFlNm;
    private DropboxAPI<AndroidAuthSession> mApi;
    private File mDropBoxFile;
    private String mCameraFileName;
    private NoteCustomList mNoteCustomList;
    private EditText mTitleEditText, mDescriptionEditText;
    private ImageView mNoteImage;
    private String mImagePath = Constants.IMAGE_NOT_SET;
    private String mId;
    private boolean mGoingToCameraOrGallery = false, mIsEditing = false;
    private boolean mIsImageSet = false;
    private boolean mIsList = false;
    private Bundle mBundle;
    private ImageView mStorageSelection;
    private boolean mIsNotificationMode = false;
    private String mDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mBundle = savedInstanceState;
        setContentView(R.layout.activity_detail_note_layout);
        getToolbarWithDisplayUpAsHomeEnabled();
        if (getIntent().getStringExtra(Constants.LIST_TYPE_NOTES) != null) {
            initializeComponents(LIST);
        } else {
            initializeComponents(NORMAL);
        }

        setUpIfEditing();
        if (getIntent().getStringExtra(Constants.CAMERA) != null) {
            callCamera();
        }

    }

    private void setUpIfEditing() {
        if (getIntent().getStringExtra(Constants.ID) != null) {
            mId = getIntent().getStringExtra(Constants.ID);
            mIsEditing = true;
            if (getIntent().getStringExtra(Constants.LIST) != null) {
                initializeComponents(LIST);
            }
            setValues(mId);
            mStorageSelection.setEnabled(false);
        }
        if (getIntent().getStringExtra(Constants.NOTIFICATION) != null) {
            Note aNote = new Note(getIntent().getStringExtra(Constants.NOTIFICATION));
            mId = aNote.getId() + "";
            mIsNotificationMode = true;
            setValues(aNote);
            removeFromReminder(aNote);
            mStorageSelection.setEnabled(false);
        }
    }

    private void setValues(String id) {
        String[] projection = {BaseColumns._ID,
                NotesContract.NotesColumns.NOTE_TITLE,
                NotesContract.NotesColumns.NOTE_DESCRIPTION,
                NotesContract.NotesColumns.NOTE_DATE,
                NotesContract.NotesColumns.NOTE_IMAGE,
                NotesContract.NotesColumns.NOTE_IMAGE_STORAGE_SELECTION,
                NotesContract.NotesColumns.NOTE_TIME};
        // Query database - check parameters to return only partial records.
        Uri r = NotesContract.URI_TABLE;
        String selection = NotesContract.NotesColumns.NOTE_ID + " = " + id;
        Cursor cursor = getContentResolver().query(r, projection, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTE_TITLE));
                    String description = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTE_DESCRIPTION));
                    String time = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTE_TIME));
                    String date = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTE_DATE));
                    String image = cursor.getString(cursor.getColumnIndex(NotesContract.NotesColumns.NOTE_IMAGE));
                    int storageSelection = cursor.getInt(cursor.getColumnIndex(NotesContract.NotesColumns.NOTE_IMAGE_STORAGE_SELECTION));
                    mTitleEditText.setText(title);
                    if (mIsList) {
                        CardView cardView = (CardView) findViewById(R.id.card_view);
                        cardView.setVisibility(View.GONE);
                        setUpList(description);
                    } else {
                        mDescriptionEditText.setText(description);
                    }
                    sTimeTextView.setText(time);
                    sDateTextView.setText(date);
                    mImagePath = image;
                    if (!image.equals(Constants.IMAGE_NOT_SET)) {
                        mNoteImage.setImageBitmap(NotesActivity.mSendImage);
                    }
                    switch (storageSelection) {
                        case Constants.GOOGLE_DRIVE_SELECTION:
                            updateStorageSelection(null, R.drawable.ic_google_drive_grey600_24dp, Constants.GOOGLE_DRIVE_SELECTION);
                            break;
                        case Constants.DEVICE_SELECTION:
                        case Constants.NONE_SELECTION:
                            updateStorageSelection(null, R.drawable.ic_folder_grey600_24dp, Constants.DEVICE_SELECTION);
                            break;
                        case Constants.DROP_BOX_SELECTION:
                            updateStorageSelection(null, R.drawable.ic_dropbox_grey600_24dp, Constants.DROP_BOX_SELECTION);
                            break;
                    }
                } while (cursor.moveToNext());
            }
        }

    }

    private void setValues(Note note) {
        getSupportActionBar().setTitle(Constants.REMINDERS);
        String title = note.getTitle();
        String description = note.getDescription();
        String time = note.getTime();
        String date = note.getDate();
        String image = note.getImagePath();
        if (note.getType().equals(Constants.LIST)) {
            mIsList = true;
        }
        mTitleEditText.setText(title);
        if (mIsList) {
            initializeComponents(LIST);
            CardView cardView = (CardView) findViewById(R.id.card_view);
            cardView.setVisibility(View.GONE);
            setUpList(description);
        } else {
            mDescriptionEditText.setText(description);
        }
        sTimeTextView.setText(time);
        sDateTextView.setText(date);
        mImagePath = image;
        int storageSelection = note.getStorageSelection();
        switch (storageSelection) {
            case Constants.GOOGLE_DRIVE_SELECTION:
                updateStorageSelection(null, R.drawable.ic_google_drive_grey600_24dp, Constants.GOOGLE_DRIVE_SELECTION);
                break;
            case Constants.DEVICE_SELECTION:
            case Constants.NONE_SELECTION:
                if (!mImagePath.equals(Constants.IMAGE_NOT_SET)) {
                    updateStorageSelection(null, R.drawable.ic_folder_grey600_24dp, Constants.DEVICE_SELECTION);
                }
                break;
            case Constants.DROP_BOX_SELECTION:
                updateStorageSelection(BitmapFactory.decodeFile(mImagePath), R.drawable.ic_dropbox_grey600_24dp, Constants.DROP_BOX_SELECTION);
                break;

            default:
                break;
        }
    }

    private void updateStorageSelection(Bitmap bitmap, int storageSelectionResource, int selection) {
        if (bitmap != null) {
            mNoteImage.setImageBitmap(bitmap);
        }
        mStorageSelection.setBackgroundResource(storageSelectionResource);
        AppSharedPreferences.setPersonalNotesPreference(getApplicationContext(), selection);
    }

    private void setUpList(String description) {
        mDescription = description;
        if (!mIsNotificationMode) {
            mNoteCustomList.setUpForEditMode(description);
        } else {
            LinearLayout newItemLayout = (LinearLayout) findViewById(R.id.add_check_list_layout);
            newItemLayout.setVisibility(View.GONE);
            mNoteCustomList.setUpForListNotification(description);
        }

        LinearLayout layout = (LinearLayout) findViewById(R.id.add_check_list_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoteCustomList.addNewCheckbox();
            }
        });
    }

    private void initializeComponents(int choice) {
        if (choice == LIST) {
            CardView cardView = (CardView) findViewById(R.id.card_view);
            cardView.setVisibility(View.GONE);
            cardView = (CardView) findViewById(R.id.card_view_list);
            cardView.setVisibility(View.VISIBLE);
            mIsList = true;
        } else if (choice == NORMAL) {
            CardView cardView = (CardView) findViewById(R.id.card_view_list);
            cardView.setVisibility(View.GONE);
            mIsList = false;
        }

        mStorageSelection = (ImageView) findViewById(R.id.image_storage);
        if (AppSharedPreferences.getUploadPreference(getApplicationContext()) ==
                Constants.GOOGLE_DRIVE_SELECTION) {
            mStorageSelection.setBackgroundResource(R.drawable.ic_google_drive_grey600_24dp);
        } else if (AppSharedPreferences.getUploadPreference(getApplicationContext()) ==
                Constants.DROP_BOX_SELECTION) {
            mStorageSelection.setBackgroundResource(R.drawable.ic_dropbox_grey600_24dp);
        } else {
            mStorageSelection.setBackgroundResource(R.drawable.ic_folder_grey600_24dp);
        }

        mNoteCustomList = new NoteCustomList(this);
        mNoteCustomList.setUp();
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.check_list_layout);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(mNoteCustomList);
        mStorageSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(NoteDetailActivity.this, v);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.actions_image_selection, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.action_device) {
                            updateStorageSelection(null, R.drawable.ic_folder_grey600_24dp, Constants.DEVICE_SELECTION);
                        } else if (menuItem.getItemId() == R.id.action_google_drive) {
                            if (!AppSharedPreferences.isGoogleDriveAuthenticated(getApplicationContext())) {
                                startActivity(new Intent(NoteDetailActivity.this, GoogleDriveSelectionActivity.class));
                                finish();
                            } else {
                                updateStorageSelection(null, R.drawable.ic_google_drive_grey600_24dp, Constants.GOOGLE_DRIVE_SELECTION);
                            }
                        } else if (menuItem.getItemId() == R.id.action_dropbox) {
                            AppSharedPreferences.setPersonalNotesPreference(getApplicationContext(), Constants.DROP_BOX_SELECTION);
                            if (!AppSharedPreferences.isDropBoxAuthenticated(getApplicationContext())) {
                                startActivity(new Intent(NoteDetailActivity.this, DropboxPickerActivity.class));
                                finish();
                            } else {
                                updateStorageSelection(null, R.drawable.ic_dropbox_grey600_24dp, Constants.DROP_BOX_SELECTION);
                            }
                        }

                        if (mBundle != null) {
                            mCameraFileName = mBundle.getString("mCameraFileName");
                        }
                        AndroidAuthSession session = DropboxActions.buildSession(getApplicationContext());
                        mApi = new DropboxAPI<AndroidAuthSession>(session);

                        return false;
                    }
                });
            }
        });

        mTitleEditText = (EditText) findViewById(R.id.make_note_title);
        mNoteImage = (ImageView) findViewById(R.id.image_make_note);
        mDescriptionEditText = (EditText) findViewById(R.id.make_note_detail);
        sDateTextView = (TextView) findViewById(R.id.date_textview_make_note);
        sTimeTextView = (TextView) findViewById(R.id.time_textview_make_note);
        ImageView datePickerImageView = (ImageView) findViewById(R.id.date_picker_button);
        ImageView dateTimeDeleteImageView = (ImageView) findViewById(R.id.delete_make_note);
        dateTimeDeleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sDateTextView.setText("");
                sTimeTextView.setText(Constants.TIME_NOT_SET);
            }
        });

        datePickerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppDatePickerDialog datePickerDialog = new AppDatePickerDialog();
                datePickerDialog.show(getSupportFragmentManager(), Constants.DATE_PICKER);
            }
        });

        LinearLayout layout = (LinearLayout) findViewById(R.id.add_check_list_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoteCustomList.addNewCheckbox();
            }
        });
    }

    private Calendar getTargetTime() {
        Calendar calNow = Calendar.getInstance();
        Calendar calSet = (Calendar) calNow.clone();
        calSet.set(Calendar.MONTH, sMonth);
        calSet.set(Calendar.YEAR, sYear);
        calSet.set(Calendar.DAY_OF_MONTH, sDay);
        calSet.set(Calendar.HOUR_OF_DAY, sHour);
        calSet.set(Calendar.MINUTE, sMinute);
        calSet.set(Calendar.SECOND, sSecond);
        calSet.set(Calendar.MILLISECOND, 0);
        if (calSet.compareTo(calNow) <= 0) {
            calSet.add(Calendar.DATE, 1);
        }

        return calSet;
    }

    private void setAlarm(Calendar targetCal, Note note) {
        Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(Constants.NOTIFICATION, note.toString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                note.getId(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetCal.getTimeInMillis(), pendingIntent);
    }

    private void saveInDropBox() {
        AndroidAuthSession session = DropboxActions.buildSession(getApplicationContext());
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        session = mApi.getSession();
        if (session.authenticationSuccessful()) {
            try {
                session.finishAuthentication();
                DropboxActions.storeAuth(session, getApplicationContext());
            } catch (IllegalStateException e) {
                showToast(Constants.AUTH_ERROR_DROPBOX + e.getLocalizedMessage());
            }
        }

        DropBoxImageUploadAsync upload = new DropBoxImageUploadAsync(this, mApi,
                mDropBoxFile, Constants.NOTE_PREFIX + GDUtilities.time2Titl(null) + Constants.JPG);
        upload.execute();
        ContentValues values = createContentValues(Constants.NOTE_PREFIX + GDUtilities.time2Titl(null), Constants.DROP_BOX_SELECTION, true);
        createNoteAlarm(values, insertNote(values));
    }

    private void saveInGoogleDrive() {
        GDUtilities.init(this);
        if (checkPlayServices() && checkUserAccount()) {
            GDActions.init(this, GDUtilities.AM.getActiveEmil());
            GDActions.connect(true);
        }
        if (mBundle != null) {
            sTmpFlNm = mBundle.getString(Constants.TMP_FILE_NAME);
        }
        final String resourceId = Constants.NOTE_PREFIX + GDUtilities.time2Titl(null) + Constants.JPG;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    File tmpFl = new File(mImagePath);
                    GDActions.create(AppSharedPreferences.getGoogleDriveResourceId(getApplicationContext()),
                            resourceId, GDUtilities.MIME_JPEG, GDUtilities.file2Bytes(tmpFl));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // Add more error handling here
                }

            }
        }).start();
        ContentValues values = createContentValues(Constants.NOTE_PREFIX + GDUtilities.time2Titl(null) +
                Constants.JPG, Constants.GOOGLE_DRIVE_SELECTION, true);
        createNoteAlarm(values, insertNote(values));
    }

    private void saveInDevice() {
        ContentValues values = createContentValues(mImagePath, Constants.DEVICE_SELECTION, true);
        int id = insertNote(values);
        mId = id + "";
        createNoteAlarm(values, id);
        //TODO check if need to create alarm here
//        saveNote();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(Constants.TMP_FILE_NAME, sTmpFlNm);
        outState.putString("mCameraFileName", mCameraFileName);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(NoteDetailActivity.this, NotesActivity.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_camera:
                mGoingToCameraOrGallery = true;
                callCamera();
                break;

            case R.id.action_gallery:
                mGoingToCameraOrGallery = true;
                callGallery();
                break;

            case android.R.id.home:
                if (!mIsNotificationMode) {
                    saveNote();
                } else {
                    if (!sTimeTextView.getText().toString().equals(Constants.TIME_NOT_SET)) {
                        performAsReminder();
                    } else {
                        performAsNotes();
                    }
                    moveToArchive(mIsList);
                    mActionType = ARCHIVE;
                    mToolbarTitle = Constants.ARCHIVES;
                    startActivity(new Intent(NoteDetailActivity.this, ArchivesActivity.class));
                    finish();
                }
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void removeFromReminder(Note reminder) {
        ContentResolver cr = getContentResolver();
        Uri uri = Uri.parse(NotesContract.URI_BASE + "/notes/" + reminder.getId());
        cr.delete(uri, null, null);
    }

    private void moveToArchive(boolean isList) {
        String type;
        ContentValues values = new ContentValues();
        TextView title = (TextView) findViewById(R.id.make_note_title);
        TextView description = (TextView) findViewById(R.id.make_note_detail);
        TextView dateTime = (TextView) findViewById(R.id.time_textview_make_note);
        values.put(ArchivesContract.ArchivesColumns.ARCHIVES_TITLE, title.getText().toString());
        values.put(ArchivesContract.ArchivesColumns.ARCHIVES_DATE_TIME, dateTime.getText().toString());
        if (isList) {
            type = Constants.LIST;
            values.put(ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION, mDescription);
        } else {
            type = Constants.NORMAL;
            values.put(ArchivesContract.ArchivesColumns.ARCHIVES_DESCRIPTION, description.getText().toString());
        }

        values.put(ArchivesContract.ArchivesColumns.ARCHIVES_TYPE, type);
        values.put(ArchivesContract.ArchivesColumns.ARCHIVES_CATEGORY, Constants.REMINDERS);

        ContentResolver cr = getContentResolver();
        Uri uri = Uri.parse(ArchivesContract.URI_BASE + "/archives");
        cr.insert(uri, values);
    }

    private void callGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, TAKE_GALLERY_CODE);
    }

    protected void saveNote() {
        if (mIsEditing) {
            switch (AppSharedPreferences.getUploadPreference(getApplicationContext())) {
                case Constants.DROP_BOX_SELECTION:
                    if (!mImagePath.equals(Constants.IMAGE_NOT_SET)) {
                        editForSaveInDropBox();
                    } else {
                        editForSaveInDevice();
                    }
                    break;

                case Constants.GOOGLE_DRIVE_SELECTION:
                    if (!mImagePath.equals(Constants.IMAGE_NOT_SET) && mIsImageSet) {
                        editForSaveInGoogleDrive();
                    } else {
                        editForSaveInDevice();
                    }
                    break;

                case Constants.DEVICE_SELECTION:
                case Constants.NONE_SELECTION:
                    editForSaveInDevice();
                    break;
            }
        } else if (mTitleEditText.getText().toString().length() > 0 && !mGoingToCameraOrGallery) {
            switch (AppSharedPreferences.getUploadPreference(getApplicationContext())) {
                case Constants.DROP_BOX_SELECTION:
                    if (!mImagePath.equals(Constants.IMAGE_NOT_SET)) {
                        saveInDropBox();
                    } else {
                        saveInDevice();
                    }
                    break;

                case Constants.GOOGLE_DRIVE_SELECTION:
                    if (!mImagePath.equals(Constants.IMAGE_NOT_SET)) {
                        saveInGoogleDrive();
                    } else {
                        saveInDevice();
                    }
                    break;

                case Constants.DEVICE_SELECTION:
                case Constants.NONE_SELECTION:
                    saveInDevice();
                    break;
            }
        }
        startActivity(new Intent(NoteDetailActivity.this, NotesActivity.class));
        finish();
    }

    private void editForSaveInDropBox() {
        if(AppSharedPreferences.isDropBoxAuthenticated(getApplicationContext())) {
            AndroidAuthSession session = DropboxActions.buildSession(getApplicationContext());
            mApi = new DropboxAPI<AndroidAuthSession>(session);
            session = mApi.getSession();
            if(session.authenticationSuccessful()) {
                try {
                    session.finishAuthentication();
                    DropboxActions.storeAuth(session, getApplicationContext());
                } catch(IllegalStateException e) {
                    showToast(Constants.AUTH_ERROR_DROPBOX + e.getLocalizedMessage());
                }
            }
        }
        ContentValues values = createContentValues("", Constants.DROP_BOX_SELECTION, false);
        if(mIsImageSet) {
            String filename = Constants.NOTE_PREFIX + GDUtilities.time2Titl(null) + Constants.JPG;
            values.put(NotesContract.NotesColumns.NOTE_IMAGE, filename);
            mDropBoxFile = new File(getApplicationContext().getCacheDir(), filename);
            try {
                mDropBoxFile.createNewFile();
            } catch(IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            Bitmap newImage = ((BitmapDrawable) mNoteImage.getDrawable()).getBitmap();
            newImage.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapData = bos.toByteArray();
            try {
                FileOutputStream fos = new FileOutputStream(mDropBoxFile);
                fos.write(bitmapData);
                fos.flush();
                fos.close();
            } catch(IOException e) {
                e.printStackTrace();
            }

            DropBoxImageUploadAsync uploadAsync = new DropBoxImageUploadAsync(this, mApi, mDropBoxFile, filename);
            uploadAsync.execute();
        }

        updateNote(values);
        createNoteAlarm(values, (int) System.currentTimeMillis());
    }

    private void editForSaveInGoogleDrive() {
        GDUtilities.init(this);
        final String resourceId = Constants.NOTE_PREFIX + GDUtilities.time2Titl(null) + Constants.JPG;
        if(checkPlayServices() && checkUserAccount()) {
            GDActions.init(this, GDUtilities.AM.getActiveEmil());
            GDActions.connect(true);
        }
        if(mBundle != null) {
            sTmpFlNm = mBundle.getString(Constants.TMP_FILE_NAME);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    if(mIsImageSet) {
                        File tmpFL = null;
                        try {
                            tmpFL = new File(mImagePath);
                            GDActions.create(AppSharedPreferences.getGoogleDriveResourceId(getApplicationContext()),
                                    resourceId, GDUtilities.MIME_JPEG, GDUtilities.file2Bytes(tmpFL));
                        } finally {
//                            if(tmpFL != null) {
//                                tmpFL.delete();
//                            }
                        }
                    }
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        ContentValues values = createContentValues(resourceId, Constants.GOOGLE_DRIVE_SELECTION,  false);
        updateNote(values);
        createNoteAlarm(values, (int) System.currentTimeMillis());
    }

    private void editForSaveInDevice() {
        ContentValues values = createContentValues(mImagePath, Constants.DEVICE_SELECTION, false);
        updateNote(values);
        createNoteAlarm(values, Integer.parseInt(mId));
    }

    private void callCamera() {
        Intent cameraIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap photo = null;
        switch(requestCode) {
            case Constants.REQ_ACCPICK:
                if(resultCode == Activity.RESULT_OK && data != null) {
                    String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if(GDUtilities.AM.setEmil(email) == GDUtilities.AM.CHANGED) {
                        GDActions.init(this, GDUtilities.AM.getActiveEmil());
                        GDActions.connect(true);
                    }
                } else if(GDUtilities.AM.getActiveEmil() == null) {
                    GDUtilities.AM.removeActiveAccnt();
                    finish();
                }
                break;
            case Constants.REQ_AUTH:
            case Constants.REQ_RECOVER:
                sIsInAuth = false;
                if(resultCode == Activity.RESULT_OK) {
                    GDActions.connect(true);
                } else if(resultCode == RESULT_CANCELED) {
                    GDUtilities.AM.removeActiveAccnt();
                    finish();
                }
                break;

            case Constants.REQ_SCAN: {
                if(resultCode == Activity.RESULT_OK) {
                    final String titl = GDUtilities.time2Titl(null);
                    if (titl != null && sTmpFlNm != null) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                File tmpFl = null;
                                GDActions.createTreeGDAA(GDUtilities.MYROOT, titl, GDUtilities.file2Bytes(tmpFl));
                            }
                        }).start();
                    }
                }
                break;
            }

        }

        if(requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            mGoingToCameraOrGallery = false;
            photo = (Bitmap) data.getExtras().get("data");
            mNoteImage.setImageBitmap(photo);
            Uri tempUri = getImageUri(getApplicationContext(), photo);
            File finalFile = new File(getRealPathFromURI(tempUri));
            mImagePath = finalFile.toString();
            mIsImageSet = true;
        } else if (requestCode == TAKE_GALLERY_CODE) {
            if(resultCode == RESULT_OK) {
                mGoingToCameraOrGallery = false;
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                mImagePath = cursor.getString(columnIndex);
                cursor.close();
                File tempFile = new File(mImagePath);
                photo = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                mNoteImage.setVisibility(View.VISIBLE);
                mNoteImage.setImageBitmap(photo);
                mIsImageSet = true;
            } else {
                mIsImageSet = false;
            }
        }
        if(mIsImageSet) {
            mDropBoxFile = new File(mImagePath);
        }
    }

    private Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(),
                inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(!sIsInAuth) {
            if(connectionResult.hasResolution()) {
                try {
                    sIsInAuth = true;
                    connectionResult.startResolutionForResult(this, Constants.REQ_AUTH);
                } catch(IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    // Add other error handling here
                    finish();
                }
            } else {
                finish();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    private boolean checkUserAccount() {
        String email = GDUtilities.AM.getActiveEmil();
        Account account = GDUtilities.AM.getPrimaryAccnt(true);
        if(email == null) {
            if(account == null) {
                account = showAccountPicker();
                return false;
            } else {
                // Only one a/c registered
                GDUtilities.AM.setEmil(account.name);
            }
            return true;
        }

        account = GDUtilities.AM.getActiveAccnt();
        if(account == null) {
            account = showAccountPicker();
            return false;
        }
        return true;
    }

    private boolean checkPlayServices() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(status != ConnectionResult.SUCCESS) {
            if(GooglePlayServicesUtil.isUserRecoverableError(status)) {
                errorDialog(status, Constants.REQ_RECOVER);
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    private void errorDialog(int errorCode, int requestCode) {
        Bundle args = new Bundle();
        args.putInt(Constants.DIALOG_ERROR, errorCode);
        args.putInt(Constants.REQUEST_CODE, requestCode);
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), Constants.DIALOG_ERROR);
    }

    private ContentValues createContentValues(String noteImage, int storageSelection, boolean isSave) {
        if(noteImage == null || noteImage.equals("")) {
            noteImage = Constants.IMAGE_NOT_SET;
        }
        ContentValues values = new ContentValues();
        values.put(NotesContract.NotesColumns.NOTE_TITLE, mTitleEditText.getText().toString());
        values.put(NotesContract.NotesColumns.NOTE_DATE, sDateTextView.getText().toString());
        values.put(NotesContract.NotesColumns.NOTE_TIME, sTimeTextView.getText().toString());
        if(mIsImageSet || isSave) {
            values.put(NotesContract.NotesColumns.NOTE_IMAGE, noteImage);
        }
        values.put(NotesContract.NotesColumns.NOTE_IMAGE_STORAGE_SELECTION, storageSelection);
        String type = Constants.NORMAL;
        String description = mDescriptionEditText.getText().toString();
        if(mIsList) {
            description = mNoteCustomList.getLists();
            type = Constants.LIST;
        }

        values.put(NotesContract.NotesColumns.NOTE_TYPE, type);
        values.put(NotesContract.NotesColumns.NOTE_DESCRIPTION, description);

        return values;
    }

    private int insertNote(ContentValues values) {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse(NotesContract.URI_BASE + "/notes");
        Uri returned = contentResolver.insert(uri, values);
        String[] temp = returned.toString().split("/");
        return Integer.parseInt(temp[temp.length-1]);
    }

    private void updateNote(ContentValues values) {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = Uri.parse(NotesContract.URI_BASE + "/notes");
        String selection = NotesContract.NotesColumns.NOTE_ID + " = " + mId;
        contentResolver.update(uri, values, selection, null);
    }

    private void createNoteAlarm(ContentValues values, int id) {
        if(!sTimeTextView.getText().toString().equals(Constants.TIME_NOT_SET)) {
            Note note = new Note(values.getAsString(NotesContract.NotesColumns.NOTE_TITLE),
                    values.getAsString(NotesContract.NotesColumns.NOTE_DESCRIPTION),
                    values.getAsString(NotesContract.NotesColumns.NOTE_DATE),
                    values.getAsString(NotesContract.NotesColumns.NOTE_TIME),
                    id,
                    values.getAsInteger(NotesContract.NotesColumns.NOTE_IMAGE_STORAGE_SELECTION),
                    values.getAsString(NotesContract.NotesColumns.NOTE_TYPE));
            note.setImagePath(values.getAsString(NotesContract.NotesColumns.NOTE_IMAGE));
            setAlarm(getTargetTime(), note);

        }
    }

    private Account showAccountPicker() {
        Account account = GDUtilities.AM.getPrimaryAccnt(false);
        Intent intent = AccountPicker.newChooseAccountIntent(account, null,
                new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE},true,null, null, null, null);
        startActivityForResult(intent, Constants.REQ_ACCPICK);
        return account;
    }

    public static class AppDatePickerDialog extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private int mYear, mMonth, mDay;
        private String tempMonth;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            tempMonth = c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.UK);
            return new DatePickerDialog(getActivity(), this, mYear, mMonth, mDay);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            if(year == mYear) {
                if(monthOfYear == mMonth) {
                    if(dayOfMonth == mDay) {
                        sDateTextView.setText(Constants.TODAY);
                    } else {
                        sDateTextView.setText(dayOfMonth + " " + sMonth);
                    }
                } else {
                    sDateTextView.setText(dayOfMonth + " " + sMonth);
                }
            } else {
                sDateTextView.setText(dayOfMonth + " " + sMonth + " " + year);
            }
            sYear = year;
            sMonth = monthOfYear;
            sDay = dayOfMonth;
            AppTimePickerDialog timePickerDialog = new AppTimePickerDialog();
            timePickerDialog.show(getFragmentManager(), Constants.DATE_PICKER);
        }
    }


    public static class AppTimePickerDialog extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {
        private int mHour, mMinute;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), this, mHour, mMinute,DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if(minute <10) {
                sTimeTextView.setText(hourOfDay + ":0" + minute);
            } else {
                sTimeTextView.setText(hourOfDay + ":" + minute);
            }
            sHour = hourOfDay;
            sMinute = minute;
            sSecond = 0;
        }
    }
}
