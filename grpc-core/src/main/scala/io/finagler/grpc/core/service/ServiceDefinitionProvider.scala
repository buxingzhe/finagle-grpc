package io.finagler.grpc.core.service

trait ServiceDefinitionProvider {

  def getServiceDefinitions: java.util.List[ServiceDefinition]

}
