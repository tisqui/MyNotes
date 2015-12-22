package com.squirrel.mynotes;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by squirrel on 12/19/15.
 */
public class TrashActivity  extends BaseActivity implements
        LoaderManager.LoaderCallbacks<List<DeletedNote>> {
    private static final int LOADER_ID = 1;
    private RecyclerView mRecyclerView;
    private TrashAdapter mTrashAdapter;
    private ContentResolver mContentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        mToolbar = getToolbar();
        setUpNavigationDrawer();
        setUpRecyclerView();
        removeActions();
    }

    private void setUpRecyclerView() {
        mContentResolver = getContentResolver();
        getSupportLoaderManager().initLoader(LOADER_ID, null, TrashActivity.this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_home);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mTrashAdapter = new TrashAdapter(TrashActivity.this, new ArrayList<DeletedNote>());
        mRecyclerView.setAdapter(mTrashAdapter);
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(final View view, final int position) {
                PopupMenu popup = new PopupMenu(TrashActivity.this, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.actions_archive_delete, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.action_delete_archive) {
                            delete(view, position);
                        }
                        return false;
                    }
                });
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        }));
    }

    private void delete(View view, int position) {
        String _ID = ((TextView) view.findViewById(R.id.id_note_custom_home)).getText().toString();
        Uri uri = TrashContract.Trash.buildTrashUri(_ID);
        mContentResolver.delete(uri, null, null);
        mTrashAdapter.delete(position);
        changeNoItemTag();
    }

    private void changeNoItemTag() {
        if (mTrashAdapter.getItemCount() != 0) {
            TextView noItemTV = (TextView) findViewById(R.id.no_item_message_view);
            noItemTV.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            TextView noItemTV = (TextView) findViewById(R.id.no_item_message_view);
            noItemTV.setText(Constants.ARCHIVE_EMPTY);
            noItemTV.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<List<DeletedNote>> onCreateLoader(int id, Bundle args) {
        mContentResolver = getContentResolver();
        return new TrashLoader(TrashActivity.this, mContentResolver);
    }

    @Override
    public void onLoadFinished(Loader<List<DeletedNote>> loader, List<DeletedNote> data) {
        mTrashAdapter.setData(data);
        changeNoItemTag();
    }

    @Override
    public void onLoaderReset(Loader<List<DeletedNote>> loader) {
        mTrashAdapter.setData(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trash_empty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_empty_trash) {
            new AppDatabase(getApplicationContext()).emptyTrash();
            mTrashAdapter.setData(new ArrayList<DeletedNote>());
            mTrashAdapter.notifyDataSetChanged();
            showToast(Constants.TRASH_EMPTY);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    private void showToast(String msg) {
//        Toast error = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
//        error.show();
//    }
}
