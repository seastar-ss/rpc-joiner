package com.cs.ss.lib.rabbitmq.rpc.utils;

import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;

/**
 * Created by ss on 2016/6/5.
 */
public interface DataPackger {
    RpcCallBaseModel packParam(Object... args);

    RpcCallBaseModel packObjParam(Object... args);

    int getArgCount();
}
