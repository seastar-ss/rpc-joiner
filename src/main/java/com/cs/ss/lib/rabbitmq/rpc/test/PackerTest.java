package com.cs.ss.lib.rabbitmq.rpc.test;

import com.shawn.ss.lib.tools.L;
import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;
import com.cs.ss.lib.rabbitmq.rpc.utils.PackHelper;
import com.google.gson.Gson;

import java.util.Date;

/**
 * Created by ss on 2016/6/28.
 */
public class PackerTest {
    public static void main(String[] args) throws ClassNotFoundException {
        L.w("test:",TestInterface.class.getName());
        TestModel[] atest = {new TestModel(), new TestModel()};
        atest[0].a = 1;
        atest[0].b = "hahahah";
        atest[0].c = 123L;

        atest[1].a = 1;
        atest[1].b = "hahahah";
        atest[1].c = 123L;
        baseTest(atest);
        Class[] cls=new Class[]{String.class, Integer.class,Date.class, int[].class, TestModel.class, TestModel[].class};
        PackHelper p = new PackHelper(cls);
        RpcCallBaseModel model = p.packParam("aksdkf", 1,new Date(), new int[]{1, 2, 43, 5}, atest[0], atest);
        String ret = new Gson().toJson(model);
        L.w("ret:", ret);
        PackHelper pp=new PackHelper(cls);
        Object[] objects = pp.uppackAllParam(model);
        for(Object obj:objects){
            L.w("classes:",obj==null?"NULL":obj.getClass().getName());
            L.w("value:",obj.toString());
        }

    }

    private static void baseTest(TestModel[] atest) {
        Class t = Integer[].class;

        Integer[] a = new Integer[]{1, 5, 6, 77};
        L.w("infos:", t.isInstance(a));
        L.w("toString:", a.toString());
        L.w("baseType:", t.getComponentType());

        L.w("classes:", atest.getClass());
        Gson gson = new Gson();
        String s = gson.toJson(atest);
        L.w("toJson:", s);
        try {
            Class cs = Class.forName("[Lcom.cs.ss.lib.rabbitmq.rpc.test.TestModel;");

            Object o = gson.fromJson(s, cs);
            if (o instanceof TestModel[]) {
                TestModel[] e = (TestModel[]) o;
                L.w("test:", e[0].a);
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
