package com.cs.ss.lib.rabbitmq.rpc.model;

import com.shawn.ss.lib.tools.CollectionBuilder;
import com.shawn.ss.lib.tools.code_gen.wapper.DataWrapper;

import java.util.Collections;
import java.util.List;
//import com.sun.javafx.collections.MappingChange;

/**
 * Created by ss on 2016/5/26.
 */
public class RpcCallBaseModel extends DataWrapper {
    public static Integer MULTIPLE_CONTENT = 7;
    public static Integer VOID_CONTENT = 0;
    public static Integer INT_CONTENT = 1;
    public static Integer STRING_CONTENT = 2;
    public static Integer DOUBLE_CONTENT = 3;
    public static Integer STRING_ARRAY_CONTENT = 4;
    public static Integer INTEGER_ARRAY_CONTENT = 5;
    public static Integer DOUBLE_ARRAY_CONTENT = 6;

    public static Integer SUCCESS = 1;
    public static Integer CONTINUING = 0;
    public static Integer BAD_REQUEST = -1;
    public static Integer EXCEPTION = -2;
    public static Integer UNREACHABLE = -3;


    int contentType = MULTIPLE_CONTENT;
    List<Arg> content;
    //    List<String> types;
    String requestId;
    String encryptKey;
    String encryptSalt;

    RpcCallBaseModel() {
        this.setStatus(CONTINUING);
    }


    public static RpcCallBaseModel buildModel(int paramCount) {

        RpcCallBaseModel model = new RpcCallBaseModel();
        if (paramCount == 0) {
            model.setContent(Collections.EMPTY_LIST);
        } else {
            model.setContent(CollectionBuilder.<Arg>newArrayList(paramCount));
        }
//        model.setTypes(CollectionBuilder.newArrayList(paramCount));
//        model.setMsg(msg);
//        model.setStatus(status);
        return model;
    }

    public static RpcCallBaseModel buildModel(int paramCount, String msg, int status) {
        RpcCallBaseModel model = new RpcCallBaseModel();
        model.setContent(CollectionBuilder.<Arg>newArrayList(paramCount));
//        model.setTypes(CollectionBuilder.newArrayList(paramCount));
        model.setMsg(msg);
        model.setStatus(status);
        return model;
    }

    public static RpcCallBaseModel buildModel(String msg, int status) {
        RpcCallBaseModel model = new RpcCallBaseModel();
        model.setMsg(msg);
        model.setStatus(status);
        model.setContentType(VOID_CONTENT);
        return model;
    }

    public int getContentType() {
        return contentType;
    }

    public RpcCallBaseModel setContentType(int contentType) {
        this.contentType = contentType;
        return this;
    }

    public List<Arg> getContents() {
        return content;
    }

    public RpcCallBaseModel setContent(List<Arg> content) {
        this.content = content;
        return this;
    }

//    public List<String> getTypes() {
//        return types;
//    }


    public boolean addContent(Arg arg) {
//        Arg arg=new Arg(type,s);
        boolean flag = content.add(arg);
        return flag;
    }

    public Arg getContent(int index) {
        return content.get(index);
    }

    //    public RpcCallBaseModel setTypes(List<String> types) {
//        this.types = types;
//        return this;
//    }

    public String getRequestId() {
        return requestId;
    }

    public RpcCallBaseModel setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    public String getEncryptKey() {
        return encryptKey;
    }

    public RpcCallBaseModel setEncryptKey(String encryptKey) {
        this.encryptKey = encryptKey;
        return this;
    }

    public String getEncryptSalt() {
        return encryptSalt;
    }

    public RpcCallBaseModel setEncryptSalt(String encryptSalt) {
        this.encryptSalt = encryptSalt;
        return this;
    }


    public static class Arg {
        String contentType;
        String content;

        public Arg(String contentType, byte[] content) {
            this.contentType = contentType;
            this.content = new String(content);
        }

        public Arg(String contentType, String content) {
            this.contentType = contentType;
            this.content = content;
        }

        public String getContentType() {
            return contentType;
        }

        public byte[] getContent() {
            return content.getBytes();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Arg{");
            sb.append("contentType='").append(contentType).append('\'');
            sb.append(", content='").append(content).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
