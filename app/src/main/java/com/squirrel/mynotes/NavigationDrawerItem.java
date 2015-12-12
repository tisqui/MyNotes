package com.squirrel.mynotes;

/**
 * Created by squirrel on 12/11/15.
 * Content of the Navigation Drawer item
 */
public class NavigationDrawerItem {
    private int iconId;
    private String itemTitle;

    public NavigationDrawerItem(int iconId, String itemTitle) {
        this.iconId = iconId;
        this.itemTitle = itemTitle;
    }

    public int getIconId() {
        return iconId;
    }

    public String getItemTitle() {
        return itemTitle;
    }
}
