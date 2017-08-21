package com.cs.ss.lib.rabbitmq.rpc.utils;

import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;

/**
 * Created by ss on 2016/6/5.
 */
public interface DataUnpackager {
    Object unpackParam(RpcCallBaseModel model, int tClass);

    Object[] uppackAllParam(RpcCallBaseModel model);

    int getParamCount();
}
