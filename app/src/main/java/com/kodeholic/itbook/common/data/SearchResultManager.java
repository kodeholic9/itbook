package com.kodeholic.itbook.common.data;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.jakewharton.disklrucache.DiskLruCache;
import com.kodeholic.itbook.common.DatabaseManager;
import com.kodeholic.itbook.common.JobExecutor;
import com.kodeholic.itbook.database.TBL_BOOKMARK;
import com.kodeholic.itbook.database.TBL_NEW_LIST;
import com.kodeholic.itbook.database.TBL_SEARCH_RESULT;
import com.kodeholic.itbook.lib.http.HttpListener;
import com.kodeholic.itbook.lib.http.HttpResponse;
import com.kodeholic.itbook.lib.http.HttpUtil;
import com.kodeholic.itbook.lib.util.JSUtil;
import com.kodeholic.itbook.lib.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SearchResultManager {
    public static final String TAG = SearchResultManager.class.getSimpleName();

    //캐시 크기
    public static final int CACHE_SIZE = 1 * 1024 * 1024;

    private volatile static SearchResultManager sInstance;
    private Context mContext = null;

    //DB
    private TBL_SEARCH_RESULT mTblSearchResult;

    //메모리 캐시
    private LruCache<String, SearchResult> mCache;

    private SearchResultManager(Context context) {
        mContext = context;
        mTblSearchResult = DatabaseManager.getInstance(mContext).getTblSearchResult();

        //메모리 기반의 텍스트 캐시 생성
        mCache = new LruCache<String, SearchResult>(CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, SearchResult value) {
                return value.length();
            }
        };
    }

    public static SearchResultManager getInstance(Context context) {
        if (sInstance == null) {
            synchronized (SearchResultManager.class) {
                if (sInstance == null) {
                    sInstance = new SearchResultManager(context);
                }
            }
        }
        return sInstance;
    }

    public void clear() {
        if (mCache != null) { mCache.evictAll(); }
    }

    /**
     * SearchResult를 캐시에서 찾고, 없으면 DB에서 찾아 반환한다.
     * @param queryString
     * @param pageNo
     * @param f
     * @return
     */
    public SearchResult getSearchResult(String queryString, int pageNo, String f) {
        Log.d(TAG, "getSearchResult() - f: " + f + ", queryString: " + queryString + ", pageNo: " + pageNo);
        //메모리 캐시에서 참조
        String cacheKey = toKey(queryString, pageNo);
        SearchResult result;
        synchronized (mCache) {
            if ((result = mCache.get(cacheKey)) != null) {
                return result;
            }
        }

        //DB에서 참조
        if ((result = mTblSearchResult.getSearchResult(queryString, pageNo, f)) != null) {
            putToCache(cacheKey, result, "getSearchResult");
            return result;
        }

        return null;
    }

    private String toKey(String queryString, int pageNo) {
        return queryString + "." + pageNo;
    }

    /**
     * SearchResult를 BookListRes 포맷으로 반환한다.
     * @param queryString
     * @param pageNo
     * @param f
     * @return
     */
    public BookListRes getBookListRes(String queryString, int pageNo, String f) {
        try {
            SearchResult result = getSearchResult(queryString, pageNo, f);
            if (result != null) {
                return JSUtil.json2Object(result.getJsonResult(), BookListRes.class);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Cache에 보관한다.
     * @param result
     * @param f
     */
    public void putToCache(String cacheKey, SearchResult result, String f) {
        Log.d(TAG, "putToCache(1) - f: " + f + ", cacheKey: " + cacheKey);
        synchronized (mCache) {
            mCache.put(cacheKey, result);
        }
        Log.d(TAG, "putToCache() - create: " + mCache.createCount()
                + ", eviction: " + mCache.evictionCount()
                + ", hit: " + mCache.hitCount()
                + ", miss: " + mCache.missCount()
                + ", size: " + mCache.size()
        );
    }

    /**
     * Database 및 Cache에 보관한다.
     * @param queryString
     * @param pageNo
     * @param jsonResult
     * @param f
     */
    public void putToCache(String queryString, int pageNo, String jsonResult, String f) {
        Log.d(TAG, "putToCache(2) - f: " + f + ", queryString: " + queryString + ", pageNo: " + pageNo);
        String cacheKey = toKey(queryString, pageNo);
        SearchResult result = new SearchResult(queryString, pageNo, jsonResult);
        //cache
        putToCache(cacheKey, result, f);
        //database
        mTblSearchResult.addSearchResult(result, f);
    }
}
