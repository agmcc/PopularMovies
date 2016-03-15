package com.example.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.popularmovies.data.MovieContract;
import com.example.popularmovies.data.Serializer;

import java.util.HashMap;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder> {

//    HashMap<String, String> reviewMap;
    public CursorAdapter mCursorAdapter;
    private Context mContext;

    //Dataset goes here- will test with reviews
    public DetailAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursorAdapter = new CursorAdapter(mContext, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                return LayoutInflater.from(context)
                        .inflate(R.layout.detail_recycler_item, parent, false);
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                if (cursor != null) {
//                    //should put these in DF member variables
//                    final int posterInd = 0;
//                    final int titleInd = 1;
//                    final int dateInd = 2;
//                    final int ratingInd = 3;
//                    final int summaryInd = 4;
//                    final int trailersInd = 5;
//                    final int reviewsInd = 6;
                    byte[] reviewBytes = cursor.getBlob(cursor.getColumnIndex(MovieContract.Columns.REVIEWS));
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
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.detail_recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursorAdapter.getCursor().moveToPosition(position);
        View view = holder.itemView;
        view.setTag(holder);
        mCursorAdapter.bindView(view, mContext, mCursorAdapter.getCursor());
        //shpuld use ordered collection
//        holder.author_text.setText((String)reviewMap.keySet().toArray()[position]);
//        holder.content_text.setText((String)reviewMap.values().toArray()[position]);
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
