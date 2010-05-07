package com.joelpm.bidiMessages.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import com.joelpm.bidiMessages.generated.Message;
import com.joelpm.bidiMessages.generated.MessageService;

/**
 * Client that connects to the server and handles the sending and receiving
 * of message objects. Will also attempt to reconnect if the server disappears.
 * 
 * @author Joel Meyer
 */
public class Client implements MessageService.Iface {
  private final ConnectionStatusMonitor connectionMonitor;
  private final MessageSender sender;
  private final MessageReceiver receiver;
  
  private final String name;
  
  private final TTransport transport;
  private final TProtocol protocol;
  
  private final List<MessageListener> listeners;
  
  public Client(String name, String server, int port, MessageService.Iface messageHandler) {
    this.name = name;
    this.transport = new TSocket(server, port);
    this.protocol = new TBinaryProtocol(transport);
   
    this.connectionMonitor = new ConnectionStatusMonitor(transport);
   
    this.sender = new MessageSender(protocol, connectionMonitor);
    this.receiver = new MessageReceiver(protocol, messageHandler, connectionMonitor);
   
    new Thread(sender).start();
    new Thread(receiver).start();
   
    this.connectionMonitor.tryOpen();
   
    this.listeners = new ArrayList<MessageListener>();
  }
  
  public void addListener(MessageListener listener) {
    listeners.add(listener);
  }
  
  public void sendMessageToServer(String msg) {
    sender.send(new Message(name, msg));
  }
  
  @Override
  public void sendMessage(Message msg) throws TException {
    for (MessageListener listener : listeners) {
      listener.messageReceived(msg);
    }
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {
    MessageService.Iface handler = new MessageService.Iface() {
      @Override
      public void sendMessage(Message msg) throws TException {
        System.out.println("Got msg: " + msg);
      }
    };

    Client client = new Client(args[0], args[1], Integer.parseInt(args[2]), handler);
    
    client.sendMessageToServer("Hello there!");
    
    for (int i = 0; i < 100; i++) {
      client.sendMessageToServer(String.format("Message %s", i));
      Thread.sleep(1000);
    }
  }
}
