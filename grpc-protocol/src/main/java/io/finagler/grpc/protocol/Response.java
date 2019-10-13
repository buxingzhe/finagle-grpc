package io.finagler.grpc.protocol;

import io.finagler.grpc.protocol.common.Recyclable;
import io.finagler.grpc.protocol.protobuf.RpcResponse;
import io.finagler.protobuf.dto.common.ErrorDTO;
import io.finagler.protobuf.dto.common.GeneralRpcResponse;
import com.google.protobuf.Any;
import com.google.protobuf.Message;

import java.util.Map;

public class Response<M extends Message> implements RpcResponse<M>, Recyclable {
    private GeneralRpcResponse.Builder responseBuilder;
    private Message bodyProtoType;
    private M body;

    public Response() {
        this.responseBuilder = GeneralRpcResponse.newBuilder();
    }

    Response(GeneralRpcResponse response, Message bodyProtoType) {
        this.responseBuilder = response.toBuilder();
        this.bodyProtoType = bodyProtoType;
    }

    public void setError(ErrorDTO error) {
        this.responseBuilder.setError(error);
    }

    @Override
    public ErrorDTO getError() {
        return responseBuilder.getError();
    }

    @Override
    public boolean hasError() {
        return responseBuilder.hasError();
    }

    @SuppressWarnings("unchecked")
    public void setBody(M body) {
        this.body = body;
        this.bodyProtoType = body.getDefaultInstanceForType();
    }

    @Override
    public M getBody() {
        if (this.body == null) this.body = getBodyFromBuilder();
        return this.body;
    }

    public void setElapsedTime(String operation, Integer elapsedTime) {
        this.responseBuilder.putElapsedTime(operation, elapsedTime);
    }

    public void setElapsedTimes(Map<String, Integer> elapsedTimes) {
        this.responseBuilder.putAllElapsedTime(elapsedTimes);
    }

    @Override
    public Message getResponse() {
        return this.responseBuilder.build();
    }

    @Override
    public Map<String, Integer> getElapsedTimes() {
        return responseBuilder.getElapsedTimeMap();
    }

    @Override
    public void addHeader(String key, String value) {
        responseBuilder.putHeader(key, value);
    }

    public void addHeaders(Map<String, String> headers) {
        responseBuilder.putAllHeader(headers);
    }

    @Override
    public String getHeader(String headerKey) {
        return responseBuilder.getHeaderMap().get(headerKey);
    }

    @Override
    public Map<String, String> getHeaders() {
        return responseBuilder.getHeaderMap();
    }

    public Response<M> build() {
        if (responseBuilder.hasBody()) {
            if (this.body == null) this.body = getBodyFromBuilder();
            responseBuilder.clearBody();
        } else {
            if (this.body != null) responseBuilder.setBody(Any.newBuilder().setValue(body.toByteString()));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private M getBodyFromBuilder() {
        if (responseBuilder.hasBody()) {
            try {
                return (M) bodyProtoType.newBuilderForType().mergeFrom(responseBuilder.getBody().getValue()).build();
            } catch (Exception e) {
                // Ignored
            }
        }
        return null;
    }

    @Override
    public void recycle() {
        this.responseBuilder.clear();
        this.bodyProtoType = null;
        this.body = null;
    }
}
