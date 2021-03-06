/*
 * Copyright (C) 2010-2014 The MPDroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.namelessdev.mpdroid.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.namelessdev.mpdroid.MPDApplication;
import com.namelessdev.mpdroid.R;
import com.namelessdev.mpdroid.helpers.AlbumCoverDownloadListener;
import com.namelessdev.mpdroid.helpers.CoverAsyncHelper;
import com.namelessdev.mpdroid.views.holders.AbstractViewHolder;
import com.namelessdev.mpdroid.views.holders.AlbumViewHolder;

import org.a0z.mpd.Album;
import org.a0z.mpd.Artist;
import org.a0z.mpd.Item;
import org.a0z.mpd.Music;

import java.util.List;

public class AlbumDataBinder extends BaseDataBinder {

    public AlbumDataBinder(MPDApplication app, boolean isLightTheme) {
        super(app, isLightTheme);
    }

    @Override
    public AbstractViewHolder findInnerViews(View targetView) {
        // look up all references to inner views
        AlbumViewHolder viewHolder = new AlbumViewHolder();
        viewHolder.albumName = (TextView) targetView.findViewById(R.id.album_name);
        viewHolder.albumInfo = (TextView) targetView.findViewById(R.id.album_info);
        viewHolder.albumCover = (ImageView) targetView.findViewById(R.id.albumCover);
        viewHolder.coverArtProgress = (ProgressBar) targetView
                .findViewById(R.id.albumCoverProgress);
        return viewHolder;
    }

    @Override
    public int getLayoutId() {
        return R.layout.album_list_item;
    }

    public boolean isEnabled(int position, List<? extends Item> items, Object item) {
        return true;
    }

    public void onDataBind(final Context context, final View targetView,
            final AbstractViewHolder viewHolder, List<? extends Item> items,
            Object item, int position) {
        AlbumViewHolder holder = (AlbumViewHolder) viewHolder;

        final Album album = (Album) item;
        Artist artist = album.getArtist();
        String info = "";
        final long songCount = album.getSongCount();
        if (artist != null) {
            info += artist.mainText() + " - ";
        }
        if (album.getYear() > 0)
            info += Long.toString(album.getYear()) + " - ";
        if (songCount > 0) {
            info += String.format(context.getString(songCount > 1 ? R.string.tracksInfoHeaderPlural
                    : R.string.tracksInfoHeader),
                    songCount, Music.timeToString(album.getDuration()));
        }
        holder.albumName.setText(album.mainText());
        if (info != null && info.length() > 0) {
            holder.albumInfo.setVisibility(View.VISIBLE);
            holder.albumInfo.setText(info);
        } else {
            holder.albumInfo.setVisibility(View.GONE);
        }

        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(app);

        if (artist == null || album.isUnknown()) { // full albums list or
                                                   // unknown album
            holder.albumCover.setVisibility(View.GONE);
        } else {
            holder.albumCover.setVisibility(View.VISIBLE);
            final CoverAsyncHelper coverHelper = new CoverAsyncHelper(app, settings);
            final int height = holder.albumCover.getHeight();
            // If the list is not displayed yet, the height is 0. This is a
            // problem, so set a fallback one.
            coverHelper.setCoverMaxSize(height == 0 ? 128 : height);

            loadPlaceholder(coverHelper);

            // display cover art in album listing if caching is on
            if (album.getAlbumInfo().isValid() && enableCache) {
                // listen for new artwork to be loaded
                final AlbumCoverDownloadListener acd = new AlbumCoverDownloadListener(context,
                        holder.albumCover, holder.coverArtProgress,
                        lightTheme, false);
                final AlbumCoverDownloadListener oldAcd = (AlbumCoverDownloadListener) holder.albumCover
                        .getTag(R.id.AlbumCoverDownloadListener);
                if (oldAcd != null) {
                    oldAcd.detach();
                }

                holder.albumCover.setTag(R.id.AlbumCoverDownloadListener, acd);
                holder.albumCover.setTag(R.id.CoverAsyncHelper, coverHelper);
                coverHelper.addCoverDownloadListener(acd);
                loadArtwork(coverHelper, album.getAlbumInfo());
            }
        }
    }

    @Override
    public View onLayoutInflation(Context context, View targetView, List<? extends Item> items) {
        targetView.findViewById(R.id.albumCover).setVisibility(
                enableCache ? View.VISIBLE : View.GONE);
        return targetView;
    }
}
