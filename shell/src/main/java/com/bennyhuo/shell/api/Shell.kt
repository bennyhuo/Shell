package com.bennyhuo.shell.api

import java.io.Closeable
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by benny on 03/03/2018.
 */
class Shell : Closeable {
    private var executor: ExecutorService? = null
    private var shellSocket: Socket? = null

    private val shellListeners = ArrayList<ShellListener>()

    @Volatile
    private var isOpen = false

    fun addListener(listener: ShellListener) {
        synchronized(this) {
            shellListeners.add(listener)
        }
    }

    fun removeListener(listener: ShellListener) {
        synchronized(this) {
            shellListeners.remove(listener)
        }
    }

    fun execute(cmd: String) {
        if (isOpen) {
            val socket = shellSocket!!
            executor?.execute {
                try {
                    socket.getOutputStream().write("$cmd\n".toByteArray())
                    socket.getOutputStream().flush()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                }
            }
        }
    }

    fun executeOnce(cmd: String) {
        isOpen = true
        val socket = Socket()
        shellSocket = socket
        val executor = Executors.newCachedThreadPool()
        this.executor = executor
        executor.execute {
            socket.connect(InetSocketAddress("127.0.0.1", 62741))
            try {
                socket.getOutputStream().write("$cmd\n".toByteArray())
                socket.getOutputStream().flush()
                //使用这个可以优雅的只关闭输出；如果直接关闭流，会同时把socket也关掉
                socket.shutdownOutput()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            executor.execute {
                try {
                    socket.getInputStream().bufferedReader().forEachLine { newLine ->
                        val listeners = synchronized(this) { shellListeners.clone() as List<ShellListener> }
                        listeners.forEach {
                            it.onResult(newLine)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        socket.close()
                        executor.shutdownNow()
                        this.executor = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        onClosed()
                    }
                }
            }
        }
    }

    fun open() {
        if (isOpen) return
        isOpen = true
        val socket = Socket()
        shellSocket = socket
        val executor = Executors.newCachedThreadPool()
        this.executor = executor
        executor.execute {
            socket.connect(InetSocketAddress("127.0.0.1", 62741))
            executor.execute {
                try {
                    socket.getInputStream().bufferedReader().forEachLine { newLine ->
                        val listeners = synchronized(this) { shellListeners.clone() as List<ShellListener> }
                        listeners.forEach {
                            it.onResult(newLine)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    try {
                        socket.close()
                        executor.shutdownNow()
                        this.executor = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        onClosed()
                    }
                }
            }
        }
    }

    override fun close() {
        if (!isOpen) return
        isOpen = false
        try {
            shellSocket!!.shutdownOutput()
        } catch (e: Exception) {
            e.printStackTrace()
            onClosed()
        }
    }

    private fun onClosed() {
        warn("ShellSocket onClosed.")
        val listeners = synchronized(this) { shellListeners.clone() as List<ShellListener> }
        listeners.forEach {
            it.onClosed()
        }
    }

    companion object {
        fun run(cmd: String, block: (String) -> Unit) {
            val shell = Shell()
            shell.addListener(object : ShellListener {

                val resultBuilder = StringBuilder()

                override fun onResult(newLine: String) {
                    resultBuilder.append(newLine)
                            .append('\n')
                }

                override fun onClosed() {
                    block(resultBuilder.toString())
                }
            })
            shell.executeOnce(cmd)
        }
    }
}