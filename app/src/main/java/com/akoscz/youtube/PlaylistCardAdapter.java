package com.akoscz.youtube;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.akoscz.youtube.model.PlaylistVideos;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoContentDetails;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 * A RecyclerView.Adapter subclass which adapts {@link Video}'s to CardViews.
 */
public class PlaylistCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final DecimalFormat sFormatter = new DecimalFormat("#,###,###");
    private final PlaylistVideos mPlaylistVideos;
    private final YouTubeRecyclerViewFragment.LastItemReachedListener mListener;
    private static int counter = 0;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final Context mContext;
        public final TextView mTitleText;
        public final TextView mDescriptionText;
        public final ImageView mThumbnailImage;
        public final ImageView mShareIcon;
        public final TextView mShareText;
        public final TextView mDurationText;
        public final TextView mViewCountText;
        public final TextView mLikeCountText;
        public final TextView mDislikeCountText;

        public ViewHolder(View v) {
            super(v);
            mContext = v.getContext();
            mTitleText = (TextView) v.findViewById(R.id.video_title);
            mDescriptionText = (TextView) v.findViewById(R.id.video_description);
            mThumbnailImage = (ImageView) v.findViewById(R.id.video_thumbnail);
            mShareIcon = (ImageView) v.findViewById(R.id.video_share);
            mShareText = (TextView) v.findViewById(R.id.video_share_text);
            mDurationText = (TextView) v.findViewById(R.id.video_dutation_text);
            mViewCountText= (TextView) v.findViewById(R.id.video_view_count);
            mLikeCountText = (TextView) v.findViewById(R.id.video_like_count);
            mDislikeCountText = (TextView) v.findViewById(R.id.video_dislike_count);
        }
    }

    public PlaylistCardAdapter(PlaylistVideos playlistVideos, YouTubeRecyclerViewFragment.LastItemReachedListener lastItemReachedListener) {
        mPlaylistVideos = playlistVideos;
        mListener = lastItemReachedListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflate a card layout

        RecyclerView.ViewHolder vh = null;
        if(viewType == 1){

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_video_card, parent, false);
            // populate the viewholder
            vh = new ViewHolder(v);
            return vh;
        }else if(viewType == 2){

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.adslayout, parent, false);
            // populate the viewholder
            vh = new ViewHolderLayoutTwo(v);
        }
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

        Video video = null;
        switch (getItemViewType(position)) {


            case 1:
            if (mPlaylistVideos.size() == 0) {
                return;
            }

            if(counter < mPlaylistVideos.size()) {
                video = mPlaylistVideos.get(counter);


                final VideoSnippet videoSnippet = video.getSnippet();
                final VideoContentDetails videoContentDetails = video.getContentDetails();
                final VideoStatistics videoStatistics = video.getStatistics();

                ((PlaylistCardAdapter.ViewHolder) holder).mTitleText.setText(videoSnippet.getTitle());
                ((PlaylistCardAdapter.ViewHolder) holder).mDescriptionText.setText(videoSnippet.getDescription());

                // load the video thumbnail image
                Picasso.with(((PlaylistCardAdapter.ViewHolder) holder).mContext)
                        .load(videoSnippet.getThumbnails().getHigh().getUrl())
                        .placeholder(R.drawable.video_placeholder)
                        .into(((PlaylistCardAdapter.ViewHolder) holder).mThumbnailImage);

                // set the click listener to play the video
                final Video finalVideo1 = video;
                ((PlaylistCardAdapter.ViewHolder) holder).mThumbnailImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((PlaylistCardAdapter.ViewHolder) holder).mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + finalVideo1.getId())));
                    }
                });

                // create and set the click listener for both the share icon and share text
                final Video finalVideo = video;
                View.OnClickListener shareClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Watch \"" + videoSnippet.getTitle() + "\" on YouTube");
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "http://www.youtube.com/watch?v=" + finalVideo.getId());
                        sendIntent.setType("text/plain");
                        ((PlaylistCardAdapter.ViewHolder) holder).mContext.startActivity(sendIntent);
                    }
                };
                ((PlaylistCardAdapter.ViewHolder) holder).mShareIcon.setOnClickListener(shareClickListener);
                ((PlaylistCardAdapter.ViewHolder) holder).mShareText.setOnClickListener(shareClickListener);

                // set the video duration text
                ((PlaylistCardAdapter.ViewHolder) holder).mDurationText.setText(parseDuration(videoContentDetails.getDuration()));
                // set the video statistics
                ((PlaylistCardAdapter.ViewHolder) holder).mViewCountText.setText(sFormatter.format(videoStatistics.getViewCount()));
                ((PlaylistCardAdapter.ViewHolder) holder).mLikeCountText.setText(sFormatter.format(videoStatistics.getLikeCount()));
                ((PlaylistCardAdapter.ViewHolder) holder).mDislikeCountText.setText(sFormatter.format(videoStatistics.getDislikeCount()));

                if (mListener != null) {
                    // get the next playlist page if we're at the end of the current page and we have another page to get
                    final String nextPageToken = mPlaylistVideos.getNextPageToken();
                    if (!isEmpty(nextPageToken) && position == mPlaylistVideos.size() - 1) {
                        holder.itemView.post(new Runnable() {
                            @Override
                            public void run() {
                                mListener.onLastItem(position, nextPageToken);
                            }
                        });
                    }
                }
            }
                counter++;
                break;

            case 2:break;
        }
    }

    @Override
    public int getItemCount() {
        return mPlaylistVideos.size() + 2;
    }

    private boolean isEmpty(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        return false;
    }

    @Override
    public int getItemViewType(int position) {

        if(position  == 0 || position == 6){

            return 2;
        }else{

            return 1;
        }
    }

    private String parseDuration(String in) {
        boolean hasSeconds = in.indexOf('S') > 0;
        boolean hasMinutes = in.indexOf('M') > 0;

        String s;
        if (hasSeconds) {
            s = in.substring(2, in.length() - 1);
        } else {
            s = in.substring(2, in.length());
        }

        String minutes = "0";
        String seconds = "00";

        if (hasMinutes && hasSeconds) {
            String[] split = s.split("M");
            minutes = split[0];
            seconds = split[1];
        } else if (hasMinutes) {
            minutes = s.substring(0, s.indexOf('M'));
        } else if (hasSeconds) {
            seconds = s;
        }

        // pad seconds with a 0 if less than 2 digits
        if (seconds.length() == 1) {
            seconds = "0" + seconds;
        }

        return minutes + ":" + seconds;
    }

    public static class ViewHolderLayoutTwo extends RecyclerView.ViewHolder{


        public ViewHolderLayoutTwo(View itemView) {
            super(itemView);
        }
    }
}