package com.bennyhuo.shell.server

import android.util.Log

/**
 * Created by benny on 05/03/2018.
 */
val DEBUG = false

inline fun <reified T> T.debug(log: Any?) {
    if (DEBUG)
        Log.d(T::class.java.simpleName, log.toString())
}

inline fun <reified T> T.warn(log: Any?) {
    Log.e(T::class.java.simpleName, log.toString())
}