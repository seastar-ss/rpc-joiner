package com.cs.ss.lib.rabbitmq.rpc.test;

import org.springframework.stereotype.Service;

import java.lang.annotation.*;

/**
 * Created by ss on 2016/7/4.
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Findable {
    String name();
}
