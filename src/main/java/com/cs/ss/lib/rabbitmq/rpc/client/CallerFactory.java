package com.cs.ss.lib.rabbitmq.rpc.client;

import com.shawn.ss.lib.tools.StringHelper;
import com.shawn.ss.lib.tools.structure.LRUCache;
import com.cs.ss.lib.rabbitmq.rpc.api.CallerInterface;
import com.cs.ss.lib.rabbitmq.rpc.api.SenderHandlerInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

/**
 * Created by ss on 2016/5/27.
 */
@Component
public class CallerFactory {
    @Autowired
    TaskSenderFactory factory;

    LRUCache<String, Object> cache;

    public CallerFactory() {
        cache = new LRUCache<String, Object>(500);
    }


    public <T extends CallerInterface> T callerBuilder(Class<T> tClass, SenderHandlerInterface impl) {
//        String tClassName = tClass.getName();
        Object instance = buildCaller(tClass, impl);
        return (T) instance;
    }

    public void close() {
        factory.close();
    }

    public <T extends CallerInterface> Object buildCaller(Class<T> tClass, SenderHandlerInterface impl) {
        Object ret;
        String k = buildKey(tClass, impl);
        ret = cache.get(k);
        if (ret == null) {
            ret = Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{tClass}, new InvocationHandlerImpl(tClass, impl, factory));
            cache.put(k, ret);
        }
        return ret;
    }

    private String buildKey(Class<?> tClass, SenderHandlerInterface impl) {
        if (impl == null) {
            return tClass.getName();
        } else {
            return StringHelper.concat(tClass.getName(), "-", impl.hashCode());
        }
    }
}
