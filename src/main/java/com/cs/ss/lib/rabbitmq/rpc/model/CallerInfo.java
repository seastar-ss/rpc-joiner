package com.cs.ss.lib.rabbitmq.rpc.model;

import com.shawn.ss.lib.tools.StringHelper;
import com.cs.ss.lib.rabbitmq.rpc.utils.DataPackger;
import com.cs.ss.lib.rabbitmq.rpc.utils.DataUnpackager;

import java.util.Arrays;

/**
 * Created by ss on 2016/7/7.
 */
public class CallerInfo {
    String name;
    String className;
    String methodName;
    Class<?>[] cls;
    Class<?> mainClass;
    boolean async;
    DataPackger packer;
    DataUnpackager unpacker;

//    public CallerInfo(String clsName,String methodName, DataUnpackager unpacker, DataPackger packer, boolean async) {
//        String qName = StringHelper.concat(clsName, "_", methodName);
//        this.name = qName;
//        this.methodName=methodName;
//        this.unpacker = unpacker;
//        this.packer = packer;
//        this.async = async;
//    }

    public CallerInfo(String clsName, String methodName, DataUnpackager unpacker, DataPackger packer, boolean async, Class<?>[] cls, Class<?> mainClass) {
        String qName = StringHelper.concat(clsName, "_", methodName);
        this.className = clsName;
        this.name = qName;
        this.methodName = methodName;
        this.unpacker = unpacker;
        this.packer = packer;
        this.async = async;
        this.cls = cls;
        this.mainClass = mainClass;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CallerInfo{");
        sb.append("name='").append(name).append('\'');
        sb.append(", className='").append(className).append('\'');
        sb.append(", methodName='").append(methodName).append('\'');
        sb.append(", cls=").append(Arrays.toString(cls));
        sb.append(", async=").append(async);
        sb.append(", packer=").append(packer);
        sb.append(", unpacker=").append(unpacker);
        sb.append('}');
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public CallerInfo setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isAsync() {
        return async;
    }

    public CallerInfo setAsync(boolean async) {
        this.async = async;
        return this;
    }

    public DataPackger getPacker() {
        return packer;
    }

    public CallerInfo setPacker(DataPackger packer) {
        this.packer = packer;
        return this;
    }

    public DataUnpackager getUnpacker() {
        return unpacker;
    }

    public CallerInfo setUnpacker(DataUnpackager unpacker) {
        this.unpacker = unpacker;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public CallerInfo setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public CallerInfo setClassName(String className) {
        this.className = className;
        return this;
    }

    public Class<?>[] getCls() {
        return cls;
    }

    public CallerInfo setCls(Class<?>[] cls) {
        this.cls = cls;
        return this;
    }

    public Class<?> getMainClass() {
        return mainClass;
    }

    public CallerInfo setMainClass(Class<?> mainClass) {
        this.mainClass = mainClass;
        return this;
    }
}
