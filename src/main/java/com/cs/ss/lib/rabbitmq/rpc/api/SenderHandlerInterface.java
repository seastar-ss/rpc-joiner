package com.cs.ss.lib.rabbitmq.rpc.api;

import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;

/**
 * Created by ss on 2016/5/27.
 */
public interface SenderHandlerInterface {
    public void handle(RpcCallBaseModel model);



    public void postHandle();
}
