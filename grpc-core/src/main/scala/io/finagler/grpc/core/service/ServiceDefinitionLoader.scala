package io.finagler.grpc.core.service

import com.twitter.concurrent.Once
import com.twitter.finagle.util.LoadService

object ServiceDefinitionLoader {

  private[this] def registerServices = Once {
    LoadService[ServiceDefinitionProvider]().foreach { serviceProvider =>
        ServiceRegistry.register(serviceProvider.getServiceDefinitions)
      }
  }

  def apply(): Unit = registerServices()
}
