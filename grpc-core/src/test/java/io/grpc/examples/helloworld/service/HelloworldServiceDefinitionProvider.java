package io.grpc.examples.helloworld.service;

import io.finagler.grpc.core.service.ServiceDefinition;
import io.finagler.grpc.core.service.ServiceDefinitionProvider;
import io.grpc.examples.helloworld.GreeterGrpc;

import java.util.ArrayList;
import java.util.List;

public class HelloworldServiceDefinitionProvider implements ServiceDefinitionProvider {

    @Override
    public List<ServiceDefinition> getServiceDefinitions() {
        List<ServiceDefinition> result = new ArrayList<>();
        result.add(new ServiceDefinition("greeter", HelloWorldInterface.class, GreeterGrpc.getServiceDescriptor()));
        return result;
    }

}
