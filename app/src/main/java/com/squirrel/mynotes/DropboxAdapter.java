package com.squirrel.mynotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by squirrel on 12/20/15.
 */
public class DropboxAdapter extends RecyclerView.Adapter<DropboxAdapter.RecyclerViewHolder> {

    private LayoutInflater mLayoutInflater;
    private List<String> mList = Collections.emptyList();

    public DropboxAdapter(Context context, List<String> list) {
        mLayoutInflater = LayoutInflater.from(context);
        mList = list;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.custom_dropbox_adapter_layout, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.title.setText(mList.get(position));

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setData(List<String> list){
        mList = list;
    }

    public void add(String dirName) {
        mList.add(dirName);
        Collections.sort(mList, String.CASE_INSENSITIVE_ORDER);
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder{
        TextView title;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.drop_box_directory_name);
        }
    }
}
