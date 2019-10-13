package io.finagler.grpc.core.utils

import com.google.protobuf.{Any, MessageLite}
import io.finagler.grpc.core.exception.ErrorCodes
import io.finagler.grpc.core.service.ServiceName
import io.finagler.protobuf.dto.common.{ErrorDTO, Header, RequestMessage, ResponseMessage}

object MessageUtils {

  def newRequest(header: Header, body: MessageLite): RequestMessage = {
    RequestMessage.newBuilder().setHeader(header).setBody(toBytes(body)).build()
  }

  def newSuccessResponse(body: MessageLite): ResponseMessage = {
    ResponseMessage.newBuilder().setBody(toBytes(body)).build()
  }

  def newFaultResponse(error: AnyRef): ResponseMessage = {
    error match {
      case errorMsg: String => ResponseMessage.newBuilder().setError(newError(errorMsg)).build()
      case exception: Throwable => ResponseMessage.newBuilder().setError(newError(exception)).build()
      case errorObj: AnyRef => ResponseMessage.newBuilder().setError(newError(errorObj.toString)).build()
    }
  }

  def newError(error: Throwable): ErrorDTO = {
    ErrorDTO.newBuilder().setCode(ErrorCodes.system).setSource(ServiceName.shortName).setMessage(error.getMessage).build()
  }

  def newError(errorMessage: String): ErrorDTO = {
    ErrorDTO.newBuilder().setCode(ErrorCodes.system).setSource(ServiceName.shortName).setMessage(errorMessage).build()
  }

  def body[T <: MessageLite](requestMessage: RequestMessage, messageProtoType: T): T = {
    deserialize(requestMessage.getBody, messageProtoType)
  }

  def body[T <: MessageLite](responseMessage: ResponseMessage, messageProtoType: T): T = {
    deserialize(responseMessage.getBody, messageProtoType)
  }

  def toBytes(message: MessageLite): Any = {
    Any.newBuilder().setTypeUrl(message.getClass.getName).setValue(message.toByteString).build()
  }

  def deserialize[T <: MessageLite](anyMessage: Any, messageProtoType: T): T = {
    messageProtoType.newBuilderForType().mergeFrom(anyMessage.getValue).build().asInstanceOf[T]
  }
}
