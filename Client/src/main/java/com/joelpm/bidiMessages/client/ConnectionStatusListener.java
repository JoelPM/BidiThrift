package com.joelpm.bidiMessages.client;

/**
 * Interface implemented by classes that need to be notified
 * when the connection is lost or established.
 * 
 * @author Joel Meyer
 */
public interface ConnectionStatusListener {
  /**
   * Called when the connection has been lost.
   */
  public void connectionLost();
  
  /**
   * Called when the connection has been established.
   */
  public void connectionEstablished();
}
