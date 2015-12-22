package com.squirrel.mynotes;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by squirrel on 12/11/15.
 */
public abstract class BaseActivity extends AppCompatActivity {

    //id of the action
    public final static int NOTES = 1;
    public final static int REMINDERS = 2;
    public final static int ARCHIVE = 3;
    public final static int TRASH = 4;
    public final static int SETTINGS = 5;

    private Class mNextActivity;

    public static String mToolbarTitle = Constants.NOTES;
    public static int mActionType = NOTES;

    protected Toolbar mToolbar;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    public static ImageView createNote; //icon for creating a note
    public static ImageView createListNote;
    public static ImageView createPhotoNote;


    protected void setUpActionButtons(){
        createNote = (ImageView) findViewById(R.id.new_note);
        createListNote = (ImageView) findViewById(R.id.new_list_note);
        createPhotoNote = (ImageView) findViewById(R.id.new_image_note);

        createNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NoteDetailActivity.class);
                intent.putExtra(Constants.NOTE_OR_REMINDER, mToolbarTitle);
                startActivity(intent);
            }
        });

        createListNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NoteDetailActivity.class);
                intent.putExtra(Constants.LIST_TYPE_NOTES, Constants.TRUE);
                intent.putExtra(Constants.NOTE_OR_REMINDER, mToolbarTitle);
                startActivity(intent);
            }
        });

        createPhotoNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NoteDetailActivity.class);
                intent.putExtra(Constants.CAMERA, Constants.TRUE);
                intent.putExtra(Constants.NOTE_OR_REMINDER, mToolbarTitle);
                startActivity(intent);
            }
        });
    }


    public static void performAsReminder(){
        mToolbarTitle = Constants.REMINDERS;
        mActionType = REMINDERS;
    }

    public static void performAsNotes(){
        mToolbarTitle = Constants.NOTES;
        mActionType = NOTES;
    }


    protected Toolbar getToolbar(){
        if(mToolbar == null){
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            if(mToolbar != null){
                switch (mActionType){
                    case REMINDERS:
                        getSupportActionBar().setTitle(Constants.REMINDERS);
                        break;
                    case ARCHIVE:
                        getSupportActionBar().setTitle(Constants.ARCHIVES);
                        break;
                    case TRASH:
                        getSupportActionBar().setTitle(Constants.TRASH);
                        break;
                    case SETTINGS:
                        getSupportActionBar().setTitle(Constants.SETTINGS);
                        break;
                }
            }
        }
        return mToolbar;
    }

    protected Toolbar getToolbarWithDisplayUpAsHomeEnabled(){
        getToolbar();
        if(mToolbar != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if(mActionType == REMINDERS){
                getSupportActionBar().setTitle(Constants.MAKE_REMINDER);
            } else if(mActionType == SETTINGS){
                getSupportActionBar().setTitle(Constants.SETTINGS);
            } else if(mActionType == NOTES){
                getSupportActionBar().setTitle(Constants.MAKE_NOTES);
            }
        }
        return mToolbar;
    }

    //for the screens which have to actions, remove the controls

    protected void removeActions() {
        CardView cardView;
        cardView = (CardView) findViewById(R.id.card_view);
        cardView.setVisibility(View.GONE);
    }

    protected void setUpNavigationDrawer(){
        ListView navigationListView;
        List<NavigationDrawerItem> menuItemsList = new ArrayList<>();

        menuItemsList.add(new NavigationDrawerItem(android.R.drawable.ic_menu_agenda, Constants.DRAWER_NOTES));
        menuItemsList.add(new NavigationDrawerItem(android.R.drawable.ic_popup_reminder, Constants.DRAWER_REMINDERS));
        menuItemsList.add(new NavigationDrawerItem(android.R.drawable.ic_menu_gallery, Constants.DRAWER_ARCHIVES));
        menuItemsList.add(new NavigationDrawerItem(android.R.drawable.ic_menu_delete, Constants.DRAWER_TRASH));
        menuItemsList.add(new NavigationDrawerItem(android.R.drawable.ic_menu_preferences, Constants.DRAWER_SETTINGS));
        menuItemsList.add(new NavigationDrawerItem(android.R.drawable.ic_menu_help, Constants.DRAWER_HELP_AND_FEEDBACK));

        //initialize the drawer fragment
        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        DrawerLayout dl = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);

        NavigationDrawerAdapter navigationDrawerAdapter = new NavigationDrawerAdapter( menuItemsList, getApplicationContext());
        //initialize the list view for navigation drawer
        navigationListView = (ListView) findViewById(R.id.navigation_list);
        navigationListView.setAdapter(navigationDrawerAdapter);

        navigationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position){
                    case 0:
                        mNextActivity = NotesActivity.class;
                        performAsNotes();
                        break;
                    case 1:
                        mNextActivity = NotesActivity.class;
                        performAsReminder();
                        break;
                    case 2:
                        //archives
                        mNextActivity = ArchivesActivity.class;
                        mActionType = ARCHIVE;
                        break;
                    case 3:
                        //trash
                        mNextActivity = TrashActivity.class;
                        mActionType = TRASH;
                        break;
                    case 4:
                        //settings
                        mNextActivity = AppAuthentificationActivity.class;
                        mActionType = SETTINGS;
                        break;
                    case 5:
                        //help
                        mNextActivity = HelpFeedActivity.class;
                        break;
                    default:
                        mNextActivity = HelpFeedActivity.class;
                }
                AppSharedPreferences.setUserLearned(getApplicationContext(), Constants.KEY_USER_LEARNED_DRAWER, Constants.TRUE);

                Intent intent = new Intent(BaseActivity.this, mNextActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(intent, 0);
                overridePendingTransition(0, 0);
                mNavigationDrawerFragment.closeDrawer();
            }
        });
    }

    protected void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

}
