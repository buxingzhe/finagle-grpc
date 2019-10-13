package io.finagler.grpc.core.client

import java.io.File

import com.twitter.conversions.time._
import com.twitter.conversions.storage._
import com.twitter.util.{Duration, StorageUnit}

object ClientOptionsBuilder {
  def newOptions(hosts: String): ClientOptions = {
    new ClientOptions(hosts)
  }
}

class ClientOptions(hosts: String) {
  private var label: String = _
  private var keyFilePath: String = _
  private var certFilePath: String = _
  private var minPoolSize: Int = 1
  private var maxPoolSize: Int = 10
  private var requestTimeoutInSeconds: Int = 120
  private var maxSessionIdleTimeInMinutes: Int = 10
  private var sendBufferSizeInMegabytes: Int = 0
  private var receiveBufferSizeInMegabytes: Int = 0

  def withLabel(label: String): ClientOptions = {
    this.label = label
    this
  }

  def withMinPoolSize(minPoolSize: Int): ClientOptions = {
    this.minPoolSize = maxPoolSize
    this
  }

  def withMaxPoolSize(maxPoolSize: Int): ClientOptions = {
    this.maxPoolSize = maxPoolSize
    this
  }

  def withRequestTimeout(requestTimeoutInSeconds: Int): ClientOptions = {
    this.requestTimeoutInSeconds = requestTimeoutInSeconds
    this
  }

  def withKeyFile(keyFilePath: String): ClientOptions = {
    this.keyFilePath = keyFilePath
    this
  }

  def withCertFile(certFilePath: String): ClientOptions = {
    this.certFilePath = certFilePath
    this
  }

  def withMaxSessionIdleTime(maxSessionIdleTimeInMinutes: Int): ClientOptions = {
    this.maxSessionIdleTimeInMinutes = maxSessionIdleTimeInMinutes
    this
  }

  def withSendBufferSize(sendBufferSizeInMegabytes: Int): ClientOptions = {
    this.sendBufferSizeInMegabytes = sendBufferSizeInMegabytes
    this
  }

  def withReceiveBufferSizeInMegabytes(receiveBufferSizeInMegabytes: Int): ClientOptions = {
    this.receiveBufferSizeInMegabytes = receiveBufferSizeInMegabytes
    this
  }

  def getLabel: String = label

  def getHosts: String = hosts

  def getMinPoolSize: Int = minPoolSize

  def getMaxPoolSize: Int = maxPoolSize

  def getRequestTimeout: Duration = requestTimeoutInSeconds.seconds

  def getMaxSessionIdleTime: Duration = maxSessionIdleTimeInMinutes.minutes

  def getCertFile: File = if (certFilePath != null) new File(certFilePath) else null

  def getKeyFile: File = if (keyFilePath != null) new File(keyFilePath) else null

  def getSendBufferSize: StorageUnit = sendBufferSizeInMegabytes.megabytes

  def getReceiveBufferSize: StorageUnit = receiveBufferSizeInMegabytes.megabytes
}
