package com.cs.ss.lib.rabbitmq.rpc.client;

import com.shawn.ss.lib.tools.CollectionBuilder;
import com.shawn.ss.lib.tools.L;
import com.shawn.ss.lib.tools.StringHelper;
import com.cs.ss.lib.rabbitmq.rpc.InvokeException;
import com.cs.ss.lib.rabbitmq.rpc.RabbitException;
import com.cs.ss.lib.rabbitmq.rpc.api.CallerInterface;
import com.cs.ss.lib.rabbitmq.rpc.api.SenderHandlerInterface;
import com.cs.ss.lib.rabbitmq.rpc.model.CallerInfo;
import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;
import com.cs.ss.lib.rabbitmq.rpc.utils.DataPackger;
import com.cs.ss.lib.rabbitmq.rpc.utils.DataUnpackager;
import com.cs.ss.lib.rabbitmq.rpc.utils.PackHelper;
import com.google.gson.Gson;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * Created by ss on 2016/5/27.
 */
class InvocationHandlerImpl<T extends CallerInterface> implements InvocationHandler {

    protected static Gson gson = new Gson();
    final Map<String, CallerInfo> callers;
    //    final Map<String, DataPackger> dataPackgerMap;
//    final Map<String, DataUnpackager> dataUnpackgerMap;
    final String mClName;
    final TaskSenderFactory senderControler;
    final SenderHandlerInterface impl;

    public InvocationHandlerImpl(Class<T> tClass, SenderHandlerInterface impl, TaskSenderFactory factory) {
        callers = CollectionBuilder.newHashMap();
//        dataPackgerMap = CollectionBuilder.newHashMap();
//        dataUnpackgerMap = CollectionBuilder.newHashMap();
        mClName = tClass.getName();
        senderControler = factory;
        this.impl = impl;
        PackHelper.init(tClass, null, callers, true);
        initQueue();
    }

    private void initQueue() {
        Set<String> keySet = callers.keySet();
        for (String s : keySet) {
            CallerInfo val = callers.get(s);
            senderControler.addCaller(impl, s, val.isAsync());
        }
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        String qName = StringHelper.concat(mClName, "_", name);
//        Class<?> returnType = method.getReturnType();
        if (callers.containsKey(qName)) {
            CallerInfo callerInfo = callers.get(qName);
            DataPackger dataPackger = callerInfo.getPacker();
            Boolean asyn = callerInfo.isAsync();
            DataUnpackager packger = callerInfo.getUnpacker();
            RpcCallBaseModel model = dataPackger.packParam(args);
            RpcCallBaseModel call = null;
            try {
                call = senderControler.call(model, qName, asyn);
            } catch (RabbitException ex) {
                ex.printStackTrace();
                throw InvokeException.newException("调用失败:" + ex.getMessage());
            }
            if (!asyn) {
                L.w("remote returned :", gson.toJson(call));
                if (call.getStatus() == RpcCallBaseModel.SUCCESS) {
                    Object o = packger.unpackParam(call, 0);
                    return o;
                } else if (call.getStatus() == RpcCallBaseModel.BAD_REQUEST) {
                    throw InvokeException.newException("参数有误" + call.getMsg());
                } else if (call.getStatus() == RpcCallBaseModel.EXCEPTION) {
                    throw InvokeException.newException("调用失败" + call.getMsg());
                } else {
                    throw InvokeException.newException("调用失败,未知错误" + call.getMsg());
                }
            } else {
                return null;
            }
        } else {
            throw InvokeException.newException("未知方法");
        }
    }
}
