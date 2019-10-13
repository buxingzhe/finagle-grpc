package io.finagler.grpc.core.server

import java.util

class ServiceHandlerRegistry {

  private val serviceHandlers: java.util.List[ServiceHandler] = new util.ArrayList[ServiceHandler]()

  def addServiceHandler(serviceHandler: AnyRef, interfaceClasses: Array[Class[_]]): ServiceHandlerRegistry = {
    serviceHandlers.add(ServiceHandler(serviceHandler, interfaceClasses))
    this
  }

  def getServiceHandlers: java.util.List[ServiceHandler] = serviceHandlers

}
