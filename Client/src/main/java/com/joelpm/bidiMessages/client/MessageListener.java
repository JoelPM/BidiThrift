package com.joelpm.bidiMessages.client;

import com.joelpm.bidiMessages.generated.Message;

/**
 * Interface implemented by classes that want to be notified when
 * new messages are received.
 * 
 * @author Joel Meyer
 */
public interface MessageListener {
  /**
   * Called when a new message is received.
   * @param msg The message that was received.
   */
  public void messageReceived(Message msg);
}
