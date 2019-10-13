package io.grpc.examples.helloworld;

import io.finagler.grpc.core.client.ClientOptions;
import io.finagler.grpc.core.client.RemoteServiceClient;
import io.finagler.grpc.core.exception.GRpcRemoteException;
import io.finagler.grpc.protocol.protobuf.RpcRequest;
import io.finagler.grpc.protocol.protobuf.RpcResponse;
import io.finagler.grpc.protocol.RequestBuilder;
import io.finagler.grpc.core.runtime.H2GrpcStatus;
import io.finagler.grpc.core.server.ServerBuilder;
import io.finagler.grpc.core.server.ServerOptions;
import io.finagler.grpc.core.server.ServiceHandlerRegistry;
import com.twitter.util.Await;
import com.twitter.util.Future;
import io.grpc.examples.helloworld.server.HelloWorldServer;
import io.grpc.examples.helloworld.service.HelloWorldImpl;
import io.grpc.examples.helloworld.service.HelloWorldInterface;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HelloWorldTest {
    private static HelloWorldServer server = new HelloWorldServer();
    private static RemoteServiceClient client = new RemoteServiceClient(new ClientOptions("localhost:9888"));

    @BeforeClass
    public static void startupServer() {
        ServiceHandlerRegistry serviceHandlerRegistry = new ServiceHandlerRegistry()
                .addServiceHandler(new HelloWorldImpl(), new Class[] { HelloWorldInterface.class });
        ServerOptions serverOptions = new ServerOptions(":9888");
        ServerBuilder.newBuilder().withDaemon(false).withHandlerRegistry(serviceHandlerRegistry).withOptions(serverOptions).startServer();
    }

    @AfterClass
    public static void shutdown() {
        client.close();
        server.shutdown();
    }

    @Test
    public void testNormalCall() throws Exception {
        HelloWorldInterface helloClient = client.getService(HelloWorldInterface.class);
        HelloRequest request = HelloRequest.newBuilder().setName("world!").build();
        HelloReply res = helloClient.sayHello(request);
        System.out.println("Sync call response: " + res.getMessage());

        Future<HelloReply> helloRespFuture = helloClient.asyncSayHello(request);
        res = Await.result(helloRespFuture);
        System.out.println("Async call response: " + res.getMessage());
    }

    @Test
    public void testGeneralCall() {
        HelloWorldInterface helloClient = client.getService(HelloWorldInterface.class);
        RpcRequest<HelloRequest> helloRequest = RequestBuilder.<HelloRequest>newBuilder()
                .withHeader("test", "test-head-value")
                .withBody(HelloRequest.newBuilder().setName("world!").build()).build();
        RpcResponse<HelloReply> greetReply = helloClient.greet(helloRequest);
        System.out.println(greetReply.getBody().getMessage());
    }

    @Test(expected = GRpcRemoteException.class)
    public void testExceptionCall() throws Exception {
        HelloWorldInterface helloClient = client.getService(HelloWorldInterface.class);
        HelloRequest request = HelloRequest.newBuilder().build();
        HelloReply res = helloClient.sayHello(request);
        System.out.println("Sync call response: " + res.getMessage());
    }

    @Test(expected = H2GrpcStatus.InvalidArgument.class)
    public void testFutureExceptionCall() throws Exception {
        HelloWorldInterface helloClient = client.getService(HelloWorldInterface.class);
        HelloRequest request = HelloRequest.newBuilder().build();
        Future<HelloReply> helloRespFuture = helloClient.asyncSayHello(request);
        helloRespFuture.toJavaFuture().get();
    }
}
