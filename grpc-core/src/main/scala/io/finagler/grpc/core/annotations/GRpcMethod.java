package io.finagler.grpc.core.annotations;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GRpcMethod {

    String name() default "";

    String path() default "";

}
