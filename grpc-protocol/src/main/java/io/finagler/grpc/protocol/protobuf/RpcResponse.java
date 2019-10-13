package io.finagler.grpc.protocol.protobuf;

import io.finagler.protobuf.dto.common.ErrorDTO;
import com.google.protobuf.MessageLite;

import java.util.Map;

public interface RpcResponse<M extends MessageLite> extends RpcProtobufMessage<M> {

    MessageLite getResponse();

    ErrorDTO getError();

    boolean hasError();

    Map<String, Integer> getElapsedTimes();

}
