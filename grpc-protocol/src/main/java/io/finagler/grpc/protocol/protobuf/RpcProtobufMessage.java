package io.finagler.grpc.protocol.protobuf;

import io.finagler.grpc.protocol.common.RequestHeaderAware;
import io.finagler.grpc.protocol.common.RequestHeaderExtractor;
import com.google.protobuf.MessageLite;

public interface RpcProtobufMessage<M extends MessageLite> extends RequestHeaderAware, RequestHeaderExtractor {
    M getBody();
}
