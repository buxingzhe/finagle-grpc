package io.finagler.grpc.core.client

import java.lang.reflect.{InvocationHandler, Method}

import com.google.protobuf.Message
import com.twitter.util.{Await, Future}
import io.finagler.grpc.core.common.RpcContext
import io.finagler.grpc.core.exception.GRpcRemoteException
import io.finagler.grpc.core.service.{ServiceConsumer, ServiceDefinition}
import io.finagler.grpc.protocol.ResponseBuilder
import io.finagler.grpc.protocol.protobuf.RpcResponse
import io.finagler.protobuf.dto.common.GeneralRpcResponse
import io.grpc.MethodDescriptor.MethodType

class GRpcClientStubProxy(serviceDefinition: ServiceDefinition, serviceConsumer: ServiceConsumer) extends InvocationHandler {

  @SuppressWarnings(Array("unchecked"))
  @throws[Throwable]
  override def invoke(proxy: Any, method: Method, args: Array[AnyRef]): AnyRef = {
    val rpcContext: RpcContext = RpcContext.getRpcContext(serviceDefinition, method)
    val response = ClientInvoker.invoke(rpcContext, serviceConsumer.getH2ClientService, args)

    rpcContext.methodDescriptor.getType match {
      case MethodType.UNARY =>
        if (classOf[RpcResponse[Message]].isAssignableFrom(method.getReturnType)) {
          try {
            val responseFuture: Future[Message] = response.asInstanceOf[Future[Message]]
            val reply = Await.result[Message](responseFuture, serviceConsumer.getClientOptions.getRequestTimeout)
            ResponseBuilder.newBuilder(reply.asInstanceOf[GeneralRpcResponse], rpcContext.responseBodyProtoType).build()
          } catch {
            case e: Throwable => throw new GRpcRemoteException(e)
          }
        } else if (classOf[Message].isAssignableFrom(method.getReturnType)) {
          try {
            val responseFuture: Future[AnyRef] = response.asInstanceOf[Future[AnyRef]]
            Await.result[AnyRef](responseFuture, serviceConsumer.getClientOptions.getRequestTimeout)
          } catch {
            case e: Throwable => throw new GRpcRemoteException(e)
          }
        } else if (classOf[Future[AnyRef]].isAssignableFrom(method.getReturnType)) {
          response
        } else {
          throw new IllegalStateException(s"Unsupported UNARY return type: ${method.getReturnType.getName}")
        }
      case methodType: MethodType => throw new UnsupportedOperationException(s"Unsupported method type: $methodType")
    }
  }
}
