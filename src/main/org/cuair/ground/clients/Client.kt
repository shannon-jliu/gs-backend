package org.cuair.ground.clients

import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * Abstract superclass for all clients on the ground server which provides a mechanism for clients
 * to perform requests in a separate thread from the application thread.
 */
abstract class Client {

  /** State variable to indicate whether or not requests should be attempted */
  @Volatile protected var running = false

  /** Must call this method in order to start reconnection logic */
  open fun start() {
    if (running) return
    running = true
    // Separate thread for queueing and performing requests
    thread {
      while (running) {
        run()
        try {
          Thread.sleep(10)
        } catch (e: InterruptedException) {
          e.printStackTrace()
        }
      }
    }
  }

  /** Stops the re-requesting mechanism of this client. */
  open fun stop() {
    running = false
  }

  /** Perform re-requesting */
  abstract fun run()

  companion object {
    /** Default timeout value */
    @JvmField val TIMEOUT: Long = 1000

    /** Units of time for TIMEOUT */
    @JvmField val TIME_UNIT = TimeUnit.MILLISECONDS
  }
}
