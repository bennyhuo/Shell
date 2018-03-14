package com.bennyhuo.shell.api

import java.io.Closeable
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by benny on 03/03/2018.
 */
private const val CMD_EXIT = "exit"

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
                socket.getOutputStream().write("$CMD_EXIT\n".toByteArray())
                socket.getOutputStream().flush()
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
        execute(CMD_EXIT)
        isOpen = false
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

        fun run(cmd: String, file: File) {
            val shell = Shell()
            shell.addListener(object : ShellListener {

                val writer = file.bufferedWriter()

                override fun onResult(newLine: String) {
                    writer.append(newLine)
                            .append('\n')
                }

                override fun onClosed() {
                    writer.close()
                    warn("ShellOutput File: ${file.length()}")
                }
            })
            shell.executeOnce(cmd)
        }

    }
}