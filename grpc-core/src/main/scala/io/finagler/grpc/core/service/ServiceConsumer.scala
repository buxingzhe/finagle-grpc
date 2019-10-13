package io.finagler.grpc.core.service

import com.twitter.finagle.Service
import com.twitter.finagle.buoyant.h2.{Request, Response}
import io.finagler.grpc.core.client.ClientOptions

trait ServiceConsumer {

  def isAsync: Boolean

  def isStream: Boolean

  def getClientOptions: ClientOptions

  def getH2ClientService: Service[Request, Response]

}

