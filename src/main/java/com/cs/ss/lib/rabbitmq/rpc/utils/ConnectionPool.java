package com.cs.ss.lib.rabbitmq.rpc.utils;

//import com.rabbitmq.client.*;

import com.shawn.ss.lib.tools.thread_support.DefaultThreadFactory;
import com.cs.ss.lib.rabbitmq.rpc.RabbitException;
import com.rabbitmq.client.*;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.*;

/**
 * Created by ss on 2016/5/25.
 */
@Component
public class ConnectionPool implements Closeable, InitializingBean, RabbitResource {
    protected GenericObjectPool<Connection> internalPool;
    ExecutorService executorService;

    @Autowired
    ScheduledExecutorService executor;

    @Autowired
    MqConfig config;

    public ConnectionPool() {
//        initPool();

    }


    private void initPool() {
        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost(config.getHost());
//        factory.setUsername(config.getUsername());
//        factory.setPassword(password);
//        factory.setVirtualHost(vhost);
        try {
            factory.setUri(config.getUrl());
            factory.setAutomaticRecoveryEnabled(true);
            factory.setConnectionTimeout(config.getTimeout());
            factory.setNetworkRecoveryInterval(config.getRecoverTime());
            factory.setRequestedHeartbeat(config.getHeartBeatTimeout());
            factory.setHeartbeatExecutor(executor);
            factory.setRequestedChannelMax(config.getMaxChannel());
            factory.setExceptionHandler(new DefaultRabbitmqExceptionHandler());
            if (this.internalPool != null) {
                try {
                    closeInternalPool();
                } catch (Exception e) {
                }
            }

            executorService = new ThreadPoolExecutor(1, 1000,
                    600L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), new DefaultThreadFactory("ampq-connection"));
            this.internalPool = new GenericObjectPool<Connection>(new ConnectionFactoryImpl(factory, executorService), getPoolSetting());
            return;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        throw RabbitException.newException("can't build connection factory or object pool");
    }

    private GenericObjectPoolConfig getPoolSetting() {
        return new RabbitPoolConfig();
    }

    @Override
    public void close() throws IOException {
        closeInternalPool();
        executorService.shutdown();
        executor.shutdown();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initPool();
    }

    protected void closeInternalPool() {
        try {
            internalPool.close();
        } catch (Exception e) {
            throw RabbitException.newException("unable to close pool");
        }
    }

    @Override
    public Connection getConnection() {
        try {
            return internalPool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void returnConnection(Connection conn) {
        try {
            internalPool.returnObject(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class DefaultRabbitmqExceptionHandler implements ExceptionHandler {
        @Override
        public void handleUnexpectedConnectionDriverException(Connection conn, Throwable exception) {

        }

        @Override
        public void handleReturnListenerException(Channel channel, Throwable exception) {

        }

        @Override
        public void handleFlowListenerException(Channel channel, Throwable exception) {

        }

        @Override
        public void handleConfirmListenerException(Channel channel, Throwable exception) {

        }

        @Override
        public void handleBlockedListenerException(Connection connection, Throwable exception) {

        }

        @Override
        public void handleConsumerException(Channel channel, Throwable exception, Consumer consumer, String consumerTag, String methodName) {

        }

        @Override
        public void handleConnectionRecoveryException(Connection conn, Throwable exception) {

        }

        @Override
        public void handleChannelRecoveryException(Channel ch, Throwable exception) {

        }

        @Override
        public void handleTopologyRecoveryException(Connection conn, Channel ch, TopologyRecoveryException exception) {

        }
    }

    class RabbitPoolConfig extends GenericObjectPoolConfig {
        public RabbitPoolConfig() {
            // defaults to make your life with connection pool easier :)
            setTestWhileIdle(true);
            setMinEvictableIdleTimeMillis(60000);
            setTimeBetweenEvictionRunsMillis(30000);
            setNumTestsPerEvictionRun(-1);
            setMaxIdle(10);
            setMaxTotal(config.getMaxConnection());
//            setTestOnReturn(true);
            setMinIdle(2);
        }
    }

}
