package com.masu.platochess

import java.io.File
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/** App-private installed executable. No network, shell command, or external storage execution. */
class StockfishAdvisor(private val executable: File) : MoveAdvisor, AutoCloseable {
    @Volatile private var process: Process? = null
    private var output = LinkedBlockingQueue<String>()
    private fun send(command: String) {
        val p = process ?: error("Engine is not running")
        p.outputStream.write((command + "\n").toByteArray())
        p.outputStream.flush()
    }
    private fun until(prefix: String, timeout: Long): String {
        val end = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout)
        while (true) {
            val left = end - System.nanoTime()
            check(left > 0) { "Stockfish timeout" }
            val line = output.poll(left, TimeUnit.NANOSECONDS) ?: error("Stockfish timeout")
            if (line == "__EOF__") error("Stockfish exited")
            if (line.startsWith(prefix)) return line
        }
    }
    private fun start() {
        check(executable.isFile) { "Missing native Stockfish binary" }
        val p = ProcessBuilder(executable.absolutePath).redirectErrorStream(true).start()
        process = p
        val queue = LinkedBlockingQueue<String>()
        output = queue
        Thread {
            try { p.inputStream.bufferedReader().useLines { lines -> lines.forEach { queue.offer(it) } } }
            catch (_: java.io.IOException) { /* Destroying a process closes its reader asynchronously. */ }
            finally { queue.offer("__EOF__") }
        }.apply { isDaemon = true; name = "Stockfish output"; start() }
        send("uci"); until("uciok", 5000)
        send("setoption name Threads value 1")
        send("setoption name Hash value 16")
        send("isready"); until("readyok", 5000)
    }
    @Synchronized override fun bestMove(fen: String): String? {
        try {
            if (process == null) start()
            send(EngineProtocol.position(fen))
            send("go movetime 700")
            return EngineProtocol.parseBestMove(until("bestmove ", 4000))
        } catch (e: Exception) { close(); throw e }
    }
    override fun close() {
        process?.destroy()
        process = null
    }
}
