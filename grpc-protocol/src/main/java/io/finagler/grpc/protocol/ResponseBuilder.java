package io.finagler.grpc.protocol;

import io.finagler.grpc.protocol.protobuf.RpcResponse;
import io.finagler.protobuf.dto.common.ErrorDTO;
import io.finagler.protobuf.dto.common.GeneralRpcResponse;
import com.google.protobuf.Message;

import java.util.Map;

public final class ResponseBuilder<M extends Message> {

    private final Response<M> response;

    private ResponseBuilder() {
        this.response = new Response<>();
    }

    private ResponseBuilder(GeneralRpcResponse response, Message bodyProtoType) {
        this.response = new Response<>(response, bodyProtoType);
    }

    public static <T extends Message> ResponseBuilder<T> newBuilder() {
        return new ResponseBuilder<>();
    }

    public static <T extends Message> ResponseBuilder<T> newBuilder(GeneralRpcResponse response, Message bodyProtoType) {
        return new ResponseBuilder<>(response, bodyProtoType);
    }

    public ResponseBuilder<M> withHeaders(Map<String, String> headers) {
        this.response.addHeaders(headers);
        return this;
    }

    public ResponseBuilder<M> withHeader(String headerKey, String headerValue) {
        this.response.addHeader(headerKey, headerValue);
        return this;
    }

    public ResponseBuilder<M> withBody(M body) {
        response.setBody(body);
        return this;
    }

    public ResponseBuilder<M> withError(ErrorDTO error) {
        response.setError(error);
        return this;
    }

    public ResponseBuilder<M> withElapsedTimes(Map<String, Integer> elapsedTimes) {
        response.setElapsedTimes(elapsedTimes);
        return this;
    }

    public RpcResponse<M> build() {
        return this.response.build();
    }

}
