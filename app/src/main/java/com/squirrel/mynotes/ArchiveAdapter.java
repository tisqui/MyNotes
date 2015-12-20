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
public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.NoteHolder> {

    private static final String LOG_TAG = NotesAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private List<ArchivedNote> mNotesList = Collections.emptyList();
    private Context mContext;

    public ArchiveAdapter(Context context, List<ArchivedNote> mData) {
        mInflater = LayoutInflater.from(context);
        this.mNotesList = mData;
        this.mContext = context;
    }


    @Override
    public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.trash_archive_adapter_layout, parent, false);
        return new NoteHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteHolder holder, int position) {
        holder.mId.setText(mNotesList.get(position).getId() + "");
        holder.mTitle.setText(mNotesList.get(position).getTitle());
        //checking if the notes are Standart or the List type
        if(mNotesList.get(position) .getDateTime().contains(Constants.TIME_NOT_SET)){
            //the type of the note is "List", need to show the list of items
            NoteCustomList noteCustomList = new NoteCustomList(mContext);
            noteCustomList.setUpForHomeAdapter(mNotesList.get(position).getDescription());
            holder.mLinearLayout.removeAllViews();
            holder.mLinearLayout.addView(noteCustomList);
            holder.mDescription.setVisibility(View.GONE);
        } else if(mNotesList.get(position).getType().equals(Constants.LIST)) {
            NoteCustomList noteCustomList = new NoteCustomList(mContext);
            noteCustomList.setUpForListNotification(mNotesList.get(position).getDescription());
            holder.mLinearLayout.removeAllViews();
            holder.mLinearLayout.addView(noteCustomList);
            holder.mLinearLayout.setVisibility(View.VISIBLE);
            holder.mDescription.setVisibility(View.GONE);

        }else {
            holder.mDescription.setVisibility(View.GONE);
            holder.mDescription.setText(mNotesList.get(position).getDescription());
        }
        holder.mDateTime.setText(mNotesList.get(position).getDateTime()
                + " from " + mNotesList.get(position).getCategory());
    }

    @Override
    public int getItemCount() {
        return mNotesList.size();
    }

    public void setData(List<ArchivedNote> notes) {
        mNotesList = notes;
    }

    public void delete(int position) {
        mNotesList.remove(position);
        notifyItemRemoved(position);
    }

    public class NoteHolder extends RecyclerView.ViewHolder {

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
            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.home_list);
        }
    }

}
