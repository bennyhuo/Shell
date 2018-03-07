package com.bennyhuo.shell.api

/**
 * Created by benny on 03/03/2018.
 */
interface ShellListener {
    fun onResult(newLine: String)

    fun onClosed() = Unit
}