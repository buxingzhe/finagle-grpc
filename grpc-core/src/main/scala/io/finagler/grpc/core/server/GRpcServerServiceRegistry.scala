package io.finagler.grpc.core.server

import java.lang.reflect.Method

import com.google.protobuf.MessageLite
import com.twitter.util.{Future, FuturePool}
import io.finagler.grpc.core.common.RpcContext
import io.finagler.grpc.core.runtime
import io.finagler.grpc.core.runtime.ServerDispatcher
import io.finagler.grpc.core.service.{ServiceDefinitionLoader, ServiceRegistry}
import io.finagler.grpc.protocol.RequestBuilder
import io.finagler.grpc.protocol.protobuf.{RpcRequest, RpcResponse}
import io.finagler.protobuf.dto.common.GeneralRpcRequest
import io.grpc.MethodDescriptor.MethodType

import scala.collection.Seq
import scala.collection.mutable.ArrayBuffer

object GRpcServerServiceRegistry {
  private var serviceList: Seq[ServerDispatcher.Service] = new ArrayBuffer[ServerDispatcher.Service]
  ServiceDefinitionLoader()

  @SuppressWarnings(Array("unchecked"))
  def registerService(serviceDesc: ServiceHandler): Unit = {
    val interfaceClasses = serviceDesc.interfaceClasses
    for (interfaceClazz <- interfaceClasses) {
      addService(serviceDesc.serviceImpl, interfaceClazz)
    }
  }

  private def addService(serviceImpl: AnyRef, interfaceClazz: Class[_]): Unit = {
    val serviceDefinition = ServiceRegistry.getServiceDefinition(interfaceClazz)
    if (serviceDefinition != null) {
      var rpcCalls: Seq[ServerDispatcher.Rpc] = Seq()
      val methods: Array[Method] = interfaceClazz.getMethods
      for (method <- methods) {
         val rpcContext = RpcContext(serviceDefinition, method)
        val dispatcher = createDispatcher(rpcContext, serviceImpl)
        if (dispatcher != null) rpcCalls = rpcCalls :+ dispatcher
      }

      val service: ServerDispatcher.Service = new ServerDispatcher.Service() {
        override def name: String = serviceDefinition.getContextPath
        override def rpcs: Seq[ServerDispatcher.Rpc] = rpcCalls
      }

      serviceList.synchronized {
        serviceList = serviceList :+ service
      }
    }
  }

  def getServices: Seq[ServerDispatcher.Service] = serviceList

  private def createDispatcher(rpcContext: RpcContext, serviceImpl: AnyRef): ServerDispatcher.Rpc = {
    if (rpcContext.methodDescriptor == null) return null

    val uri = rpcContext.rpcUri
    val reqCodec = rpcContext.requestCodec
    val repCodec = rpcContext.responseCodec
    var dispatcher: ServerDispatcher.Rpc = null
    rpcContext.methodDescriptor .getType match {
      case MethodType.UNARY =>
        val func = buildUnaryHandlerFunction(rpcContext, serviceImpl)
        dispatcher = new ServerDispatcher.Rpc.UnaryToUnary[MessageLite, MessageLite](uri, func, reqCodec, repCodec)
      case MethodType.CLIENT_STREAMING =>
        val func = new Function[runtime.Stream[MessageLite], Future[MessageLite]] {
          override def apply(message: runtime.Stream[MessageLite]): Future[MessageLite] = rpcContext.method.invoke(serviceImpl, message).asInstanceOf[Future[MessageLite]]
        }
        dispatcher = new ServerDispatcher.Rpc.StreamToUnary[MessageLite, MessageLite](uri, func, reqCodec, repCodec)
      case MethodType.SERVER_STREAMING =>
        val func = new Function[MessageLite, runtime.Stream[MessageLite]] {
          override def apply(message: MessageLite): runtime.Stream[MessageLite] = rpcContext.method.invoke(serviceImpl, message).asInstanceOf[runtime.Stream[MessageLite]]
        }
        dispatcher = new ServerDispatcher.Rpc.UnaryToStream[MessageLite, MessageLite](uri, func, reqCodec, repCodec)
      case MethodType.BIDI_STREAMING =>
        val func = new Function[runtime.Stream[MessageLite], runtime.Stream[MessageLite]] {
          override def apply(message: runtime.Stream[MessageLite]): runtime.Stream[MessageLite] = rpcContext.method.invoke(serviceImpl, message).asInstanceOf[runtime.Stream[MessageLite]]
        }
        dispatcher = new ServerDispatcher.Rpc.StreamToStream[MessageLite, MessageLite](uri, func, reqCodec, repCodec)
      case _ =>
    }
    dispatcher
  }

  private def buildUnaryHandlerFunction(rpcContext: RpcContext, serviceImpl: AnyRef): Function[MessageLite, Future[MessageLite]] = {
      (message: MessageLite) => {
        var argObject: AnyRef = message
        val paramType = rpcContext.method.getParameterTypes()(0)
        if (classOf[RpcRequest[_]].isAssignableFrom(paramType)) {
          argObject = RequestBuilder.newBuilder(message.asInstanceOf[GeneralRpcRequest], rpcContext.requestBodyProtoType).build()
        }
        val returnType = rpcContext.method.getReturnType
        if (classOf[RpcResponse[_]].isAssignableFrom(returnType)) {
          FuturePool.unboundedPool {
            val response = rpcContext.method.invoke(serviceImpl, argObject).asInstanceOf[RpcResponse[MessageLite]]
            response.getResponse
          }
        } else if (classOf[Future[MessageLite]].isAssignableFrom(returnType)) {
          rpcContext.method.invoke(serviceImpl, argObject).asInstanceOf[Future[MessageLite]]
        } else if (classOf[MessageLite].isAssignableFrom(returnType)) {
          FuturePool.unboundedPool { rpcContext.method.invoke(serviceImpl, argObject).asInstanceOf[MessageLite]  }
        } else {
          Future.exception(new IllegalArgumentException(s"Un-support message type '${message.getClass.getName}'"))
        }
      }
  }
}
