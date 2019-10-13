package io.grpc.examples.helloworld.service;

import io.finagler.grpc.core.exception.GRpcRemoteException;
import io.finagler.grpc.protocol.protobuf.RpcRequest;
import io.finagler.grpc.protocol.protobuf.RpcResponse;
import io.finagler.grpc.protocol.ResponseBuilder;
import io.finagler.grpc.core.runtime.H2GrpcStatus;
import com.twitter.util.Future;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;

public class HelloWorldImpl implements HelloWorldInterface {

    @SuppressWarnings("unchecked")
    public Future<HelloReply> asyncSayHello(HelloRequest request) {
        if (request.getName() == null || request.getName().isEmpty()) {
            return Future.exception(new H2GrpcStatus.InvalidArgument("name is required"));
        }
        String message = "Hello " + request.getName();
        return Future.value(HelloReply.newBuilder().setMessage(message).build());
    }

    public HelloReply sayHello(HelloRequest request) throws GRpcRemoteException {
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new GRpcRemoteException(new H2GrpcStatus.InvalidArgument("name is required"));
        }
        String message = "Hello " + request.getName();
        return HelloReply.newBuilder().setMessage(message).build();
    }

    @Override
    public RpcResponse<HelloReply> greet(RpcRequest<HelloRequest> request) {
        String message = "Hello " + request.getBody().getName();
        return ResponseBuilder.<HelloReply>newBuilder().withBody(HelloReply.newBuilder().setMessage(message).build()).build();
    }
}
