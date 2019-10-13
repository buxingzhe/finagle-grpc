package io.finagler.grpc.core.server

import com.twitter.finagle.ListeningServer
import com.twitter.util.Await
import io.finagler.grpc.core.runtime.ServerDispatcher

import scala.collection.JavaConversions._

class GRpcServer(serverOptions: ServerOptions, serverServiceConfig: ServiceHandlerRegistry) {

  private var server: ListeningServer = _

  def start(): Unit = {
    for(serviceImpl <- serverServiceConfig.getServiceHandlers) {
      GRpcServerServiceRegistry.registerService(serviceImpl)
    }

    val serviceHandlers = new ServerDispatcher(GRpcServerServiceRegistry.getServices)
    server = H2ServerBuilder.build(serverOptions).serve(serverOptions.getEndpoint, serviceHandlers)
    Await.ready(server)
  }

  def stop(): Unit = {
    if (server != null) server.close()
  }
}
