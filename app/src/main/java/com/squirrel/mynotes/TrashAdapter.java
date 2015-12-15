package com.squirrel.mynotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by squirrel on 12/14/15.
 */
public class TrashAdapter extends RecyclerView.Adapter<TrashAdapter.NoteHolder> {
    private static final String LOG_TAG = NotesAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private List<DeletedNote> mNotesList = Collections.emptyList();

    public TrashAdapter(List<DeletedNote> notesList, Context context) {
        mNotesList = notesList;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.trash_archive_adapter_layout, parent, false);
        return new NoteHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteHolder holder, int position) {
        holder.mId.setText(mNotesList.get(position).getId()+"");
        holder.mTitle.setText(mNotesList.get(position).getTitle());
        holder.mDescription.setText(mNotesList.get(position).getDescription());
        holder.mDateTime.setText(mNotesList.get(position).getDateTime());
    }

    @Override
    public int getItemCount() {
        return mNotesList.size();
    }

    public void setData(List<DeletedNote> notes) {
        mNotesList = notes;
    }

    public void delete(int position) {
        mNotesList.remove(position);
        notifyItemRemoved(position);
    }

    public class NoteHolder extends RecyclerView.ViewHolder{
        private TextView mId;
        private TextView mTitle;
        private TextView mDescription;
        private TextView mDateTime;
        private LinearLayout mLinearLayout;

        public NoteHolder(View itemView) {
            super(itemView);
            mId = (TextView) itemView.findViewById(R.id.id_note_custom_home);
            mTitle = (TextView) itemView.findViewById(R.id.title_note_custom_home);
            mDateTime = (TextView) itemView.findViewById(R.id.date_time_note_custom_home);
            mDescription = (TextView) itemView.findViewById(R.id.description_note_custom_home);
            mLinearLayout =(LinearLayout) itemView.findViewById(R.id.home_list);
        }
    }
}
