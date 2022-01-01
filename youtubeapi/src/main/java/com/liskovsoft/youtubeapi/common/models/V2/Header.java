package com.liskovsoft.youtubeapi.common.models.V2;

import com.liskovsoft.youtubeapi.common.converters.jsonpath.JsonPath;
import com.liskovsoft.youtubeapi.common.models.items.Thumbnail;

import java.util.List;

public class Header {
    @JsonPath("$.thumbnail.thumbnails[*]")
    private List<Thumbnail> mThumbnails;
    @JsonPath("$.thumbnailOverlays[0].thumbnailOverlayTimeStatusRenderer.text")
    private TextItem mDuration;
    @JsonPath("$.thumbnailOverlays[0].thumbnailOverlayResumePlaybackRenderer.percentDurationWatched")
    private int mPercentWatched = -1;
    @JsonPath({
            "$.thumbnailOverlays[0].thumbnailOverlayTimeStatusRenderer.style",
            "$.thumbnailOverlays[1].thumbnailOverlayTimeStatusRenderer.style"
    })
    private String mBadgeStyle;
    @JsonPath("$.thumbnailOverlays[1].thumbnailOverlayTimeStatusRenderer.text")
    private TextItem mBadgeText;
    @JsonPath("$.movingThumbnail.thumbnails[0].url")
    private String mMovingThumbnailUrl;

    public List<Thumbnail> getThumbnails() {
        return mThumbnails;
    }

    public String getDuration() {
        return mDuration != null ? mDuration.getText() : null;
    }

    public int getPercentWatched() {
        return mPercentWatched;
    }

    public String getBadgeStyle() {
        return mBadgeStyle;
    }

    public String getBadgeText() {
        return mBadgeText != null ? mBadgeText.getText() : null;
    }

    /**
     * Animated thumbnail preview in webp format
     */
    public String getMovingThumbnailUrl() {
        return mMovingThumbnailUrl;
    }
}
