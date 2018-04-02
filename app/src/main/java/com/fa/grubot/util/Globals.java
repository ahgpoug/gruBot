package com.fa.grubot.util;

import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class Globals {

    public static class ImageMethods {
        /**
         * Метод, создающий круглую картинку
         * @param context Контекст Activity.
         * @param name Отображаемое имя.
         * @return Возвращает картинку (класс TextDrawable) с первой буквой по центру.
         */
        public static TextDrawable getRoundImage(Context context, String name) {
            ColorGenerator generator = ColorGenerator.MATERIAL;
            int color = generator.getColor(name);

            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    .useFont(Typeface.createFromAsset(context.getAssets(), "OpenSans-Light.ttf"))
                    .bold()
                    .withBorder(2)
                    .width(100)
                    .height(100)
                    .endConfig()
                    .buildRound(String.valueOf(name.charAt(0)).toUpperCase(), color);
            return drawable;
        }

        public static boolean isValidUri(String uri) {
            return (uri.startsWith("file://") || uri.startsWith("http://") || uri.startsWith("https://"));
        }
    }

    public static class InternetMethods {
        private static boolean isNetworkAvailable(Context context) {
            Runtime runtime = Runtime.getRuntime();
            int exitValue = -1;

            try {
                Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
                exitValue = ipProcess.waitFor();
            }
            catch (IOException | InterruptedException ignored) {}

            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();

            return netInfo != null && exitValue == 0 && netInfo.isConnectedOrConnecting();
        }

        public static Observable<Boolean> getNetworkObservable(Context context) {
            return Observable.just(isNetworkAvailable(context))
                    .filter(result -> result != null)
                    .subscribeOn(Schedulers.io())
                    .timeout(15, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    public static MaterialDialog getLoadingDialog(Context context) {
        return new MaterialDialog.Builder(context)
                .content("Загрузка...")
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .cancelable(false)
                .build();
    }
}
