package com.fa.grubot.util;

import android.util.Log;

import rx.exceptions.OnErrorNotImplementedException;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler exceptionHandler;

    public CustomExceptionHandler() {
        this.exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public void uncaughtException(Thread thread, Throwable throwable) {
        if (throwable instanceof OnErrorNotImplementedException || throwable instanceof IllegalStateException)
            Log.e("Debug","There was an uncaught " + throwable.getCause() + "\nBut I don't give a fuck. Deadline is coming.");
        else
            exceptionHandler.uncaughtException(thread, throwable);
    }
}
