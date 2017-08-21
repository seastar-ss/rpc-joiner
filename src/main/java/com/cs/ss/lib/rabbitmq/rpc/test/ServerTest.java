package com.cs.ss.lib.rabbitmq.rpc.test;

import com.shawn.ss.lib.tools.L;
import com.cs.ss.lib.rabbitmq.rpc.server.MethodMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by ss on 2016/7/12.
 */
public class ServerTest {

    public static void main(String[] args) {
        CallerTest test = new CallerTest();
        L.w("start", "rpc lib tester handler run");
        String defaultConfigPosition = "./conf/context.xml";
        if (args != null && args.length == 1) {
            defaultConfigPosition = args[0];
        }
        ApplicationContext ctx =
                new FileSystemXmlApplicationContext(defaultConfigPosition);
        final MethodMapper bean = ctx.getBean(MethodMapper.class);
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);
        exec.schedule(new Runnable() {
            @Override
            public void run() {
                bean.stop();
            }
        }, 5, TimeUnit.MINUTES);
    }
}
