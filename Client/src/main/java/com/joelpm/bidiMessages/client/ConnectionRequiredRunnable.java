package com.joelpm.bidiMessages.client;

import org.apache.log4j.Logger;

/**
 * An abstract class that can be extended by tasks requiring a connection
 * to the server. Provides utility methods for a task to notify others that
 * the connection has been dropped as well as be paused until the connection
 * is resumed.
 * 
 * @author Joel Meyer
 */
public abstract class ConnectionRequiredRunnable implements ConnectionStatusListener, Runnable {
  private static final Logger LOGGER = Logger.getLogger(ConnectionRequiredRunnable.class);
  protected final ConnectionStatusMonitor connectionMonitor;
  protected final String threadName;
  protected Thread executingThread;
  
  public ConnectionRequiredRunnable(ConnectionStatusMonitor connectionMonitor, String name) {
    this.connectionMonitor = connectionMonitor;
    this.connectionMonitor.addListener(this);
    this.threadName = name;
  }
  
  /**
   * Should be called if the task determines that the connection has
   * been dropped.
   */
  protected void disconnected() {
    LOGGER.info(String.format("%s detected a disconnect from the server.", threadName));
    connectionMonitor.disconnected(this);
    connectWait();
  }

  /**
   * Can be called by the task upon startup to halt execution until
   * the connection to the server has been established.
   */
  protected synchronized void connectWait() {
    executingThread = Thread.currentThread();
    try {
      LOGGER.info(String.format("%s waiting for connection to be established.", threadName));
      wait();
    } catch (InterruptedException e) {
      LOGGER.debug(String.format("%s caught InterruptedException:", threadName));
      LOGGER.debug(e);
    }
    LOGGER.info(String.format("%s notified of connection, resuming execution", threadName));
  }
  
  /**
   * Interrupts the executing thread which is most likely blocked on 
   * 1) socket read (in the case of the MessageReceiver)
   * 2) queue read (in the case of the MessageSender)
   * Regardless of what it's up to, when the thread is interrupted
   * it will wait until notified of reconnection, at which point it
   * will resume sending/receiving.
   * 
   * @see com.joelpm.bidiMessages.client.ConnectionStatusListener#connectionLost()
   */
  public synchronized void connectionLost() {
    executingThread.interrupt();
  }
  
  /**
   * Notifies the waiting thread so that execution is resumed.
   * 
   * @see com.joelpm.bidiMessages.client.ConnectionStatusListener#connectionEstablished()
   */
  public synchronized void connectionEstablished() {
    notifyAll();
  }
}
