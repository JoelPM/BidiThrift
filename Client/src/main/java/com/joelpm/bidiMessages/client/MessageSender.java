package com.joelpm.bidiMessages.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import com.joelpm.bidiMessages.generated.Message;
import com.joelpm.bidiMessages.generated.MessageService;

/**
 * The class responsible for sending messages to the server.
 * 
 * @author Joel Meyer
 */
public class MessageSender extends ConnectionRequiredRunnable {
  private final MessageService.Client client;
  private final BlockingQueue<Message> msgSendQueue;
  
  public MessageSender(
      TProtocol protocol,
      ConnectionStatusMonitor connectionMonitor) {
    super(connectionMonitor, "Message Sender");
    this.client = new MessageService.Client(protocol);
    this.msgSendQueue = new LinkedBlockingQueue<Message>();
  }
  
  public void send(Message msg) {
    msgSendQueue.add(msg);
  }
  
  @Override
  public void run() {
    connectWait();
    while (true) {
      try {
        Message msg = msgSendQueue.take();
        try {
          client.sendMessage(msg);
        } catch (TException e) {
          // The message isn't lost, but it could end up being sent out of
          // order - not ideal.
          msgSendQueue.add(msg);
          disconnected();
        }
      } catch (InterruptedException e) {
        // Thread will be interrupted if connection is lost, we should wait
        // for reconnection if that happens.
        connectWait();
      }
    }
  }
}
