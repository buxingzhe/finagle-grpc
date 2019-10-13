package io.finagler.grpc.core.common

import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

import com.google.protobuf.{Message, MessageLite}
import io.finagler.grpc.core.annotations.GRpcMethod
import io.finagler.grpc.core.runtime.ProtobufCodec
import io.finagler.grpc.core.service.ServiceDefinition
import io.finagler.grpc.protocol.protobuf.{RpcRequest, RpcResponse}
import io.finagler.protobuf.dto.common.{GeneralRpcRequest, GeneralRpcResponse}
import io.grpc.MethodDescriptor

import scala.collection.JavaConversions._

object RpcContext {
  private val generalRequestCodec = GeneralRpcRequest.getDefaultInstance
  private val generalResponseCodec = GeneralRpcResponse.getDefaultInstance
  private val clientMap = new ConcurrentHashMap[String, RpcContext]
  private val methodDescriptors = new ConcurrentHashMap[String, MethodDescriptor[_, _]]

  def registerRpcContext(serviceDefinition: ServiceDefinition): Unit = {
    constructMethodDescriptorMap(serviceDefinition)
    constructRpcContextMap(serviceDefinition)
  }

  private def constructRpcContextMap(serviceDefinition: ServiceDefinition): Unit = {
    for (method <- serviceDefinition.getServiceClass.getMethods) {
      val rpcContext = RpcContext(serviceDefinition, method)
      clientMap.putIfAbsent(rpcContext.url, rpcContext)
    }
  }

  private def constructMethodDescriptorMap(serviceDefinition: ServiceDefinition): Unit = {
    for (methodDescriptor <- serviceDefinition.getServiceDescriptor.getMethods) {
      methodDescriptors.put(methodDescriptor.getFullMethodName, methodDescriptor)
    }
  }

  private[grpc] def getRpcContext(serviceDefinition: ServiceDefinition, method: Method): RpcContext = {
    clientMap.get(s"/${serviceDefinition.getContextPath}/${getMethodPath(method)}")
  }

  private def getMethodName(method: Method): String = {
    var methodName = method.getName
    if (method.isAnnotationPresent(classOf[GRpcMethod])) {
      val name = method.getAnnotation(classOf[GRpcMethod]).name
      if (!name.isEmpty) {
        methodName = name
      }
    }
    methodName
  }

  private def getMethodPath(method: Method): String = {
    var methodPath = method.getName
    if (method.isAnnotationPresent(classOf[GRpcMethod])) {
      val path = method.getAnnotation(classOf[GRpcMethod]).path
      if (!path.isEmpty) {
        methodPath = path
      }
    }
    methodPath
  }

}

private[grpc] case class RpcContext(serviceDefinition: ServiceDefinition, method: Method) {
  val rpcName = RpcContext.getMethodName(method)
  val rpcUri = RpcContext.getMethodPath(method)
  val url = s"/${serviceDefinition.getContextPath}/$rpcUri"
  val methodDescriptor = RpcContext.methodDescriptors.get(s"${serviceDefinition.getServiceName}/$rpcName")
  val requestBodyProtoType = methodDescriptor.getRequestMarshaller.asInstanceOf[MethodDescriptor.PrototypeMarshaller[_]].getMessagePrototype.asInstanceOf[Message]
  val responseBodyProtoType = methodDescriptor.getResponseMarshaller.asInstanceOf[MethodDescriptor.PrototypeMarshaller[_]].getMessagePrototype.asInstanceOf[Message]
  private val requestProtoType = if (classOf[RpcRequest[_]].isAssignableFrom(method.getParameterTypes()(0))) RpcContext.generalRequestCodec else requestBodyProtoType
  private val responseProtoType = if (classOf[RpcResponse[_]].isAssignableFrom(method.getReturnType)) RpcContext.generalResponseCodec else responseBodyProtoType
  //val requestBodyClass: Class[_ <: Message] = requestBodyProtoType.getClass
  //val responseBodyClass: Class[_ <: Message] = responseBodyProtoType.getClass
  val requestCodec = new ProtobufCodec[MessageLite](requestProtoType)
  val responseCodec = new ProtobufCodec[MessageLite](responseProtoType)
}
