package com.cs.ss.lib.rabbitmq.rpc;

/**
 * Created by ss on 2016/5/25.
 */
public class RabbitException extends RuntimeException {
    public RabbitException(String message) {
        super(message);
    }

    public static final RabbitException newException(String msg) {
        return new RabbitException(msg);
    }
}
