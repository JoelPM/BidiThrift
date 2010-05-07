#!/usr/local/bin/thrift --gen java:beans:hashcode -O ../

namespace java com.joelpm.bidiMessages.generated

struct Message {
  1: string clientName,
  2: string message
}

service MessageService {
  oneway void sendMessage(Message msg),
}