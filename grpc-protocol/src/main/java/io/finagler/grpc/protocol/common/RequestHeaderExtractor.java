package io.finagler.grpc.protocol.common;

import java.util.Map;

public interface RequestHeaderExtractor {

    String getHeader(String headerKey);

    Map<String, String> getHeaders();

}
