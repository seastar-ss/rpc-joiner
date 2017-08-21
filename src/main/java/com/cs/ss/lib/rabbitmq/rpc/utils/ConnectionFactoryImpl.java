package com.cs.ss.lib.rabbitmq.rpc.utils;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.util.concurrent.ExecutorService;

/**
 * Created by ss on 2016/5/25.
 */
//@Component
class ConnectionFactoryImpl extends BasePooledObjectFactory<Connection> {
    //    @Autowired
//    MqConfig config;
    final ConnectionFactory factory;
    //    @Autowired
    ExecutorService executor;

    public ConnectionFactoryImpl(ConnectionFactory factory, ExecutorService executor) {
        this.factory = factory;
        this.executor = executor;
    }

    @Override
    public Connection create() throws Exception {
        Connection connection = factory.newConnection(executor);
        return connection;
    }

    @Override
    public PooledObject<Connection> wrap(Connection obj) {
        return new DefaultPooledObject<Connection>(obj);
    }

    @Override
    public void destroyObject(PooledObject<Connection> p) throws Exception {
        Connection connection = p.getObject();
        connection.close();
//        super.destroyObject(p);
    }

    @Override
    public boolean validateObject(PooledObject<Connection> p) {
        return super.validateObject(p);
    }

    @Override
    public void activateObject(PooledObject<Connection> p) throws Exception {
        super.activateObject(p);
    }

    @Override
    public void passivateObject(PooledObject<Connection> p) throws Exception {
        super.passivateObject(p);
    }
}
