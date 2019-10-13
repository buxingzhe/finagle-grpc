package io.finagler.grpc.protocol.common;

public interface RpcHeaders {

    String REQUEST_ID = "D-Rpc-RequestId";

    String SOURCE = "D-Rpc-Source";

    String DESTINATION = "D-Rpc-Destination";

    String URI = "D-Rpc-Uri";

    String TIMEOUT = "D-Rpc-Timeout";

    String RESPONSE_TIME = "D-Rpc-ReplyTime";

    String getRequestId();

    String getSource();

    String getDestination();

    String getUri();

    int getRequestTimeout();

    long getResponseTime();

}
