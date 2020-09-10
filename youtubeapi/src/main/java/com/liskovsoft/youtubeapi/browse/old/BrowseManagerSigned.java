package com.liskovsoft.youtubeapi.browse.old;

import com.liskovsoft.youtubeapi.browse.old.models.BrowseResult;
import com.liskovsoft.youtubeapi.browse.old.models.BrowseResultContinuation;
import com.liskovsoft.youtubeapi.browse.old.models.sections.RowsTabContinuation;
import com.liskovsoft.youtubeapi.browse.old.models.sections.TabbedBrowseResult;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * For signed users!
 */
public interface BrowseManagerSigned {
    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<BrowseResult> getBrowseResult(@Body String browseQuery, @Header("Authorization") String auth);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<BrowseResultContinuation> continueBrowseResult(@Body String browseQuery, @Header("Authorization") String auth);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<TabbedBrowseResult> getTabbedBrowseResult(@Body String browseQuery, @Header("Authorization") String auth);

    @Headers("Content-Type: application/json")
    @POST("https://www.youtube.com/youtubei/v1/browse")
    Call<RowsTabContinuation> continueTabbedBrowseResult(@Body String browseQuery, @Header("Authorization") String auth);
}