package com.liskovsoft.youtubeapi.browse;

import com.liskovsoft.sharedutils.mylogger.Log;
import com.liskovsoft.youtubeapi.auth.V1.AuthManager;
import com.liskovsoft.youtubeapi.browse.models.grid.GridTab;
import com.liskovsoft.youtubeapi.browse.models.grid.GridTabContinuation;
import com.liskovsoft.youtubeapi.browse.models.grid.GridTabList;
import com.liskovsoft.youtubeapi.browse.models.guide.Guide;
import com.liskovsoft.youtubeapi.browse.models.sections.SectionContinuation;
import com.liskovsoft.youtubeapi.browse.models.sections.SectionList;
import com.liskovsoft.youtubeapi.browse.models.sections.SectionTab;
import com.liskovsoft.youtubeapi.browse.models.sections.SectionTabContinuation;
import com.liskovsoft.youtubeapi.browse.models.sections.SectionTabList;
import com.liskovsoft.youtubeapi.common.helpers.RetrofitHelper;
import com.liskovsoft.youtubeapi.common.models.items.VideoItem;
import retrofit2.Call;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * For auth users only!<br/>
 * Wraps result from the {@link AuthManager} and {@link BrowseManagerSigned}
 */
public class BrowseServiceSigned {
    private static final String TAG = BrowseServiceSigned.class.getSimpleName();
    private final BrowseManagerSigned mBrowseManagerSigned;
    private static BrowseServiceSigned sInstance;
    private Map<String, Guide> mGuideMap = new HashMap<>();

    private BrowseServiceSigned() {
        mBrowseManagerSigned = RetrofitHelper.withJsonPath(BrowseManagerSigned.class);
    }

    public static BrowseServiceSigned instance() {
        if (sInstance == null) {
            sInstance = new BrowseServiceSigned();
        }

        return sInstance;
    }

    public static void unhold() {
        sInstance = null;
    }

    public GridTab getSubscriptions(String authorization) {
        GridTab subs = getGridTab(0, BrowseManagerParams.getSubscriptionsQuery(), authorization);

        // LIVE videos always on top
        if (subs != null && subs.getItemWrappers() != null) {
            Collections.sort(subs.getItemWrappers(), (o1, o2) -> {
                VideoItem item1 = o1.getVideoItem();
                VideoItem item2 = o2.getVideoItem();
                boolean isLive1 = item1 != null && item1.isLive();
                boolean isLive2 = item2 != null && item2.isLive();
                return isLive1 == isLive2 ? 0 : isLive1 ? -1 : 1;
            });
        }

        return subs;
    }

    public List<GridTab> getSubscribedChannelsAZ(String authorization) {
        List<GridTab> gridTabs = getSubscribedChannelsSection(authorization);

        return getPart(gridTabs, 1);
    }

    public List<GridTab> getSubscribedChannelsLastViewed(String authorization) {
        List<GridTab> gridTabs = getSubscribedChannelsSection(authorization);

        if (gridTabs == null) {
            return null;
        }

        List<GridTab> result = getPart(gridTabs, 0);

        // all channels should be unique
        for (GridTab tab : getPart(gridTabs, 1)) {
            if (!result.contains(tab)) {
                result.add(tab);
            }
        }

        return result;
    }

    public List<GridTab> getSubscribedChannelsUpdate(String authorization) {
        List<GridTab> subscribedChannelsAZ = getSubscribedChannelsAZ(authorization);

        if (subscribedChannelsAZ == null) {
            return null;
        }

        Collections.sort(subscribedChannelsAZ, (o1, o2) ->
                o1.hasNewContent() && !o2.hasNewContent() ? -1 : !o1.hasNewContent() && o2.hasNewContent() ? 1 : 0);

        return subscribedChannelsAZ;
    }

    private List<GridTab> getSubscribedChannelsSection(String authorization) {
        List<GridTab> gridTabs = getGridTabs(BrowseManagerParams.getSubscriptionsQuery(), authorization);
        // Exclude All Subscriptions tab (first one)
        return gridTabs != null ? gridTabs.subList(1, gridTabs.size()) : null;
    }

    public List<GridTab> getSubscribedChannelsAll(String authorization) {
        return getSubscribedChannelsSection(authorization);
    }

    public GridTab getHistory(String authorization) {
        //return getGridTab(0, BrowseManagerParams.getHistoryQuery(), authorization); // web client version (needs new parser but contains feedback data)
        return getGridTab(0, BrowseManagerParams.getMyLibraryQuery(), authorization);
    }

    public List<GridTab> getPlaylists(String authorization) {
        List<GridTab> playlists = getGridTabs(BrowseManagerParams.getMyLibraryQuery(), authorization);

        if (playlists != null) {
            playlists.remove(0); // remove "History"
            GridTab myVideos = playlists.get(0); // save "My videos" for later use
            playlists.remove(0); // remove "My videos"
            playlists.remove(1); // remove "Purchases"
            playlists.add(myVideos); // add "My videos" to the end
        }

        return playlists;
    }

    public SectionTab getHome(String authorization) {
        return getSectionTab(BrowseManagerParams.getHomeQuery(), authorization);
    }

    public SectionTab getGaming(String authorization) {
        return getSectionTab(BrowseManagerParams.getGamingQuery(), authorization);
    }

    public SectionTab getNews(String authorization) {
        return getSectionTab(BrowseManagerParams.getNewsQuery(), authorization);
    }

    public SectionTab getMusic(String authorization) {
        return getSectionTab(BrowseManagerParams.getMusicQuery(), authorization);
    }

    public SectionList getChannel(String channelId, String authorization) {
        return getSectionList(BrowseManagerParams.getChannelQuery(channelId), authorization);
    }

    private List<GridTab> getGridTabs(String query, String authorization) {
        if (authorization == null) {
            Log.e(TAG, "getGridTabs: authorization is null.");
            return null;
        }

        List<GridTab> result = null;

        Call<GridTabList> wrapper = mBrowseManagerSigned.getGridTabList(query, authorization);

        GridTabList browseResult = RetrofitHelper.get(wrapper);

        if (browseResult != null) {
            result = browseResult.getTabs();
        } else {
            Log.e(TAG, "getGridTabs: result is null");
        }

        return result;
    }

    private GridTab getGridTab(int index, String query, String authorization) {
        List<GridTab> gridTabs = getGridTabs(query, authorization);

        GridTab result = null;

        if (gridTabs != null) {
            result = gridTabs.get(index);
        }

        return result;
    }

    private List<GridTab> getGridTabs(int fromIndex, String query, String authorization) {
        List<GridTab> gridTabs = getGridTabs(query, authorization);

        List<GridTab> result = null;

        if (gridTabs != null) {
            result = new ArrayList<>();

            for (int i = fromIndex; i < gridTabs.size(); i++) {
                GridTab tab = gridTabs.get(i);

                if (tab.isUnselectable()) {
                    continue;
                }

                result.add(tab);
            }
        }

        return result;
    }

    public SectionContinuation continueSection(String nextKey, String authorization) {
        if (authorization == null) {
            Log.e(TAG, "continueGridTabResult: authorization is null.");
            return null;
        }

        if (nextKey == null) {
            Log.e(TAG, "continueGridTabResult: next search key is null.");
            return null;
        }

        String query = BrowseManagerParams.getContinuationQuery(nextKey);
        Call<SectionContinuation> wrapper = mBrowseManagerSigned.continueSection(query, authorization);

        return RetrofitHelper.get(wrapper);
    }

    public GridTabContinuation continueGridTab(String nextKey, String authorization) {
        if (authorization == null) {
            Log.e(TAG, "continueGridTab: authorization is null.");
            return null;
        }

        if (nextKey == null) {
            Log.e(TAG, "continueGridTab: next search key is null.");
            return null;
        }

        String query = BrowseManagerParams.getContinuationQuery(nextKey);
        Call<GridTabContinuation> wrapper = mBrowseManagerSigned.continueGridTab(query, authorization);

        return RetrofitHelper.get(wrapper);
    }

    public SectionTabContinuation continueSectionTab(String nextKey, String authorization) {
        if (authorization == null) {
            Log.e(TAG, "continueRowsTabResult: authorization is null.");
            return null;
        }

        if (nextKey == null) {
            Log.e(TAG, "continueGridTabResult: next search key is null.");
            return null;
        }

        String query = BrowseManagerParams.getContinuationQuery(nextKey);

        Call<SectionTabContinuation> wrapper = mBrowseManagerSigned.continueSectionTab(query, authorization);

        return RetrofitHelper.get(wrapper);
    }

    private Guide getGuide(String authorization) {
        if (authorization == null) {
            Log.e(TAG, "getGuide: authorization is null.");
            return null;
        }

        Call<Guide> wrapper = mBrowseManagerSigned.getGuide(BrowseManagerParams.getGuideQuery(), authorization);

        return RetrofitHelper.get(wrapper);
    }

    public String getSuggestToken(String authorization) {
        String result = null;

        Guide guide = mGuideMap.get(authorization);

        if (guide == null) {
            mGuideMap.clear();
            guide = getGuide(authorization);

            if (guide != null) {
                mGuideMap.put(authorization, guide);
                result = guide.getSuggestToken();
            }
        } else {
            result = guide.getSuggestToken();
        }

        return result;
    }

    private SectionTabList getSectionTabList(String query, String authorization) {
        if (authorization == null) {
            Log.e(TAG, "getRowsTabResult: authorization is null.");
            return null;
        }

        Call<SectionTabList> wrapper = mBrowseManagerSigned.getSectionTabList(query, authorization);

        return RetrofitHelper.get(wrapper);
    }

    private SectionTab getSectionTab(String query, String authorization) {
        if (authorization == null) {
            Log.e(TAG, "getRowsTab: authorization is null. Query: " + query);
            return null;
        }

        SectionTabList tabs = getSectionTabList(query, authorization);

        if (tabs == null) {
            Log.e(TAG, "getRowsTab: tabs result is empty");
            return null;
        }

        return firstNotEmpty(tabs);
    }

    private SectionList getSectionList(String query, String authorization) {
        if (authorization == null) {
            Log.e(TAG, "getSectionList: authorization is null.");
            return null;
        }

        Call<SectionList> wrapper = mBrowseManagerSigned.getSectionList(query, authorization);

        return RetrofitHelper.get(wrapper);
    }

    private SectionTab firstNotEmpty(SectionTabList tabs) {
        SectionTab result = null;

        if (tabs.getTabs() != null) {
            // find first not empty tab
            for (SectionTab tab : tabs.getTabs()) {
                if (tab.getSections() != null) {
                    result = tab;
                    break;
                }
            }
        } else {
            Log.e(TAG, "firstNotEmpty: tabs are empty");
        }

        return result;
    }

    /**
     * Channels are split by different criteria e.g. (popular and alphanumeric order)
     */
    private List<GridTab> getPart(List<GridTab> gridTabs, int partIndex) {
        List<GridTab> azGridTabs = null;

        if (gridTabs != null) {
            azGridTabs = new ArrayList<>();

            int partIndexFound = 0;

            for (GridTab tab : gridTabs) {
                if (tab.isUnselectable()) {
                    partIndexFound++;
                } else if (partIndexFound == partIndex) {
                    azGridTabs.add(tab);
                }
            }

            if (azGridTabs.isEmpty()) {
                azGridTabs = gridTabs;
            }
        }

        return azGridTabs;
    }
}
