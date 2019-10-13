package io.finagler.grpc.core.client

import java.util.concurrent.ConcurrentHashMap

import com.twitter.finagle.Service
import com.twitter.finagle.buoyant.h2.{Request, Response}
import io.finagler.grpc.core.service.{ServiceConsumer, ServiceRegistry}

class RemoteServiceClient(clientOptions: ClientOptions) extends ServiceConsumer {

  private val cachedProxy = new ConcurrentHashMap[Class[_], Any]()
  private val h2Client: Service[Request, Response] = H2ClientBuilder.build(clientOptions)
  
  @SuppressWarnings(Array("unchecked"))
  def getService[T](svcInterfaceClz: Class[T]): T = {
    val result = cachedProxy.get(svcInterfaceClz)
    if (result != null) result.asInstanceOf[T] else getServiceProxy(svcInterfaceClz)
  }

  def close(): Unit = {
    cachedProxy.clear()
    if (h2Client != null) h2Client.close()
  }

  private def getServiceProxy[T](svcInterfaceClz: Class[T]) = {
    val interfaces: Array[Class[_]] = Array(svcInterfaceClz)
    val serviceDefinition = ServiceRegistry.getServiceDefinition(svcInterfaceClz)
    if (serviceDefinition == null) throw new NullPointerException(s"service '${svcInterfaceClz.getName}' not found.")
    val proxy = java.lang.reflect.Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader, interfaces, new GRpcClientStubProxy(serviceDefinition, this))
    cachedProxy.putIfAbsent(svcInterfaceClz, proxy)
    proxy.asInstanceOf[T]
  }

  override def isAsync: Boolean = false

  override def isStream: Boolean = false

  override def getClientOptions: ClientOptions = clientOptions

  override def getH2ClientService: Service[Request, Response] = h2Client
}
