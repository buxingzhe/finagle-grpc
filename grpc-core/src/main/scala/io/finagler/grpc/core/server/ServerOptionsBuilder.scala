package io.finagler.grpc.core.server

import java.io.File

import com.twitter.conversions.time._
import com.twitter.conversions.storage._
import com.twitter.util.{Duration, StorageUnit}

object ServerOptionsBuilder {
  def newOptions(endpoint: String): ServerOptions = {
    new ServerOptions(endpoint)
  }
}

class ServerOptions(endpoint: String) {
  private var label: String = _
  private var keyFilePath: String = _
  private var certFilePath: String = _
  private var maxSessionIdleTimeInMinutes: Int = 10
  private var sendBufferSizeInMegabytes: Int = 50
  private var receiveBufferSizeInMegabytes: Int = 50
  private var requestTimeoutInSeconds: Int = 120

  def withLabel(label: String): ServerOptions = {
    this.label = label
    this
  }

  def withRequestTimeout(requestTimeoutInSeconds: Int): ServerOptions = {
    this.requestTimeoutInSeconds = requestTimeoutInSeconds
    this
  }

  def withKeyFile(keyFilePath: String): ServerOptions = {
    this.keyFilePath = keyFilePath
    this
  }

  def withCertFile(certFilePath: String): ServerOptions = {
    this.certFilePath = certFilePath
    this
  }

  def withMaxSessionIdleTime(maxSessionIdleTimeInMinutes: Int): ServerOptions = {
    this.maxSessionIdleTimeInMinutes = maxSessionIdleTimeInMinutes
    this
  }

  def withSendBufferSize(sendBufferSizeInMegabytes: Int): ServerOptions = {
    this.sendBufferSizeInMegabytes = sendBufferSizeInMegabytes
    this
  }

  def withReceiveBufferSizeInMegabytes(receiveBufferSizeInMegabytes: Int): ServerOptions = {
    this.receiveBufferSizeInMegabytes = receiveBufferSizeInMegabytes
    this
  }

  def getLabel: String = label

  def getEndpoint: String = endpoint

  def getRequestTimeout: Duration = requestTimeoutInSeconds.seconds

  def getMaxSessionIdleTime: Duration = maxSessionIdleTimeInMinutes.minutes

  def getCertFile: File = if (certFilePath != null) new File(certFilePath) else null

  def getKeyFile: File = if (keyFilePath != null) new File(keyFilePath) else null

  def getSendBufferSize: StorageUnit = sendBufferSizeInMegabytes.megabytes

  def getReceiveBufferSize: StorageUnit = receiveBufferSizeInMegabytes.megabytes

}
