package com.joelpm.bidiMessages.client;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import com.joelpm.bidiMessages.generated.MessageService;

/**
 * The class responsible for reading and deserializing incoming messages.
 * Should be run in its own thread.
 * 
 * @author Joel Meyer
 */
public class MessageReceiver extends ConnectionRequiredRunnable {
  private final MessageService.Processor processor;
  private final TProtocol protocol;
  
  public MessageReceiver(
      TProtocol protocol,
      MessageService.Iface messageService,
      ConnectionStatusMonitor connectionMonitor) {
    super(connectionMonitor, "Message Receiver");
    this.protocol = protocol;
    this.processor = new MessageService.Processor(messageService);
  }
  
  @Override
  public void run() {
    connectWait();
    while (true) {
      try {
        while (processor.process(protocol, protocol) == true) { }
      } catch (TException e) {
        disconnected();
      }
    }
  }
}
