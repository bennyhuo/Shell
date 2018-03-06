@file:JvmName("Main")

package com.bennyhuo.shell.server

import android.os.Looper

fun main(args: Array<String>) {
    Looper.prepareMainLooper()
    ShellServer("ShellServer").start()
    Looper.loop()
}
