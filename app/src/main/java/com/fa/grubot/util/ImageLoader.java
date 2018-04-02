package com.fa.grubot.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class ImageLoader implements com.stfalcon.chatkit.commons.ImageLoader {

    private Fragment fragment;

    public ImageLoader(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void loadImage(ImageView imageView, String url) {
        if (Globals.ImageMethods.isValidUri(url))
            Glide.with(fragment).load(url).apply(RequestOptions.circleCropTransform()).into(imageView);
        else
            Glide.with(fragment).load("").apply(new RequestOptions().placeholder(Globals.ImageMethods.getRoundImage(fragment.getActivity(), url))).into(imageView);
    }

    public void loadToolbarImage(ImageView imageView, String url) {
        Glide
            .with(fragment)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade(400))
            .apply(new RequestOptions().centerCrop().fitCenter())
            .apply(RequestOptions.bitmapTransform(new BlurTransformation(25)))
            .into(imageView);
    }

    public String getUriOfDrawable(int drawable) {
        Context context = fragment.getActivity();
        Resources res = context.getResources();
        return  Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(drawable)
                + '/' + res.getResourceTypeName(drawable)
                + '/' + res.getResourceEntryName(drawable)).toString();
    }
}
