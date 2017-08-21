package com.cs.ss.lib.rabbitmq.rpc;

/**
 * Created by ss on 2016/6/6.
 */
public class InvokeException extends RuntimeException {
    public InvokeException(String message) {
        super(message);
    }

    public static InvokeException newException(String msg) {
        return new InvokeException(msg);
    }
}
