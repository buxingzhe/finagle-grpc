package io.finagler.grpc.core.server

import com.twitter.finagle.buoyant.H2
import com.twitter.finagle.ssl.KeyCredentials
import com.twitter.finagle.ssl.client.SslClientConfiguration
import com.twitter.finagle.transport.Transport

object H2ServerBuilder {

  def build(serverOptions: ServerOptions): H2.Server = {
    var server = H2.server
    if (serverOptions.getLabel != null) server = server.withLabel(serverOptions.getLabel)
    val keyFile = serverOptions.getKeyFile
    val certFile = serverOptions.getCertFile
    if (keyFile != null && keyFile.exists() && certFile != null && certFile.exists()) {
      server = server.configured(Transport.ClientSsl(Some(SslClientConfiguration(keyCredentials = KeyCredentials.CertAndKey(certFile, keyFile)))))
    }
    server.withRequestTimeout(serverOptions.getRequestTimeout)
      .withSession.maxIdleTime(serverOptions.getMaxSessionIdleTime)
  }
}



