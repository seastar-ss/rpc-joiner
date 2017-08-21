package com.cs.ss.lib.rabbitmq.rpc.client;

import com.shawn.ss.lib.tools.CollectionBuilder;
import com.shawn.ss.lib.tools.L;
import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Created by ss on 2016/5/26.
 */
public class TaskSenderTester {

    public static void main(String args[]) throws NoSuchMethodException, InterruptedException {
        L.w("start", "rpc module libs tester run");
        String defaultConfigPosition = "./conf/context.xml";
        if (args != null && args.length == 1) {
            defaultConfigPosition = args[0];
        }
        ApplicationContext ctx =
                new FileSystemXmlApplicationContext(defaultConfigPosition);
        TaskSenderFactory bean = ctx.getBean(TaskSenderFactory.class);
        bean.addCaller(null, "com.cs.ss.lib.rabbitmq.rpc.test.TestInterface_intTest", true);
        bean.addCaller(null, "com.cs.ss.lib.rabbitmq.rpc.test.TestInterface_intTest", false);
        for (int i = 0; i < 1000; ++i) {
//            if () {
            boolean flag;
//            flag = i % 2 == 0;
            flag = false;
            String content = String.valueOf((int) (Math.random() * 1000));

            RpcCallBaseModel ret = bean.call(RpcCallBaseModel.buildModel("test", 1).setContent(CollectionBuilder.<RpcCallBaseModel.Arg>listBuilder().add(new RpcCallBaseModel.Arg("b", "23")).getList()), "com.cs.ss.lib.rabbitmq.rpc.test.TestInterface_intTest", flag);
            if (ret == null) {
                L.w("called ", content);
            } else {
                L.w("called ", content, " ", ret.getMsg(), " ", ret.getStatus(), " ", ret.getContents());
            }
            Thread.sleep(300 + (int) (600 * Math.random()));
//            }else{
//
//            }
        }
        bean.close();
    }
}
