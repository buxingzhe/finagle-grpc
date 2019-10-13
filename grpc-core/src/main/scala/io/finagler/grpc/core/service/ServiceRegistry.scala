package io.finagler.grpc.core.service

import java.util
import java.util.concurrent.ConcurrentHashMap

import io.finagler.grpc.core.common.RpcContext

import scala.collection.JavaConversions._

object ServiceRegistry {
  private val serviceDefinitions: ConcurrentHashMap[String, ServiceDefinition] = new ConcurrentHashMap[String, ServiceDefinition]
  ServiceDefinitionLoader()

  def register(serviceDefinition: ServiceDefinition): Unit = {
    serviceDefinitions.put(serviceDefinition.getServiceClass.getName, serviceDefinition)
    RpcContext.registerRpcContext(serviceDefinition)
  }

  def register(serviceDescList: util.List[ServiceDefinition]): Unit = {
    for (serviceDescriptor <- serviceDescList) {
      register(serviceDescriptor)
    }
  }

  def getServiceDefinition(serviceClass: Class[_]): ServiceDefinition = serviceDefinitions.get(serviceClass.getName)

  def getServiceDefinitions: util.Collection[ServiceDefinition] = {
    serviceDefinitions.values()
  }

}
