package io.finagler.grpc.core.exception

class GRpcRemoteException(message: String, cause: Throwable) extends Exception(message, cause) {

  def this(cause: Throwable) = {
    this("", cause)
  }

  def this(message: String) = {
    this(message, null)
  }
}
