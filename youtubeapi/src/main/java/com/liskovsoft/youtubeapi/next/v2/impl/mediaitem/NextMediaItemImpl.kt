package com.liskovsoft.youtubeapi.next.v2.impl.mediaitem

import com.liskovsoft.youtubeapi.common.helpers.ServiceHelper
import com.liskovsoft.youtubeapi.next.v2.gen.kt.NextVideoItem
import com.liskovsoft.youtubeapi.next.v2.helpers.*
import com.liskovsoft.youtubeapi.service.YouTubeMediaServiceHelper

data class NextMediaItemImpl(var nextVideoItem: NextVideoItem): BaseMediaItemImpl() {
    override val videoIdItem by lazy { nextVideoItem.getVideoId() }
    override val channelIdItem: String? = null
    override val titleItem by lazy { nextVideoItem.getTitle() }
    override val cardThumbImageUrl by lazy { nextVideoItem.getThumbnails()?.findHighResThumbnailUrl() }
    override val playlistIdItem by lazy { nextVideoItem.getPlaylistId() }
    override val playlistIndexItem by lazy { nextVideoItem.getPlaylistIndex() }
    override val mediaUrl by lazy { ServiceHelper.videoIdToFullUrl(videoIdItem) ?: null }
    override val descriptionItem by lazy { YouTubeMediaServiceHelper.createDescription(nextVideoItem.getAuthor()) ?: null }
    override val playlistParamsItem by lazy { nextVideoItem.getParams() }
}