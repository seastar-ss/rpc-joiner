package com.cs.ss.lib.rabbitmq.rpc.utils;

import com.rabbitmq.client.Connection;

/**
 * Created by ss on 2016/5/25.
 */
public interface RabbitResource {
    Connection getConnection();

    void returnConnection(Connection conn);
}
