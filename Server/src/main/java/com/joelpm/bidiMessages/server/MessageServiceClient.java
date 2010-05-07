package com.joelpm.bidiMessages.server;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import com.joelpm.bidiMessages.generated.Message;
import com.joelpm.bidiMessages.generated.MessageService;
import com.joelpm.bidiMessages.generated.MessageService.Iface;

/**
 * This class is a stub that the server can use to send messages back
 * to the client.
 * 
 * @author Joel Meyer
 */
public class MessageServiceClient implements Iface {
  protected final TTransport transport;
  protected final String addy;
  protected final int port;
  protected final MessageService.Client client;
  
  public MessageServiceClient(TTransport transport) {
    TSocket tsocket = (TSocket)transport;
    this.transport = transport;
    
    this.client = new MessageService.Client(new TBinaryProtocol(transport));
    this.addy = tsocket.getSocket().getInetAddress().getHostAddress();
    this.port = tsocket.getSocket().getPort();
    
  }
  
  public String getAddy() {
    return addy;
  }
  
  public void sendMessage(Message msg) throws TException {
    this.client.sendMessage(msg);
  }
}
