package io.finagler.grpc.core.client

import com.twitter.finagle.Service
import com.twitter.finagle.buoyant.H2
import com.twitter.finagle.buoyant.h2.{Request, Response}
import com.twitter.finagle.ssl.KeyCredentials
import com.twitter.finagle.ssl.client.SslClientConfiguration
import com.twitter.finagle.transport.Transport

object H2ClientBuilder {

  def build(clientOptions: ClientOptions): Service[Request, Response] = {
    var client = H2.client
    if (clientOptions.getLabel != null) client = client.withLabel(clientOptions.getLabel)
    val keyFile = clientOptions.getKeyFile
    val certFile = clientOptions.getCertFile
    if (keyFile != null && keyFile.exists() && certFile != null && certFile.exists()) {
      client = client.configured(Transport.ClientSsl(Some(SslClientConfiguration(keyCredentials = KeyCredentials.CertAndKey(certFile, keyFile)))))
    }
    client.withRequestTimeout(clientOptions.getRequestTimeout)
      .withSession.maxIdleTime(clientOptions.getMaxSessionIdleTime)
      .withSessionPool.minSize(clientOptions.getMinPoolSize)
      .withSessionPool.maxSize(clientOptions.getMaxPoolSize)
      .newService(clientOptions.getHosts)
  }
}



