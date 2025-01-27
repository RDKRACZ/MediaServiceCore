package com.liskovsoft.youtubeapi.service;

import com.liskovsoft.mediaserviceinterfaces.data.MediaItem;
import com.liskovsoft.mediaserviceinterfaces.MediaService;
import com.liskovsoft.mediaserviceinterfaces.data.MediaGroup;
import io.reactivex.Observable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class YouTubeMediaServiceTest {
    private MediaService mService;

    @Before
    public void setUp() {
        // fix issue: No password supplied for PKCS#12 KeyStore
        // https://github.com/robolectric/robolectric/issues/5115
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");

        mService = YouTubeMediaService.instance();
    }

    /**
     * <a href="https://www.ibm.com/developerworks/ru/library/j-5things5/index.html">More info about concurrent utils...</a>
     */
    @Test
    public void testThatSearchNotEmpty() throws InterruptedException {
        Observable<MediaGroup> result = mService.getMediaGroupService().getSearchObserve("hello world");

        CountDownLatch finish = new CountDownLatch(1);

        List<MediaItem> list = new ArrayList<>();

        result.subscribe(mediaTab -> {
            MediaItem mediaItem = mediaTab.getMediaItems().get(0);
            list.add(mediaItem);
            assertNotNull(mediaItem);
            finish.countDown();
        }, throwable -> fail());

        boolean await = finish.await(5_000, TimeUnit.MILLISECONDS);
        assertTrue("Counter not zero", await);
        assertTrue("Has media items", list.size() > 0);
    }

    @Test
    public void testThatRecommendedNotEmpty() throws InterruptedException {
        Observable<MediaGroup> result = mService.getMediaGroupService().getRecommendedObserve();

        CountDownLatch finish = new CountDownLatch(1);

        List<MediaItem> list = new ArrayList<>();

        result.subscribe(mediaTab -> {
            MediaItem mediaItem = mediaTab.getMediaItems().get(0);
            list.add(mediaItem);
            assertNotNull(mediaItem);
            finish.countDown();
        }, throwable -> fail());

        boolean await = finish.await(5_000, TimeUnit.MILLISECONDS);
        assertTrue("Counter not zero", await);
        assertTrue("Has media items", list.size() > 0);
    }

    @Test
    public void testThatRecommendedNotEmpty2() {
        MediaGroup result = mService.getMediaGroupService().getRecommended();

        assertTrue("Has media items", !result.isEmpty());
    }
}