package io.finagler.grpc.core.server

import com.twitter.finagle.ListeningServer
import com.twitter.finagle.buoyant.H2
import com.twitter.finagle.ssl.KeyCredentials
import com.twitter.finagle.ssl.client.SslClientConfiguration
import com.twitter.finagle.transport.Transport
import com.twitter.util.{Future, FuturePool}
import io.finagler.grpc.core.runtime.ServerDispatcher

object ServerBuilder {
  def newBuilder: ServerBuilder = {
    new ServerBuilder
  }
}

class ServerBuilder {
  private var serverOptions: ServerOptions = _
  private var serviceHandlerRegistry: ServiceHandlerRegistry = _
  private var isDaemon = false

  def withOptions(serverOptions: ServerOptions): ServerBuilder = {
    this.serverOptions = serverOptions
    this
  }

  def withHandlerRegistry(serviceHandlerRegistry: ServiceHandlerRegistry): ServerBuilder = {
    this.serviceHandlerRegistry = serviceHandlerRegistry
    this
  }

  def withDaemon(isDaemon: Boolean): ServerBuilder = {
    this.isDaemon = isDaemon
    this
  }

  def startServer(): Future[ListeningServer] = {
    assert(serverOptions != null)
    assert(serviceHandlerRegistry != null)

    var server = H2.server
    if (serverOptions.getLabel != null) server = server.withLabel(serverOptions.getLabel)
    val keyFile = serverOptions.getKeyFile
    val certFile = serverOptions.getCertFile
    if (keyFile != null && keyFile.exists() && certFile != null && certFile.exists()) {
      server = server.configured(Transport.ClientSsl(Some(SslClientConfiguration(keyCredentials = KeyCredentials.CertAndKey(certFile, keyFile)))))
    }
    server = server.withRequestTimeout(serverOptions.getRequestTimeout)
      .withSession.maxIdleTime(serverOptions.getMaxSessionIdleTime)

    serviceHandlerRegistry.getServiceHandlers.forEach(serviceImpl => GRpcServerServiceRegistry.registerService(serviceImpl))
    val serverDispatcher = new ServerDispatcher(GRpcServerServiceRegistry.getServices)
    if (isDaemon) {
      FuturePool.unboundedPool {
        server.serve(serverOptions.getEndpoint, serverDispatcher)
      }
    } else {
      FuturePool.immediatePool {
        server.serve(serverOptions.getEndpoint, serverDispatcher)
      }
    }
  }

}
