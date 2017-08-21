package com.cs.ss.lib.rabbitmq.rpc.utils;

import com.shawn.ss.lib.tools.L;
import com.shawn.ss.lib.tools.TypeConstants;
import com.cs.ss.lib.rabbitmq.rpc.InvokeException;
import com.cs.ss.lib.rabbitmq.rpc.model.CallerInfo;
import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;
import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by ss on 2016/6/5.
 */
public class PackHelper implements DataPackger, DataUnpackager {

    public static Gson gson = new Gson();
    //    Map<Integer,Class> types;
    Class<?>[] types;
    int tCount;
//    Class<?> returnType;

    public PackHelper(Class<?>[] types) {
        this.types = types;
        this.tCount = types.length;
//        this.returnType = returnType;
    }

    @Override
    public RpcCallBaseModel packParam(Object... args) {
//        int n = args.length;
        if ((args == null || args.length == 0 || (args.length == 1 && args[0] == null))
                && (types == null || types.length == 0 || (types.length == 1 && (types[0].equals(Void.class)) || types[0].equals(void.class)))) {
            return RpcCallBaseModel.buildModel(0);
        }
        L.w("args:", args == null ? "null" : args);
        L.w(" types:", types);
        checkArgs(args);
        RpcCallBaseModel ret = RpcCallBaseModel.buildModel(tCount);
        if (tCount > 0) {
            for (int i = 0; i < tCount; ++i) {
                Object arg = args[i];
                RpcCallBaseModel.Arg seal = seal(arg, i);
                ret.addContent(seal);
            }
        } else {
            ret.setContentType(RpcCallBaseModel.VOID_CONTENT);
        }
        return ret;
    }

    @Override
    public RpcCallBaseModel packObjParam(Object... args) {
        return null;
    }

    @Override
    public int getArgCount() {
        return tCount;
    }

    private RpcCallBaseModel.Arg seal(Object arg, int i) {
        RpcCallBaseModel.Arg ret;
        Class t = types[i];
        if (TypeConstants.PRIMITIVE_CLASS.contains(t)) {
            ret = new RpcCallBaseModel.Arg(t.getName(), arg.toString());
        }
//        else if (t.isArray() && t.getComponentType().equals(byte.class)) {
//            byte[] args = (byte[]) arg;
//            ret = new RpcCallBaseModel.Arg(t.getName(), args);
////            ret = null;
//        }
        else {
            ret = new RpcCallBaseModel.Arg(t.getName(), gson.toJson(arg));
        }
//        ret.contentType=;
//        ret.content=gson.toJson(arg);
        return ret;
    }

    private void checkArgs(Object[] args) {
//        int n = args.length;
        if (types.length != args.length) {
            throw InvokeException.newException("parameter count not match to the interface function required");
        }
        for (int i = 0; i < tCount; ++i) {
            if (args[i] != null) {
                Class<?> argCls = args[i].getClass();
                L.w("test class: ", argCls, " ", types[i]);

                if (!types[i].isAssignableFrom(argCls) && !TypeConstants.boxable(types[i], argCls)) {
                    args[i] = TypeConstants.testPackPrType(args[i], types[i]);
                    if (args[i] == null) {
//                    if (argCls.isPrimitive()) {
//                        if (!Number.class.isAssignableFrom(argCls) && !Number.class.isAssignableFrom(types[i])) {
//                            throw InvokeException.newException("parameter " + i + " not match to the interface function required");
//                        }
//                    } else if (!argCls.equals(Object.class)) {
                        throw InvokeException.newException("parameter " + i + " not match to the interface function required");
//                    } else {
////                    types[i].cast(args[i]);
//
//                    }
                    }
                }
            }
        }
    }

    @Override
    public Object unpackParam(RpcCallBaseModel model, int i) {
        if (i >= tCount) {
            throw InvokeException.newException("no such param ,index " + i + " is too big ");
        }
        Class t = types[i];
        RpcCallBaseModel.Arg arg = model.getContent(i);
        String contentType = arg.getContentType();
        byte[] content = arg.getContent();
        String clsName = t.getName();
        L.w("classes matched:", clsName, " ", contentType);
//        if (!clsName.equals(contentType)) {
//            throw InvokeException.newException("param not match the defined function");
//        }
        if (TypeConstants.PRIMITIVE_CLASS.contains(t)) {
            return TypeConstants.uppackPrType(content, contentType);
        }
//        else if (t.isArray() && TypeConstants.PRIMITIVE_CLASS.contains(t.getComponentType())) {
//
//        }
        else {
            try {
                Class<?> cls = Class.forName(contentType);
                Object o = gson.fromJson(new String(content), cls);
                return o;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

//    public static Object uppackPrType(String content,String contentType) {
////        String content = arg.getContent();
////        String contentType = arg.getContentType();
////        java.lang.Character c;
////        Date d;
//        if(contentType.equals("I") || contentType.equals("java.lang.Integer")){
//            return Integer.valueOf(content);
//        }else if(contentType.equals("J") || contentType.equals("java.lang.Long")){
//            return Long.parseLong(content);
//        }else if(contentType.equals("B") || contentType.equals("java.lang.Byte")){
//            return Byte.parseByte(content);
//        }else if(contentType.equals("C") || contentType.equals("java.lang.Character")){
//            return new Character(content.charAt(0));
//        }else if(contentType.equals("Z") || contentType.equals("java.lang.Boolean")){
//            return Boolean.parseBoolean(content);
//        }else if(contentType.equals("D") || contentType.equals("java.lang.Double")){
//            return Double.parseDouble(content);
//        }else if(contentType.equals("java.lang.String")){
//            return content;
//        }else if(contentType.equals("java.util.Date")){
//            return content;
//        }
//        return null;
//    }

    @Override
    public Object[] uppackAllParam(RpcCallBaseModel model) {
//        int n=types.length;
        Object[] objs = new Object[tCount];
        for (int i = 0; i < tCount; ++i) {
            objs[i] = unpackParam(model, i);
        }
        return objs;
    }

    @Override
    public int getParamCount() {
        return tCount;
    }

    private static PackHelper[] createHandler(Class<?>[] types, Class<?> returnType) {
        return new PackHelper[]{new PackHelper(types), new PackHelper(new Class[]{returnType})};
    }

    public static <T> void init(Class<T> tClass, Class<T> cls, Map<String, CallerInfo> callers, boolean revert) {
        Method[] methods = tClass.getDeclaredMethods();
        String mClName = tClass.getName();
        for (Method m : methods) {
            String name = m.getName();

            Class<?>[] types = m.getParameterTypes();
            Class<?> returnType = m.getReturnType();
            boolean noReturen = returnType.equals(Void.class) || returnType.equals(void.class);
            L.w("types: ", types, " ", returnType, " ret:", noReturen);
            PackHelper[] dataHandler = createHandler(types, returnType);
            CallerInfo info;
            if (revert) {
                info = new CallerInfo(mClName, name, dataHandler[1], dataHandler[0], noReturen, types, cls);
            } else {
                info = new CallerInfo(mClName, name, dataHandler[0], dataHandler[1], noReturen, types, cls);
            }
            L.w("caller info:", info.toString());
//            this.dataPackgerMap.put(qName, dataHandler);
//            this.dataUnpackgerMap.put(qName, dataHandler);
            callers.put(info.getName(), info);
//            m.getParameters();
//            for (int i=0,n=types.length;i<n;++i) {
//                Class<?> t = types[i];
//                if (PRIMITIVE_CLASS.contains(t)) {
//
//                } else {
//                    L.w("unsupport type","only string int double is supported");
//                }
//            }
        }
    }
}
