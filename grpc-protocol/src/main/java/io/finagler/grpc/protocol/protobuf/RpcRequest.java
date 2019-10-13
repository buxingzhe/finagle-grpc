package io.finagler.grpc.protocol.protobuf;

import io.finagler.grpc.protocol.common.RpcHeaders;
import com.google.protobuf.MessageLite;

public interface RpcRequest<M extends MessageLite> extends RpcProtobufMessage<M>, RpcHeaders {

    MessageLite getRequest();

}
