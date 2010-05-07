package com.joelpm.bidiMessages.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 * This class is responsible for notifying others when the
 * connection is lost or established and for attempting to
 * reconnect when the connection is lost.
 * 
 * @author Joel Meyer
 */
public class ConnectionStatusMonitor {
  /**
   * Simple task used to attempt a reconnect every few seconds.
   */
  private class RetryTask extends TimerTask {
    @Override public void run() {
      tryOpen();
    }
  }
  
  private final Timer timer;
  
  private final TTransport transport;
  private final AtomicBoolean connected;
  private final List<ConnectionStatusListener> listeners;
  
  public ConnectionStatusMonitor(TTransport transport) {
    this.transport = transport;
    this.connected = new AtomicBoolean(false);
    this.listeners = new ArrayList<ConnectionStatusListener>();
    
    this.timer = new Timer();
  }
  
  public void addListener(ConnectionStatusListener listener) {
    listeners.add(listener);
  }
  
  public void disconnected(ConnectionStatusListener noticer) {
    if (connected.compareAndSet(true, false)) {
      for (ConnectionStatusListener listener : listeners) {
        // The thread running the noticer is our current execution thread. If we
        // notify him he'll block and we'll be deadlocked. Since he noticed the
        // disconnect he is responsible for initiating his own wait state.
        if (listener == noticer) continue;
        listener.connectionLost();
      }
      
      // Try to reconnect in five seconds
      timer.schedule(new RetryTask(), 5 * 1000);
    }
  }
  
  /**
   * Attempts to reconnect to the server.
   */
  public void tryOpen() {
    if (connected.get()) return;
    
    // Make sure it's closed
    transport.close();
    
    try {
      transport.open();
      connected.set(true);
      for (ConnectionStatusListener listener : listeners) {
        listener.connectionEstablished();
      }
      return;
    } catch (TTransportException e) {
      
    }
    
    timer.schedule(new RetryTask(), 5 * 1000);
  }
}
