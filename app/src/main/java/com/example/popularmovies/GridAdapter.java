//package com.example.popularmovies;
//
//import android.content.Context;
//import android.database.Cursor;
//import android.support.v7.widget.RecyclerView;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.CursorAdapter;
//import android.widget.ImageView;
//
//import com.example.popularmovies.data.MovieContract;
//import com.nostra13.universalimageloader.core.ImageLoader;
//
//
//public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {
//
//    private static final String LOG_TAG = GridAdapter.class.getSimpleName();
//
//    private static GridItemCallback mCallback;
//    private Context mContext;
//    private CursorAdapter mCursorAdapter;
//
//    public GridAdapter(Context context, Cursor cursor, GridItemCallback gridItemCallback) {
//        mCallback = gridItemCallback;
//        mContext = context;
//        mCursorAdapter = new CursorAdapter(mContext, cursor, 0) {
//            @Override
//            public View newView(Context context, Cursor cursor, final ViewGroup parent) {
//                return new ImageView(context);
//            }
//
//            @Override
//            public void bindView(View view, Context context, Cursor cursor) {
////                Log.i(LOG_TAG, "Binding view");
//                //use index?
//                String url = cursor.getString(
//                        cursor.getColumnIndex(MovieContract.Columns.POSTER));
//
//                ImageLoader.getInstance().displayImage(url, (ImageView)view);
////                Picasso.with(context)
////                        .load(url)
////                        .into((ImageView) view);
//            }
//        };
//    }
//
//    public void swapCursor(Cursor cursor) {
//        mCursorAdapter.swapCursor(cursor);
//        notifyDataSetChanged();
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
//        return new ViewHolder(v);
//    }
//
//    @Override
//    public void onBindViewHolder(ViewHolder holder, final int position) {
//        mCursorAdapter.getCursor().moveToPosition(position);
//        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mCallback.onGridItemClick(position);
//            }
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return mCursorAdapter.getCount();
//    }
//
//    public interface GridItemCallback {
//        public void onGridItemClick(int position);
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//
//        public ViewHolder(View v) {
//            super(v);
//        }
//
//    }
//
//}
