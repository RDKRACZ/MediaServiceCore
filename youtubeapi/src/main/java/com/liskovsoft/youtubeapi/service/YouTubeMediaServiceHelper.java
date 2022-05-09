package com.liskovsoft.youtubeapi.service;

import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import com.liskovsoft.youtubeapi.common.helpers.ServiceHelper;
import com.liskovsoft.youtubeapi.common.models.items.Thumbnail;
import com.liskovsoft.youtubeapi.next.v2.impl.mediagroup.MediaGroupImpl;
import com.liskovsoft.youtubeapi.service.data.YouTubeMediaGroup;

import java.util.List;

public final class YouTubeMediaServiceHelper {
    /**
     * Optimal thumbnail for tv screen
     */
    public static final int LOW_RES_THUMBNAIL_INDEX = 2;

    /**
     * Find optimal thumbnail for tv screen
     */
    public static String findLowResThumbnailUrl(List<Thumbnail> thumbnails) {
        if (thumbnails == null) {
            return null;
        }

        int size = thumbnails.size();

        if (size == 0) {
            return null;
        }

        return thumbnails.get(size > LOW_RES_THUMBNAIL_INDEX ? LOW_RES_THUMBNAIL_INDEX : size - 1).getUrl();
    }

    public static String findHighResThumbnailUrl(List<Thumbnail> thumbnails) {
        if (thumbnails == null) {
            return null;
        }

        int size = thumbnails.size();

        if (size == 0) {
            return null;
        }

        return thumbnails.get(size - 1).getUrl();
    }

    /**
     * Additional video info such as user, published etc.
     */
    public static String createInfo(Object... items) {
        return ServiceHelper.itemsToInfo(items);
    }

    public static String extractNextKey(MediaGroup mediaTab) {
        String result = null;

        if (mediaTab instanceof YouTubeMediaGroup) {
            result = ((YouTubeMediaGroup) mediaTab).mNextPageKey;
        } else if (mediaTab instanceof MediaGroupImpl) {
            result = ((MediaGroupImpl) mediaTab).getNextPageKey();
        }

        return result;
    }
}
