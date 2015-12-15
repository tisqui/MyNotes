package com.squirrel.mynotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by squirrel on 12/14/15.
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteHolder> {

    private static final String LOG_TAG = NotesAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private List<Note> mNotesList = Collections.emptyList();
    private Context mContext;

    public NotesAdapter(List<Note> notesList, Context context) {
        mNotesList = notesList;
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public NoteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.custom_notes_adapter_layout, parent, false);
        return new NoteHolder(view);
    }

    /**
     * Update the content of the view with the data
     *
     * @param holder   the holder for the content views
     * @param position of the Note in the list on which we are working
     */
    @Override
    public void onBindViewHolder(NoteHolder holder, int position) {
        holder.mId.setText(mNotesList.get(position).getId() + "");
        holder.mTitle.setText(mNotesList.get(position).getTitle());
        //checking if the notes are Standart or the List type
        if (mNotesList.get(position).getType().equals(Constants.LIST)) {
            //the type of the note is "List", need to show the list of items
            NoteCustomList customList = new NoteCustomList(mContext);
            customList.setUpForHomeAdapter(mNotesList.get(position).getDescription());
            holder.mLinearLayout.removeAllViews();
            holder.mLinearLayout.addView(customList);
            holder.mDescription.setVisibility(View.GONE);
        } else {
            //type of note is Standard
            holder.mDescription.setVisibility(View.GONE);
            holder.mDescription.setText(mNotesList.get(position).getDescription());
        }
        holder.mDate.setText(mNotesList.get(position).getDate() + " " + mNotesList.get(position).getTime());

        if (mNotesList.get(position).getBitmap() != null) {
            //if there is an image
            holder.mImage.setImageBitmap(mNotesList.get(position).getBitmap());
            holder.mImage.setVisibility(View.VISIBLE);
        } else if (mNotesList.get(position).getImagePath().equals(Constants.IMAGE_NOT_SET)) {
            holder.mImage.setVisibility(View.GONE);
        } else {
            holder.mImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mNotesList.size();
    }

    public void setData(List<Note> notes) {
        mNotesList = notes;
    }

    public void delete(int position) {
        mNotesList.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Image dowloading may take time, so this method is for notifying the view
     * when the image is received
     */
    public void notifyImageReceived() {
        notifyDataSetChanged();
    }

    public class NoteHolder extends RecyclerView.ViewHolder {

        private TextView mId;
        private TextView mTitle;
        private TextView mDate;
        private TextView mDescription;
        private ImageView mImage;
        private LinearLayout mLinearLayout;


        public NoteHolder(View itemView) {
            super(itemView);
            mId = (TextView) itemView.findViewById(R.id.id_note_custom_home);
            mTitle = (TextView) itemView.findViewById(R.id.title_note_custom_home);
            mDate = (TextView) itemView.findViewById(R.id.date_time_note_custom_home);
            mDescription = (TextView) itemView.findViewById(R.id.description_note_custom_home);
            mImage = (ImageView) itemView.findViewById(R.id.image_note_custom_home);
            mLinearLayout = (LinearLayout) itemView.findViewById(R.id.home_list);
        }
    }
}
