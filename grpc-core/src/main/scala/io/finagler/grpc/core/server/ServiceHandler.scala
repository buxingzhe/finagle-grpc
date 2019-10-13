package io.finagler.grpc.core.server

case class ServiceHandler(serviceImpl: AnyRef, interfaceClasses: Array[Class[_]]) {
}
