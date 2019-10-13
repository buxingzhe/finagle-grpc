package io.finagler.grpc.protocol;

import io.finagler.grpc.protocol.common.RpcHeaders;
import io.finagler.grpc.protocol.protobuf.RpcRequest;
import io.finagler.protobuf.dto.common.GeneralRpcRequest;
import io.finagler.protobuf.dto.common.Header;
import com.google.protobuf.Message;

public final class RequestBuilder<M extends Message> {

    private final Request<M> request;

    private RequestBuilder() {
         this.request = new Request<>();
    }

    private RequestBuilder(GeneralRpcRequest request, Message bodyProtoType) {
        this.request = new Request<>(request, bodyProtoType);
    }

    public static <T extends Message> RequestBuilder<T> newBuilder() {
        return new RequestBuilder<>();
    }

    public static <T extends Message> RequestBuilder<T> newBuilder(GeneralRpcRequest request, Message bodyProtoType) {
        return new RequestBuilder<>(request, bodyProtoType);
    }

    public RequestBuilder<M> withHeader(Header header) {
        this.request.addHeader(RpcHeaders.REQUEST_ID, header.getTaskId());
        this.request.addHeader(RpcHeaders.URI, header.getUri());
        this.request.addHeader(RpcHeaders.SOURCE, header.getSource());
        this.request.addHeader(RpcHeaders.DESTINATION, header.getDestination());
        this.request.addHeader(RpcHeaders.TIMEOUT, String.valueOf(header.getTimeout()));
        return this;
    }

    public RequestBuilder<M> withHeader(String headerKey, String headerValue) {
        this.request.addHeader(headerKey, headerValue);
        return this;
    }

    public RequestBuilder<M> withBody(M body) {
        this.request.setBody(body);
        return this;
    }

    public RpcRequest<M> build() {
        return this.request.build();
    }

}
