package io.finagler.grpc.protocol.common;

import java.util.Map;

public interface RequestHeaderAware {

    void addHeader(String key, String value);

    void addHeaders(Map<String, String> headers);

}
