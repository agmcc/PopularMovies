package com.example.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.popularmovies.data.Serializer;

import java.util.HashMap;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder> {

    public CursorAdapter mCursorAdapter;
    private Context mContext;

    //Dataset goes here- will test with reviews
    public DetailAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursorAdapter = new CursorAdapter(mContext, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                //here use method to decide which view to use (movie card, review card, trailer card)
                return LayoutInflater.from(context)
                        .inflate(R.layout.detail_recycler_item, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                //then also decide what to put into view
                if (cursor != null) {
                    byte[] reviewBytes = cursor.getBlob(DetailFragment.ColumnIndices.reviewsInd);
                    HashMap<String, String> reviewMap =
                            (HashMap<String, String>) Serializer.deserialize(reviewBytes);

                    String author = (String) reviewMap.keySet().toArray()[cursor.getPosition()];
                    String content = (String) reviewMap.values().toArray()[cursor.getPosition()];

                    ViewHolder holder = (ViewHolder) view.getTag();
                    holder.author_text.setText(author);
                    holder.content_text.setText(content);
                }
            }
        };
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursorAdapter.getCursor().moveToPosition(position);
        View view = holder.itemView;
        view.setTag(holder);
        mCursorAdapter.bindView(view, mContext, mCursorAdapter.getCursor());
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView author_text;
        public TextView content_text;

        public ViewHolder(View v) {
            super(v);
            author_text = (TextView) v.findViewById(R.id.cardview_author);
            content_text = (TextView) v.findViewById(R.id.cardview_content);
        }
    }

}
