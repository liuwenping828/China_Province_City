package com.wenping.chinacity.utils;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

/**
 * 图片加载类,统一适配(方便换库,方便管理)
 */
public class ImageLoader {

    /*public static void load(Context context, @DrawableRes int imageRes, ImageView view) {
        Glide.with(context).load(imageRes).crossFade().into(view);
    }*/

    public static void loadAndDiskCache(Context context, @DrawableRes int imageRes, ImageView view) {
        Glide.with(context).load(imageRes).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().into(view);
    }
}
