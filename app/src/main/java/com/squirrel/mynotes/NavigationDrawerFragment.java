package com.squirrel.mynotes;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by squirrel on 12/11/15.
 */
public class NavigationDrawerFragment extends Fragment {

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private boolean mUsedDrawer;
    private boolean mFromSavedInstanceState;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUsedDrawer = Boolean.valueOf(AppSharedPreferences.hasUserLearned(getActivity(), Constants.KEY_USER_LEARNED_DRAWER, Constants.FALSE));
        if (savedInstanceState != null) {
            mFromSavedInstanceState = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.navigation_drawer_list, container, false);

    }

    public void setUpDrawer(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {
        View container = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (slideOffset < 0.6) {
                    toolbar.setAlpha(1 - slideOffset / 2);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {

                if (!mUsedDrawer) {
                    //the drawer was never opened previously
                    mUsedDrawer = true;
                    AppSharedPreferences.setUserLearned(getActivity(), Constants.KEY_USER_LEARNED_DRAWER, Constants.TRUE);
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                AppSharedPreferences.setUserLearned(getActivity(), Constants.KEY_USER_LEARNED_DRAWER, Constants.TRUE);
            }
        };

        if (!mUsedDrawer && !mFromSavedInstanceState) {
            //if the drawer was never opened or not coming from switch orientation
            mDrawerLayout.openDrawer(container);
        }

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawers();
    }
}
