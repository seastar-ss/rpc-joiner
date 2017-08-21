package com.cs.ss.lib.rabbitmq.rpc.api;

import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;

/**
 * Created by ss on 2016/5/27.
 */
public interface HandlerInterface {
    RpcCallBaseModel handle(RpcCallBaseModel model);

    void preHandle(RpcCallBaseModel model);

    void postHandle();

    RpcCallBaseModel handleException(Exception ex);


    void stop();
}
