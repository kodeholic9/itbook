package com.kodeholic.itbook.ui.base;

import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.kodeholic.itbook.common.BitmapCacheManager;
import com.kodeholic.itbook.lib.util.Log;

public class BindingAdapters {
    public static final String TAG = BindingAdapters.class.getSimpleName();

    @BindingAdapter("app:loadImage")
    public static void loadImage(View view, String url) {
        if (url == null || url.length() == 0) {
            Log.d(TAG, "loadImage() - Invalid url");
            return;
        }
        BitmapCacheManager.getInstance(view.getContext()).loadBitmap(url, (ImageView)view, "loadImage");
    }
}
