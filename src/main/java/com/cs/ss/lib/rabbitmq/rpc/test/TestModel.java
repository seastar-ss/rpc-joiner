package com.cs.ss.lib.rabbitmq.rpc.test;

import com.shawn.ss.lib.tools.L;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ss on 2016/6/28.
 */
public class TestModel {
    public static final SimpleDateFormat DEFAULT_DATE_PARSER = new SimpleDateFormat("EE MMM dd HH:mm:ss Z yyyy");
    public static void main(String[] args) throws ParseException {
//        L.w("parse:",DEFAULT_DATE_PARSER.parse("Wed Jun 29 18:48:04 CST 2016"));
        L.w("string:","string".split(""));
        L.w("to String",DEFAULT_DATE_PARSER.format(new Date()));
//        int.class.isPrimitive();
        int a[]=new int[10];
        for(int i=0;i<10;++i){
            a[i]=i+1;

        }
        printA(a);
        testModifyArray(a);
        printA(a);
    }

    private static void printA(int[] a) {
        for(int i=0;i<10;++i){
            L.w("a :", a[i]);

        }
    }

    private static void testModifyArray(int[] a) {
        for(int i=0;i<10;++i){
            a[i]=i+10;
        }
    }

    int a;
    String b;
    Long c;
}
