package com.cs.ss.lib.rabbitmq.rpc.test;

import com.cs.ss.lib.rabbitmq.rpc.api.CallerInterface;

/**
 * Created by ss on 2016/7/1.
 */
public interface TestInterface extends CallerInterface{
    int intTest(int[] a,String b);
    void doTest(TestModel model,boolean b);
}
