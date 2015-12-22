package com.squirrel.mynotes;

import android.os.Bundle;

/**
 * Created by squirrel on 12/19/15.
 */
public class HelpFeedActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_feedback_layout);
        mToolbar = getToolbar();
        setUpNavigationDrawer();
    }
}
