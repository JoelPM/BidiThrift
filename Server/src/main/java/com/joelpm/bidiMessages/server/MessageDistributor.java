package com.joelpm.bidiMessages.server;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import org.apache.thrift.TException;
import com.joelpm.bidiMessages.generated.Message;
import com.joelpm.bidiMessages.generated.MessageService.Iface;

/**
 * A simple class that uses a blocking queue to accept and publish
 * messages. This class should be run in its own thread to ensure
 * that message sending doesn't hijack the message receiving thread.
 * 
 * @author Joel Meyer
 */
public class MessageDistributor implements Iface, Runnable {
  private static final Logger LOGGER = Logger.getLogger(MessageDistributor.class);
  
  private final BlockingQueue<Message> messageQueue;
  private final List<MessageServiceClient> clients;
  
  public MessageDistributor() {
    this.messageQueue = new LinkedBlockingQueue<Message>();
    this.clients = new ArrayList<MessageServiceClient>();
  }
  
  public void addClient(MessageServiceClient client) {
    // There should be some synchronization around this list
    clients.add(client);
    LOGGER.info(String.format("Added client at %s", client.getAddy()));
  }
  
  @Override
  public void run() {
    while (true) {
      try {
        Message msg = messageQueue.take();

        Iterator<MessageServiceClient> clientItr = clients.iterator();
        while (clientItr.hasNext()) {
          MessageServiceClient client = clientItr.next();
          try {
            client.sendMessage(msg);
          } catch (TException te) {
            // Most likely client disconnected, should remove it from the list
            clientItr.remove();
            LOGGER.info(String.format("Removing %s from client list.", client.getAddy()));
            LOGGER.debug(te);
          }
        }
      } catch (InterruptedException ie) {
        LOGGER.debug(ie);
      }
    }
  }

  @Override
  public void sendMessage(Message msg) throws TException {
    messageQueue.add(msg);
    LOGGER.info(String.format("Adding message to queue:\n%s", msg));
  }
}
