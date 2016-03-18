package com.example.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.example.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private static final String LOG_TAG = GridAdapter.class.getSimpleName();
    private static GridItemCallback mCaller;
    private Context mContext;
    private CursorAdapter mCursorAdapter;

    public GridAdapter(Context context, Cursor cursor, GridItemCallback gridItemCallback) {
        mCaller = gridItemCallback;
        mContext = context;
        mCursorAdapter = new CursorAdapter(mContext, cursor, 0) {
            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                ImageView view = new ImageView(context);
                view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                return view;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                String url = cursor.getString(
                        cursor.getColumnIndex(MovieContract.Columns.POSTER));
                Picasso.with(context)
                        .load(url)
                        .placeholder(R.drawable.black_square)
                        .into((ImageView) view);
            }
        };
    }

    public static GridItemCallback getCaller() {
        return mCaller;
    }

    public void swapCursor(Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    public interface GridItemCallback {
        public void onGridItemClick(int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ViewHolder(View v) {
            super(v);
            v.setClickable(true);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            getCaller().onGridItemClick(getAdapterPosition());
        }
    }


}
