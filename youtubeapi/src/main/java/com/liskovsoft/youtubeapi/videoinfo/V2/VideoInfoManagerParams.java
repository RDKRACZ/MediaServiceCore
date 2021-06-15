package com.liskovsoft.youtubeapi.videoinfo.V2;

import com.liskovsoft.youtubeapi.app.AppService;
import com.liskovsoft.youtubeapi.common.helpers.ServiceHelper;

public class VideoInfoManagerParams {
    private static final String VIDEO_ID = "\"videoId\":\"%s\",\"cpn\":\"%s\"";

    public static String getVideoInfoQuery(String videoId) {
        String channelTemplate = String.format(VIDEO_ID, videoId, AppService.instance().getClientPlaybackNonce());
        return ServiceHelper.createQuery(channelTemplate);
    }
}
