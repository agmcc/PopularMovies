package com.example.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.popularmovies.DetailFragment.Indices;
import com.example.popularmovies.data.Serializer;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.HashMap;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder> {

    private static final String LOG_TAG = DetailAdapter.class.getSimpleName();

    private static final int INFO_COUNT = 1;
    private static final int INFO = 0;
    private static final int TRAILER = 1;
    private static final int REVIEW = 2;
    public static HashMap<String, URL> trailerMap;
    public static Context mContext;
    public static int trailerOffsetInd = 0;
    private static boolean mShowReviewDetails;
    private Cursor mCursor;
    private int trailerCount = 0;
    private int reviewCount = 0;
    private HashMap<String, String> reviewMap;
    private int reviewOffsetInd = 0;
    private static final int MAX_REVIEW_LINES = 3;

    public DetailAdapter(Cursor cursor, Context context) {
        mCursor = cursor;
        mContext = context;
        if (mCursor != null) {
            if (mCursor.moveToFirst()) {
                byte[] trailerBytes = mCursor.getBlob(Indices.trailers);
                if (trailerBytes != null) {
                    trailerMap = (HashMap<String, URL>) Serializer.deserialize(trailerBytes);
                    trailerCount = trailerMap.values().size();
                }
                byte[] reviewBytes = mCursor.getBlob(Indices.reviews);
                if (reviewBytes != null) {
                    reviewMap = (HashMap<String, String>) Serializer.deserialize(reviewBytes);
                    reviewCount = reviewMap.values().size();
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return INFO;

        if (trailerCount > 0 && reviewCount > 0) {
            trailerOffsetInd = 1;
            reviewOffsetInd = trailerCount + 1;
            if (position > 0 && position < trailerCount + 1)
                return TRAILER;
            else if (position > trailerCount)
                return REVIEW;
        } else if (trailerCount > 0) {
            trailerOffsetInd = 1;
            if (position > 0)
                return TRAILER;
        } else if (reviewCount > 0) {
            reviewOffsetInd = 1;
            if (position > 0)
                return REVIEW;
        }
        return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layoutId;
        switch (viewType) {
            case INFO:
                layoutId = R.layout.detail_movie_info;
                break;
            case TRAILER:
                layoutId = R.layout.detail_movie_trailer;
                break;
            case REVIEW:
                layoutId = R.layout.detail_movie_review;
                break;
            default:
                layoutId = 0;
        }

        View v = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        if (mCursor == null)
            return;
        if (!mCursor.moveToFirst())
            return;

        switch (getItemViewType(position)) {
            case INFO:
                Picasso.with(mContext).load(mCursor.getString(Indices.poster))
                        .placeholder(R.drawable.black_square)
                        .fit()
                        .centerInside()
                        .into(holder.poster);
                holder.title.setText(mCursor.getString(Indices.title));

                holder.date.setText(Integer.toString(
                        mCursor.getInt(Indices.date)
                ));
                holder.rating.setText(mCursor.getString(Indices.rating));
                holder.summary.setText(mCursor.getString(Indices.summary));
                break;
            case TRAILER:
                holder.trailer.setText((String) trailerMap.keySet().toArray()[position - trailerOffsetInd]);
                break;
            case REVIEW:
                holder.author.setText((String) reviewMap.keySet().toArray()[position - reviewOffsetInd]);
                holder.review.setText((String) reviewMap.values().toArray()[position - reviewOffsetInd]);
                holder.review.post(new Runnable() {
                    @Override
                    public void run() {
//                        Log.i(LOG_TAG, "Lines: " + holder.review.getLineCount());
                        if (holder.review.getLineCount() <= MAX_REVIEW_LINES) {
                            holder.expandImage.setVisibility(View.GONE);
                            holder.itemView.setClickable(false);
                        } else {
                            holder.review.setMaxLines(MAX_REVIEW_LINES);
                            holder.review.setEllipsize(TextUtils.TruncateAt.END);
                        }
                    }
                });
                break;
            default:
                throw new UnsupportedOperationException("No matching items");
        }
    }

    @Override
    public int getItemCount() {
        return INFO_COUNT + reviewCount + trailerCount;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public ImageView poster;
        public TextView title;
        public TextView date;
        public TextView rating;
        public TextView summary;
        public TextView trailer;
        public TextView author;
        public TextView review;
        public ImageView expandImage;

        public ViewHolder(View v) {
            super(v);
            v.setClickable(true);
            v.setOnClickListener(this);
            poster = (ImageView) v.findViewById(R.id.detail_movie_poster);
            title = (TextView) v.findViewById(R.id.detail_movie_title);
            date = (TextView) v.findViewById(R.id.detail_movie_date);
            rating = (TextView) v.findViewById(R.id.detail_movie_rating);
            summary = (TextView) v.findViewById(R.id.detail_movie_summary);
            trailer = (TextView) v.findViewById(R.id.detail_trailer_title);
            author = (TextView) v.findViewById(R.id.detail_author);
            review = (TextView) v.findViewById(R.id.detail_review);
            expandImage = (ImageView)v.findViewById(R.id.expandReviewImage);
        }

        @Override
        public void onClick(View v) {
            int type = getItemViewType();
            switch (type) {
                case TRAILER:
                    URL clickURL = (URL) DetailAdapter.trailerMap.values().toArray()[getAdapterPosition() - trailerOffsetInd];
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(clickURL.toString()));
                    DetailAdapter.mContext.startActivity(intent);
                    break;
                case REVIEW:
                    mShowReviewDetails = !mShowReviewDetails;
                    if (mShowReviewDetails) {
                        review.setMaxLines(Integer.MAX_VALUE);
                        review.setEllipsize(null);
                        expandImage.setImageResource(R.drawable.ic_arrow_up);
                    } else {
                        review.setMaxLines(3);
                        review.setEllipsize(TextUtils.TruncateAt.END);
                        expandImage.setImageResource(R.drawable.ic_arrow_down);
                    }
                    break;
            }
        }
    }
}