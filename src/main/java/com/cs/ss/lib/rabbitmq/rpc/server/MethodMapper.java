package com.cs.ss.lib.rabbitmq.rpc.server;

import com.shawn.ss.lib.tools.CollectionBuilder;
import com.shawn.ss.lib.tools.L;
import com.shawn.ss.lib.tools.thread_support.DefaultThreadFactory;
import com.cs.ss.lib.rabbitmq.rpc.api.HandlerInterface;
import com.cs.ss.lib.rabbitmq.rpc.api.server_annotaions.ServerImpl;
import com.cs.ss.lib.rabbitmq.rpc.model.CallerInfo;
import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;
import com.cs.ss.lib.rabbitmq.rpc.utils.DataPackger;
import com.cs.ss.lib.rabbitmq.rpc.utils.DataUnpackager;
import com.cs.ss.lib.rabbitmq.rpc.utils.MqConfig;
import com.cs.ss.lib.rabbitmq.rpc.utils.PackHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by ss on 2016/5/27.
 */
//@Component
public class MethodMapper implements InitializingBean, ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

    private Integer maxChannelPerConnection;

    final Map<String, CallerInfo> callers;
    Set<BeanDefinition> clazzes;
    String basePackage;
    ExecutorService executor;
    //    @Value("${app.server.impl.base.package}")
    String serverImplBasePackage;

    public String getServerImplBasePackage() {
        return serverImplBasePackage;
    }

    public void setServerImplBasePackage(String serverImplBasePackage) {
        this.serverImplBasePackage = serverImplBasePackage;
//        return this;
    }

    @Autowired
    TaskHandlerFactory serverController;

    @Autowired
    MqConfig config;

    ApplicationContext context;

    public MethodMapper() {
        callers = CollectionBuilder.newHashMap();
    }

    public Set<BeanDefinition> findAnnotatedClasses(String scanPackage) {
        Set<BeanDefinition> components = CollectionBuilder.newHashSet();
        if (scanPackage.indexOf(",") > 0) {
        } else {
            ClassPathScanningCandidateComponentProvider provider = createComponentScanner();
            components.addAll(provider.findCandidateComponents(scanPackage));
            L.w("componets:", components.size());
        }
        return components;
    }

    private ClassPathScanningCandidateComponentProvider createComponentScanner() {
        // Don't pull default filters (@Component, etc.):
        ClassPathScanningCandidateComponentProvider provider
                = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(ServerImpl.class));
//        provider.addIncludeFilter(new AnnotationTypeFilter(Component.class));
        return provider;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        L.w("after properties set called ", System.currentTimeMillis());
        if (maxChannelPerConnection == null) {
            maxChannelPerConnection = config.getMaxChannel();
        }
    }

    private void initQueues() throws IllegalAccessException, InstantiationException {
        Set<String> keySet = callers.keySet();

//        Method[] methods = cl.getMethods();
//        L.w("all methods:", Arrays.toString(methods));
        int n = callers.size();
        if (n > 0) {

            executor = new ThreadPoolExecutor(n, n,
                    600L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), new DefaultThreadFactory("ampq-consumers"));
            TaskHandlerFactory.setExecutor(executor);
            List<Map<String, HandlerInterface>> maps = CollectionBuilder.newArrayList(); //CollectionBuilder.newHashMap();
            Map<String, HandlerInterface> map = CollectionBuilder.newHashMap();
            for (String s : keySet) {
                final CallerInfo val = callers.get(s);
                Class cl = val.getMainClass();
                final Object o = this.context.getBean(cl);
                String methodName = val.getMethodName();
                String clsName = val.getClassName();
//            if (clsName.equals(cl.getName())) {
                try {
                    final Method clMethod = cl.getMethod(methodName, val.getCls());
//                    serverController.newTask(val.getName(), new MyHandlerInterface(val, clMethod, o));
                    if (map.size() >= maxChannelPerConnection) {
                        maps.add(map);
                        map = CollectionBuilder.newHashMap();
                    }
                    map.put(val.getName(), new MyHandlerInterface(val, clMethod, o));
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
//            }
            }
            maps.add(map);
            for (Map<String, HandlerInterface> ms : maps) {
                serverController.newTasks(ms);
            }

//            callers.clear();
        }
    }

    public void stop() {
        serverController.stop();
        executor.shutdown();
    }

    public Integer getMaxChannelPerConnection() {
        return maxChannelPerConnection;
    }

    public MethodMapper setMaxChannelPerConnection(Integer maxChannelPerConnection) {
        this.maxChannelPerConnection = maxChannelPerConnection;
        return this;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
//        contextRefreshedEvent.getSource()
        L.w("context event:", contextRefreshedEvent.getTimestamp());
        clazzes = findAnnotatedClasses(serverImplBasePackage);
        for (BeanDefinition def : clazzes) {
//            Class<? extends BeanDefinition> aClass = def.getClass();
            try {
                Class<?> cl = Class.forName(def.getBeanClassName());
                Class[] interfaces = cl.getInterfaces();
                if (interfaces.length > 0) {
                    for (Class clItem : interfaces) {
                        PackHelper.init(clItem, cl, callers, false);
                    }

                }
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        try {
            initQueues();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
//        for(BeanDefinition def:clazzes)
//            try {
//                initQueues(cl);
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            }
    }


    private static class MyHandlerInterface implements HandlerInterface {
        private final CallerInfo val;
        private final Method clMethod;
        private final Object o;

        public MyHandlerInterface(CallerInfo val, Method clMethod, Object o) {
            this.val = val;
            this.clMethod = clMethod;
            this.o = o;
        }

        @Override
        public RpcCallBaseModel handle(RpcCallBaseModel model) {
            DataUnpackager unpacker = val.getUnpacker();
            Object[] objects = unpacker.uppackAllParam(model);
            L.w("unpacker:", unpacker.getParamCount());
            L.w(" ", objects);
            try {
                Object invoke = clMethod.invoke(o, objects);
                DataPackger packer = val.getPacker();
                RpcCallBaseModel param = packer.packParam(invoke);
                param.setMsg("handled by " + o.getClass());
                param.setStatus(RpcCallBaseModel.SUCCESS);
                return param;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return RpcCallBaseModel.buildModel("失败 " + e.getMessage(), RpcCallBaseModel.EXCEPTION);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
                return RpcCallBaseModel.buildModel("失败 " + e.getMessage(), RpcCallBaseModel.EXCEPTION);
            }
//            return null;
        }

        @Override
        public void preHandle(RpcCallBaseModel model) {

        }

        @Override
        public void postHandle() {

        }

        @Override
        public RpcCallBaseModel handleException(Exception ex) {
            return RpcCallBaseModel.buildModel("失败 " + ex.getMessage(), RpcCallBaseModel.EXCEPTION);
        }
    }
}
