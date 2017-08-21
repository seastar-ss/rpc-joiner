package com.cs.ss.lib.rabbitmq.rpc.server;

/**
 * Created by ss on 2016/5/17.
 */

import com.shawn.ss.lib.tools.L;
import com.cs.ss.lib.rabbitmq.rpc.api.HandlerInterface;
import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

public class TaskRunnerTester {
    /**
     * @param args
     */
    public static void main(String[] args) {
        L.w("start", "rpc lib tester handler run");
        String defaultConfigPosition = "./conf/context.xml";
        if (args != null && args.length == 1) {
            defaultConfigPosition = args[0];
        }
        ApplicationContext ctx =
                new FileSystemXmlApplicationContext(defaultConfigPosition);
        TaskHandlerFactory taker = ctx.getBean(TaskHandlerFactory.class);
        taker.newTask("com.cs.ss.lib.rabbitmq.rpc.test.TestInterface_intTest", new HandlerInterface() {
            @Override
            public RpcCallBaseModel handle(RpcCallBaseModel model) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                L.w("recieve:", model.getContents());
                if (Math.random() > 0.5) {
                    throw new RuntimeException("错了");
                }
                RpcCallBaseModel ret = RpcCallBaseModel.buildModel(1, "成功", RpcCallBaseModel.SUCCESS);
                ret.addContent(new RpcCallBaseModel.Arg(Integer.class.getName(), String.valueOf((int) Math.floor(Math.random() * 1000))));
                return ret;
            }

            @Override
            public void preHandle(RpcCallBaseModel model) {

            }

            @Override
            public void postHandle() {
                L.w("finish handle one");
            }

            @Override
            public RpcCallBaseModel handleException(Exception ex) {
                return RpcCallBaseModel.buildModel("失败", RpcCallBaseModel.EXCEPTION);
            }

            @Override
            public void stop() {

            }
        });
//        taker.runTask(new TaskHandler("test_queue") {
//
//            @Override
//            protected RpcCallBaseModel handle(RpcCallBaseModel model) {
//                return RpcCallBaseModel.buildModel("成功",1);
//            }
//
//            @Override
//            protected void preHandle(RpcCallBaseModel model) {
//
//            }
//        });
    }
}
