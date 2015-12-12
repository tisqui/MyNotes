package com.squirrel.mynotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by squirrel on 12/11/15.
 */
public class NavigationDrawerAdapter extends BaseAdapter {

    private List<NavigationDrawerItem> mListOfItems;
    private LayoutInflater mLayoutInflater;

    public NavigationDrawerAdapter(List<NavigationDrawerItem> listOfItems, Context context) {
        super();
        mListOfItems = listOfItems;
        mLayoutInflater = LayoutInflater.from(context);

    }

    // returns how many items are in the list
    @Override
    public int getCount() {
        return mListOfItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mListOfItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * To get the navigation menu item - sets the title of the menu item and icon
     * @param position - the position of the item in the menu (list)
     * @param convertView - the natigation drawer item view
     * @param parent
     * @return navigation item view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = mLayoutInflater.inflate(R.layout.navigation_drawer_list, null);
        NavigationDrawerItem navigationDrawerItem = mListOfItems.get(position);

        TextView textView = (TextView) convertView.findViewById(R.id.navigation_drawer_item_text);
        textView.setText(navigationDrawerItem.getItemTitle());

        ImageView imageView = (ImageView) convertView.findViewById(R.id.navigation_drawer_item_icon);
        imageView.setImageResource(navigationDrawerItem.getIconId());

        return convertView;

    }
}
