package com.gyoheul.calm_routine.data

import android.util.Log
import com.gyoheul.calm_routine.BuildConfig

object LogModel {
    fun d(obj: Any, message: String, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.d(obj::class.simpleName, message, tr)
        }
    }

    fun e(obj: Any, message: String, tr: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(obj::class.simpleName, message, tr)
        }
    }
}