package com.cs.ss.lib.rabbitmq.rpc.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by ss on 2016/5/25.
 */
@Component
public class MqConfig {

    //    @Value("${rbt.ip}")
//    String host;
//    @Value("${rbt.user}")
//    String username;
//    @Value("${rbt.pass}")
//    String password;
//    @Value("${rbt.vhost}")
//    String vhost;
    @Value("${rbt.timeout}")
    Integer timeout;
    @Value("${rbt.recoverInteval}")
    Integer recoverTime;
    @Value("${rbt.content.num}")
    Integer parallelCount;
    @Value("${rbt.url}")
    String url;
    @Value("${rbt.max.connection}")
    Integer maxConnection;
    @Value("${rbt.max.channel.per.connection}")
    Integer maxChannel;
    @Value("${rbt.heart.beat.time}")
    Integer heartBeatTimeout;

    //amqp://userName:password@hostName:portNumber/virtualHost

    public MqConfig() {
    }

//    public String getHost() {
//        return host;
//    }
//
//    public MqConfig setHost(String host) {
//        this.host = host;
//        return this;
//    }
//
//    public String getUsername() {
//        return username;
//    }
//
//    public MqConfig setUsername(String username) {
//        this.username = username;
//        return this;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public MqConfig setPassword(String password) {
//        this.password = password;
//        return this;
//    }
//
//    public String getVhost() {
//        return vhost;
//    }
//
//    public MqConfig setVhost(String vhost) {
//        this.vhost = vhost;
//        return this;
//    }

    public Integer getTimeout() {
        return timeout;
    }

    public MqConfig setTimeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

//    public Integer getSize() {
//        return size;
//    }
//
//    public MqConfig setSize(Integer size) {
//        this.size = size;
//        return this;
//    }

    public String getUrl() {
        return url;
    }

    public MqConfig setUrl(String url) {
        this.url = url;
        return this;
    }

    public Integer getRecoverTime() {
        return recoverTime;
    }

    public MqConfig setRecoverTime(Integer recoverTime) {
        this.recoverTime = recoverTime;
        return this;
    }

    public Integer getParallelCount() {
        return parallelCount;
    }

    public MqConfig setParallelCount(Integer parallelCount) {
        this.parallelCount = parallelCount;
        return this;
    }

    public Integer getMaxConnection() {
        return maxConnection;
    }

    public MqConfig setMaxConnection(Integer maxConnection) {
        this.maxConnection = maxConnection;
        return this;
    }

    public Integer getMaxChannel() {
        return maxChannel;
    }

    public MqConfig setMaxChannel(Integer maxChannel) {
        this.maxChannel = maxChannel;
        return this;
    }

    public Integer getHeartBeatTimeout() {
        return heartBeatTimeout;
    }

    public MqConfig setHeartBeatTimeout(Integer heartBeatTimeout) {
        this.heartBeatTimeout = heartBeatTimeout;
        return this;
    }
}
