package com.cs.ss.lib.rabbitmq.rpc.api.server_annotaions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ss on 2016/7/7.
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServerImpl {
    public String[] impl() default {""};
}
