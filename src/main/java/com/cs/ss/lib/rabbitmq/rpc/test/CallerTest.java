package com.cs.ss.lib.rabbitmq.rpc.test;

import com.shawn.ss.lib.tools.L;
import com.cs.ss.lib.rabbitmq.rpc.client.CallerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Created by ss on 2016/7/1.
 */
public class CallerTest {
    //    @Autowired
    static CallerFactory factory;

    public static void main(String[] args) {
        CallerTest test = new CallerTest();
        L.w("start", "rpc lib tester handler run");
        String defaultConfigPosition = "./conf/context.xml";
        if (args != null && args.length == 1) {
            defaultConfigPosition = args[0];
        }
        ApplicationContext ctx =
                new FileSystemXmlApplicationContext(defaultConfigPosition);
        factory = ctx.getBean(CallerFactory.class);
        TestInterface testInterface = factory.callerBuilder(TestInterface.class, null);
        for (int i = 0; i < 100; ++i) {
            int what = testInterface.intTest(new int[]{i + 1, i + 2, 4, i}, "what");
            L.w("return :", what);
        }
        factory.close();
    }
}
