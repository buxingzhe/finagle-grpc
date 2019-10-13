package io.grpc.examples.helloworld.server;

import io.finagler.grpc.core.server.GRpcServer;
import io.finagler.grpc.core.server.ServerOptions;
import io.finagler.grpc.core.server.ServiceHandlerRegistry;
import io.grpc.examples.helloworld.service.HelloWorldImpl;
import io.grpc.examples.helloworld.service.HelloWorldInterface;

public class HelloWorldServer {

    private GRpcServer server = null;

    public static void main(String[] args) {
        new HelloWorldServer().startup(":9888");
    }

    public void startup(String endpoint) {
        ServiceHandlerRegistry serviceHandlerRegistry = new ServiceHandlerRegistry()
                .addServiceHandler(new HelloWorldImpl(), new Class[] { HelloWorldInterface.class });

        ServerOptions serverOptions = new ServerOptions(endpoint);
        server = new GRpcServer(serverOptions, serviceHandlerRegistry);
        server.start();
    }

    public void shutdown() {
        if (server!= null) server.stop();
    }
}
