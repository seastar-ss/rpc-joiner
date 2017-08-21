package com.cs.ss.lib.rabbitmq.rpc.test;

import com.cs.ss.lib.rabbitmq.rpc.api.server_annotaions.ServerImpl;

/**
 * Created by ss on 2016/7/12.
 */
//@ServerImpl
public class TestServerImpl implements TestInterface {
    @Override
    public int intTest(int[] a, String b) {
        System.out.println("hooooooooo");
        return (int) (Math.random() * 100);
    }

    @Override
    public void doTest(TestModel model, boolean b) {
        System.out.println("hahahahahah");
    }
}
