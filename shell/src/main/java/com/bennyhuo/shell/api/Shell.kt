package com.bennyhuo.shell.api

import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by benny on 03/03/2018.
 */
class Shell {
    private var executor: ExecutorService? = null
    private lateinit var shellSocket: Socket

    private val shellListeners = ArrayList<ShellListener>()

    @Volatile
    private var isOpen = false

    fun addListener(listener: ShellListener){
        synchronized(this){
            shellListeners.add(listener)
        }
    }

    fun removeListener(listener: ShellListener) {
        synchronized(this){
            shellListeners.remove(listener)
        }
    }

    fun execute(cmd: String){
        if(isOpen) {
            executor?.execute {
                try {
                    shellSocket.getOutputStream().write("$cmd\n".toByteArray())
                    shellSocket.getOutputStream().flush()
                } catch (e: Exception) {
                }
            }
        }
    }

    fun open() {
        if(isOpen) return
        isOpen = true
        val socket = Socket()
        shellSocket = socket
        val executor = Executors.newCachedThreadPool()
        this.executor = executor
        executor.execute {
            socket.connect(InetSocketAddress("127.0.0.1", 62741))
            executor.execute {
                try {
                    socket.getInputStream().bufferedReader().forEachLine {
                        newLine ->
                        val listeners = synchronized(this){ shellListeners.clone() as List<ShellListener> }
                        listeners.forEach {
                            it.onResult(newLine)
                        }
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    fun close() {
        if(!isOpen) return
        isOpen = false
        shellSocket.close()
        executor?.shutdownNow()
        executor = null
    }
}