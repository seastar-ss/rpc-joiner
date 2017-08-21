package com.cs.ss.lib.rabbitmq.rpc.api;

/**
 * Created by ss on 2016/5/26.
 */
public interface Cipher {
    public String encrypt(String resource);

    public String decrypt(String resource);
}
