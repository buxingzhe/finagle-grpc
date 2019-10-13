package io.grpc.examples.helloworld.service;

import io.finagler.grpc.core.annotations.GRpcMethod;
import io.finagler.grpc.core.exception.GRpcRemoteException;
import io.finagler.grpc.protocol.protobuf.RpcRequest;
import io.finagler.grpc.protocol.protobuf.RpcResponse;
import com.twitter.util.Future;
import io.grpc.examples.helloworld.HelloReply;
import io.grpc.examples.helloworld.HelloRequest;


public interface HelloWorldInterface {

    @GRpcMethod(name = "sayHello")
    Future<HelloReply> asyncSayHello(HelloRequest request);


    HelloReply sayHello(HelloRequest request) throws GRpcRemoteException;

    @GRpcMethod(name = "sayHello")
    RpcResponse<HelloReply> greet(RpcRequest<HelloRequest> request);

}
