package com.cs.ss.lib.rabbitmq.rpc.client;

import com.cs.ss.lib.rabbitmq.rpc.api.TaskSenderInterface;
import com.shawn.ss.lib.tools.CollectionBuilder;
import com.shawn.ss.lib.tools.StringHelper;
import com.cs.ss.lib.rabbitmq.rpc.api.SenderHandlerInterface;
import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;
import com.cs.ss.lib.rabbitmq.rpc.utils.ConnectionPool;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by ss on 2016/5/27.
 */
@Component
public class TaskSenderFactory implements InitializingBean {
//    Cipher cipher;
//
//    public Cipher getCipher() {
//        return cipher;
//    }
//
//    public TaskSenderFactory setCipher(Cipher cipher) {
//        this.cipher = cipher;
//        return this;
//    }

    @Autowired
    ScheduledExecutorService service;
    @Autowired
    ConnectionPool pool;

    private Map<String, TaskSenderInterface> senders;

    public TaskSenderFactory() {
    }


    void addCaller(SenderHandlerInterface impl, String methodName, boolean async) {
        String key = StringHelper.concat(methodName, "-", async);
        TaskSenderInterface sender = new TaskSenderImpl(pool, async);
        if (impl != null) {
            sender.setImpl(impl);
        }
        senders.put(key, sender);
    }

    RpcCallBaseModel call(RpcCallBaseModel model, String methodName, boolean async) throws NoSuchMethodException {
        TaskSenderInterface sender = null;
        String key = StringHelper.concat(methodName, "-", async);
        if (senders.containsKey(key)) {
            sender = senders.get(key);
            RpcCallBaseModel ret = sender.send(methodName, model);
            return ret;
        }
//        if(impl!=null){
//            sender.setImpl(impl);
//        }
        throw new NoSuchMethodException("called method not registered");
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        senders = CollectionBuilder.newHashMap();
//        sender.setExecutorService(service);
    }

    public void close() {
        CollectionBuilder.travelMap(senders, new CollectionBuilder.Traveler<String, TaskSenderInterface>() {
            @Override
            public boolean travel(String s, TaskSenderInterface taskSender, int i) {
                taskSender.close();
                return true;
            }
        });
        try {
            pool.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
