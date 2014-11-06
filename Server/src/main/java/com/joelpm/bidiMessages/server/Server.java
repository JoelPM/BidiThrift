package com.joelpm.bidiMessages.server;

import org.apache.log4j.Logger;

import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransport;
import com.joelpm.bidiMessages.generated.MessageService;

/**
 * A simple server that accepts messages from clients and broadcasts
 * them out to all connected clients.
 * 
 * @author Joel Meyer
 */
public class Server {
  private static final Logger LOGGER = Logger.getLogger(Server.class);
  
  public static void main(String[] args) throws Exception {
    final int port = Integer.parseInt(args[0]);
    
    final MessageDistributor messageDistributor = new MessageDistributor();

    new Thread(messageDistributor).start();
    
    // Using our own TProcessorFactory gives us an opportunity to get
    // access to the transport right after the client connection is
    // accepted.
    TProcessorFactory processorFactory = new TProcessorFactory(null) {
      @Override
      public TProcessor getProcessor(TTransport trans) {
        messageDistributor.addClient(new MessageServiceClient(trans));
        return new MessageService.Processor(messageDistributor);
      }
    };

    TServerTransport serverTransport = new TServerSocket(port);
    TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(serverTransport);
    serverArgs.processorFactory(processorFactory);
    TServer server = new TThreadPoolServer(serverArgs);
    LOGGER.info("Server started");
    server.serve();
  }
}
