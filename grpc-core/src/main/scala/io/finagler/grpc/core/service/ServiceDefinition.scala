package io.finagler.grpc.core.service

import io.grpc.ServiceDescriptor

class ServiceDefinition(serviceContextPath: String, interfaceClazz: Class[_], serviceDescriptor: ServiceDescriptor) {

  def getServiceName: String = serviceDescriptor.getName

  def getServiceClass: Class[_] = interfaceClazz

  def getServiceDescriptor: ServiceDescriptor = serviceDescriptor

  def getContextPath: String = serviceContextPath

}
