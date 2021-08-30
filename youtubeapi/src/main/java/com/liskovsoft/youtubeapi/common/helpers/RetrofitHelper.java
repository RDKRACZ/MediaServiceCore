package com.liskovsoft.youtubeapi.common.helpers;

import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.liskovsoft.youtubeapi.BuildConfig;
import com.liskovsoft.youtubeapi.app.AppConstants;
import com.liskovsoft.youtubeapi.common.converters.jsonpath.converter.JsonPathConverterFactory;
import com.liskovsoft.youtubeapi.common.converters.jsonpath.converter.JsonPathSkipConverterFactory;
import com.liskovsoft.youtubeapi.common.converters.jsonpath.typeadapter.JsonPathSkipTypeAdapter;
import com.liskovsoft.youtubeapi.common.converters.jsonpath.typeadapter.JsonPathTypeAdapter;
import com.liskovsoft.youtubeapi.common.converters.querystring.converter.QueryStringConverterFactory;
import com.liskovsoft.youtubeapi.common.converters.regexp.converter.RegExpConverterFactory;
import com.liskovsoft.youtubeapi.common.interceptors.UnzippingInterceptor;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RetrofitHelper {
    // Ignored when specified url is absolute
    private static final String DEFAULT_BASE_URL = "https://www.youtube.com";
    // Default timeout 10 sec
    private static final long TIMEOUT_SEC = 30;
    public static boolean sForceEnableProfiler;
    private static Retrofit sGsonRetrofit;
    private static Retrofit sJsonPathRetrofit;
    private static Retrofit sJsonPathSkipRetrofit;
    private static Retrofit sQueryStringRetrofit;
    private static Retrofit sRegExpRetrofit;
    private static OkHttpClient sOkHttpClient;

    public static <T> T withGson(Class<T> clazz) {
        if (sGsonRetrofit == null) {
            sGsonRetrofit = createRetrofit(GsonConverterFactory.create());
        }

        return sGsonRetrofit.create(clazz);
    }

    public static <T> T withJsonPath(Class<T> clazz) {
        if (sJsonPathRetrofit == null) {
            sJsonPathRetrofit = createRetrofit(JsonPathConverterFactory.create());
        }

        return sJsonPathRetrofit.create(clazz);
    }

    /**
     * Skips first line of the response
     */
    public static <T> T withJsonPathSkip(Class<T> clazz) {
        if (sJsonPathSkipRetrofit == null) {
            sJsonPathSkipRetrofit = createRetrofit(JsonPathSkipConverterFactory.create());
        }

        return sJsonPathSkipRetrofit.create(clazz);
    }

    public static <T> T withQueryString(Class<T> clazz) {
        if (sQueryStringRetrofit == null) {
            sQueryStringRetrofit = createRetrofit(QueryStringConverterFactory.create());
        }

        return sQueryStringRetrofit.create(clazz);
    }

    public static <T> T withRegExp(Class<T> clazz) {
        if (sRegExpRetrofit == null) {
            sRegExpRetrofit = createRetrofit(RegExpConverterFactory.create());
        }

        return sRegExpRetrofit.create(clazz);
    }

    public static <T> T get(Call<T> wrapper) {
        try {
            return wrapper.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> JsonPathTypeAdapter<T> adaptJsonPathSkip(Class<?> clazz) {
        Configuration conf = Configuration
                .builder()
                .mappingProvider(new GsonMappingProvider())
                .jsonProvider(new GsonJsonProvider())
                .build();

        ParseContext parser = JsonPath.using(conf);

        return new JsonPathSkipTypeAdapter<>(parser, clazz);
    }

    private static Retrofit.Builder createBuilder() {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder().baseUrl(DEFAULT_BASE_URL);

        retrofitBuilder.client(createOkHttpClient());

        return retrofitBuilder;
    }

    private static Retrofit createRetrofit(Converter.Factory factory) {
        Retrofit.Builder builder = createBuilder();

        return builder
                .addConverterFactory(factory)
                .build();
    }
    
    public static OkHttpClient createOkHttpClient() {
        if (sOkHttpClient == null) {
            OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();

            //disableCache(okBuilder);

            setupConnectionParams(okBuilder);

            addCommonHeaders(okBuilder);

            enableDecompression(okBuilder);

            debugSetup(okBuilder);

            sOkHttpClient = okBuilder.build();
        }

        return sOkHttpClient;
    }

    private static void disableCache(OkHttpClient.Builder okBuilder) {
        // Disable cache (could help with dlfree error on Eltex)
        okBuilder.cache(null);
    }

    /**
     * https://stackoverflow.com/questions/39219094/sockettimeoutexception-in-retrofit<br/>
     * https://stackoverflow.com/questions/63047533/connection-pool-okhttp
     */
    private static void setupConnectionParams(OkHttpClient.Builder okBuilder) {
        // Default timeout 10 sec
        okBuilder.connectTimeout(TIMEOUT_SEC, TimeUnit.SECONDS);
        okBuilder.readTimeout(TIMEOUT_SEC, TimeUnit.SECONDS);
        okBuilder.writeTimeout(TIMEOUT_SEC, TimeUnit.SECONDS);
        //okBuilder.connectionPool(new ConnectionPool(10, 10, TimeUnit.MINUTES));
        //okBuilder.protocols(listOf(Protocol.HTTP_1_1));
    }

    private static void addCommonHeaders(OkHttpClient.Builder builder) {
        builder.addInterceptor(chain -> {
            Request.Builder requestBuilder = chain.request().newBuilder();
            requestBuilder.header("User-Agent", AppConstants.APP_USER_AGENT);

            // Enable compression in production
            requestBuilder.header("Accept-Encoding", BuildConfig.DEBUG ?
                    AppConstants.ACCEPT_ENCODING_IDENTITY : AppConstants.ACCEPT_ENCODING_DEFAULT);

            // Emulate browser request
            //requestBuilder.header("Connection", "keep-alive");
            //requestBuilder.header("Cache-Control", "max-age=0");
            requestBuilder.header("Referer", "https://www.youtube.com/tv");

            return chain.proceed(requestBuilder.build());
        });
    }

    /**
     * Checks that response is compressed and do uncompress if needed.
     */
    private static void enableDecompression(Builder builder) {
        // Add gzip/deflate/br support
        //builder.addInterceptor(BrotliInterceptor.INSTANCE);
        builder.addInterceptor(new UnzippingInterceptor());
    }

    private static void debugSetup(OkHttpClient.Builder okBuilder) {
        if (BuildConfig.DEBUG) {
            // Force enable for unit tests.
            // If you enable it to all requests - expect slowdowns.
            //if (sForceEnableProfiler) {
            //    addProfiler(okBuilder);
            //}

            addProfiler(okBuilder);

            addLogger(okBuilder);
        }
    }

    private static void addProfiler(OkHttpClient.Builder okBuilder) {
        okBuilder.addInterceptor(new OkHttpProfilerInterceptor());
    }

    private static void addLogger(OkHttpClient.Builder okBuilder) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        okBuilder.addInterceptor(logging);
    }
}
