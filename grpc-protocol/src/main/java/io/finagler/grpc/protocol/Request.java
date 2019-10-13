package io.finagler.grpc.protocol;

import io.finagler.grpc.protocol.common.Recyclable;
import io.finagler.grpc.protocol.protobuf.RpcRequest;
import io.finagler.protobuf.dto.common.GeneralRpcRequest;
import com.google.protobuf.Any;
import com.google.protobuf.Message;

import java.util.Map;

public class Request<M extends Message> implements RpcRequest<M>, Recyclable {
    private GeneralRpcRequest.Builder requestBuilder;
    private Message bodyProtoType;
    private M body;

    public Request() {
        this.requestBuilder = GeneralRpcRequest.newBuilder();
    }

    Request(GeneralRpcRequest request, Message bodyProtoType) {
        this.requestBuilder = request.toBuilder();
        this.bodyProtoType = bodyProtoType;
    }

    @SuppressWarnings("unchecked")
    public void setBody(M body) {
        this.body = body;
        this.bodyProtoType = this.body.getDefaultInstanceForType();
    }

    @Override
    public M getBody() {
        if (this.body == null) this.body = getBodyFromBuilder();
        return this.body;
    }

    @Override
    public void addHeader(String key, String value) {
        requestBuilder.putHeader(key, value);
    }

    @Override
    public void addHeaders(Map<String, String> headers) {
        requestBuilder.putAllHeader(headers);
    }

    @Override
    public String getHeader(String headerKey) {
        return requestBuilder.getHeaderMap().get(headerKey);
    }

    @Override
    public Map<String, String> getHeaders() {
        return requestBuilder.getHeaderMap();
    }

    @Override
    public String getRequestId() {
        return requestBuilder.getHeaderMap().get(REQUEST_ID);
    }

    @Override
    public String getSource() {
        return requestBuilder.getHeaderMap().get(SOURCE);
    }

    @Override
    public String getDestination() {
        return requestBuilder.getHeaderMap().get(DESTINATION);
    }

    @Override
    public String getUri() {
        return requestBuilder.getHeaderMap().get(URI);
    }

    @Override
    public int getRequestTimeout() {
        String timeOut = requestBuilder.getHeaderMap().get(TIMEOUT);
        try {
            return Integer.valueOf(timeOut);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public long getResponseTime() {
        String responseTime = requestBuilder.getHeaderMap().get(RESPONSE_TIME);
        try {
            return Long.valueOf(responseTime);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public Message getRequest() {
        return requestBuilder.build();
    }

    public Request<M> build() {
        if (requestBuilder.hasBody()) {
            if (this.body == null) this.body = getBodyFromBuilder();
            requestBuilder.clearBody();
        } else {
            if (this.body != null) requestBuilder.setBody(Any.newBuilder().setValue(body.toByteString()));
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    private M getBodyFromBuilder() {
        try {
            if (requestBuilder.hasBody()) return (M) this.bodyProtoType.newBuilderForType().mergeFrom(requestBuilder.getBody().getValue()).build();
        } catch (Exception ex) {
            // Ignored
        }
        return null;
    }

    @Override
    public void recycle() {
        this.requestBuilder.clear();
        this.bodyProtoType = null;
        this.body = null;
    }
}
