package com.cs.ss.lib.rabbitmq.rpc.server;

import com.shawn.ss.lib.tools.CollectionBuilder;
import com.cs.ss.lib.rabbitmq.rpc.api.HandlerInterface;
import com.cs.ss.lib.rabbitmq.rpc.utils.ConnectionPool;
import com.cs.ss.lib.rabbitmq.rpc.utils.MqConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Created by ss on 2016/5/17.
 */

@Component
public class TaskHandlerFactory implements InitializingBean {

    @Autowired
    ConnectionPool pool;
    //    @Autowired
    static ExecutorService executor;
    @Autowired
    MqConfig config;

    private Map<String, List<HandlerInterface>> handlers;
    private List<TasksHandler> multiHandlers;

//    Cipher cipher;
//
//    public Cipher getCipher() {
//        return cipher;
//    }
//
//    public TaskHandlerFactory setCipher(Cipher cipher) {
//        this.cipher = cipher;
//        return this;
//    }

    public TaskHandlerFactory() {
    }

    public void newTasks(Map<String, HandlerInterface> interfaces) {
        TasksHandler tasksHandler = new TasksHandler(interfaces);
        tasksHandler.setConn(pool);
        tasksHandler.setConfig(config);
        tasksHandler.build(executor);
        multiHandlers.add(tasksHandler);
    }

    public void newTask(String taskName, HandlerInterface impl) {
        TaskHandler handler = new TaskHandler(taskName, impl);
        handler.setConn(pool);
        handler.setConfig(config);
        List<HandlerInterface> handlerList = null;
        if (handlers.containsKey(taskName)) {
            handlerList = handlers.get(taskName);
        } else {
            handlerList = CollectionBuilder.newArrayList();
        }
        handlerList.add(handler);
        handlers.put(taskName, handlerList);
        runTask(handler);
    }


    private void runTask(TaskHandler taskHandler) {
//        Connection connection = pool.getConnection();

        Future<?> submit = executor.submit(taskHandler);
        taskHandler.setControl(submit);
    }

    public void stop() {
        stopAllTask();
        executor.shutdown();
    }

    private void stopAllTask() {
        CollectionBuilder.travelMap(handlers, new CollectionBuilder.Traveler<String, List<HandlerInterface>>() {
            @Override
            public boolean travel(String s, List<HandlerInterface> taskHandlers, int i) {
                for (HandlerInterface t : taskHandlers) {
                    t.stop();
                }
                return true;
            }
        });
        for (TasksHandler mul : multiHandlers) {
            mul.close();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        executor = Executors.newFixedThreadPool(config.getParallelCount());
        handlers = CollectionBuilder.newHashMap();
        multiHandlers = CollectionBuilder.newArrayList();
    }

    public static void setExecutor(ExecutorService executor) {
        TaskHandlerFactory.executor = executor;
    }
}
