package io.finagler.grpc.core.client

import io.finagler.grpc.protocol.protobuf.RpcRequest
import io.finagler.grpc.core.runtime.ClientDispatcher.Rpc.{StreamToStream, StreamToUnary, UnaryToStream, UnaryToUnary}
import com.google.protobuf.MessageLite
import com.twitter.finagle.Service
import com.twitter.finagle.buoyant.h2.{Request, Response}
import io.finagler.grpc.core.common.RpcContext
import io.finagler.grpc.core.exception.GRpcRemoteException
import io.finagler.grpc.core.runtime
import io.grpc.MethodDescriptor.MethodType

object ClientInvoker {

  def invoke(rpcContext: RpcContext, h2ClientService: Service[Request, Response], args: Array[AnyRef]): AnyRef = {
    try {
      var argObject: AnyRef = args(0)
      rpcContext.methodDescriptor.getType match {
        case MethodType.UNARY =>
          val paramType = rpcContext.method.getParameterTypes()(0)
          if (classOf[RpcRequest[_]].isAssignableFrom(paramType)) {
            argObject = argObject.asInstanceOf[RpcRequest[MessageLite]].getRequest
          }
          val clientDispatcher = UnaryToUnary(h2ClientService, rpcContext.url, rpcContext.requestCodec, rpcContext.responseCodec)
          clientDispatcher(argObject.asInstanceOf[MessageLite])
        case MethodType.SERVER_STREAMING =>
          val clientDispatcher = UnaryToStream(h2ClientService, rpcContext.url, rpcContext.requestCodec, rpcContext.responseCodec)
          clientDispatcher(argObject.asInstanceOf[MessageLite])
        case MethodType.CLIENT_STREAMING =>
          val clientDispatcher = StreamToUnary(h2ClientService, rpcContext.url, rpcContext.requestCodec, rpcContext.responseCodec)
          clientDispatcher(argObject.asInstanceOf[runtime.Stream[MessageLite]])
        case MethodType.BIDI_STREAMING =>
          val clientDispatcher = StreamToStream(h2ClientService, rpcContext.url, rpcContext.requestCodec, rpcContext.responseCodec)
          clientDispatcher(argObject.asInstanceOf[runtime.Stream[MessageLite]])
        case _ => null
      }
    } catch {
      case th: Throwable => throw new GRpcRemoteException(th)
    }
  }

}
