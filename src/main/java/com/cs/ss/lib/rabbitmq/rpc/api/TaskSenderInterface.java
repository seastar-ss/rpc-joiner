package com.cs.ss.lib.rabbitmq.rpc.api;

//import com.cs.ss.lib.rabbitmq.rpc.client.TaskSenderImpl;
import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;

public interface TaskSenderInterface {
    SenderHandlerInterface getImpl();

    TaskSenderInterface setImpl(SenderHandlerInterface impl);

    RpcCallBaseModel send(String calledQueueName, RpcCallBaseModel model);

    void close();
}
