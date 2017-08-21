package com.cs.ss.lib.rabbitmq.rpc.client;

import com.shawn.ss.lib.tools.StringHelper;
import com.cs.ss.lib.rabbitmq.rpc.RabbitException;
import com.cs.ss.lib.rabbitmq.rpc.api.SenderHandlerInterface;
import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;
import com.cs.ss.lib.rabbitmq.rpc.utils.ConnectionPool;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeoutException;

/**
 * Created by ss on 2016/5/26.
 */
//@Component
final class TaskSenderImpl implements com.cs.ss.lib.rabbitmq.rpc.api.TaskSenderInterface {
    public static final String CHARACTER_ENCODING = "UTF-8";
    private static final int MAX_FAIL_TIME = 4;
    //    @Autowired
    final ConnectionPool pool;
    //    @Autowired
//    ScheduledExecutorService executorService;
    final boolean async;
    Connection connection;
    volatile Channel channel;
    SenderHandlerInterface impl;

    public TaskSenderImpl(ConnectionPool pool, boolean async) {
        this.pool = pool;
        this.async = async;
    }

    @Override
    public SenderHandlerInterface getImpl() {
        return impl;
    }

    @Override
    public TaskSenderImpl setImpl(SenderHandlerInterface impl) {
        this.impl = impl;
        return this;
    }

    //    public ConnectionPool getPool() {
//        return pool;
//    }

//    public TaskSenderImpl setPool(ConnectionPool pool) {
//        this.pool = pool;
//        return this;
//    }

//    public ScheduledExecutorService getExecutorService() {
//        return executorService;
//    }
//
//    public TaskSenderImpl setExecutorService(ScheduledExecutorService executorService) {
//        this.executorService = executorService;
//        return this;
//    }

    @Override
    public RpcCallBaseModel send(String calledQueueName, RpcCallBaseModel model) {
        if (!async) {
            return sendSync(calledQueueName, model);
        } else {
            sendAsync(calledQueueName, model);
            return null;
        }

    }

    private void sendAsync(String calledQueueName, RpcCallBaseModel model) {
        if (channel == null || connection == null) {
            synchronized (TaskSenderImpl.class) {
                if (channel == null || connection == null) {
                    try {
                        init();
                    } catch (Exception e) {
                        RabbitException.newException("无法连接服务器");
                    }
                }
            }
        }
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .contentEncoding(CHARACTER_ENCODING)
                .build();
        String request = StringHelper.JsonHelper.toJson(model);
        try {
            channel.confirmSelect();
            channel.basicPublish("", calledQueueName, props, request.getBytes());
            channel.waitForConfirmsOrDie();
        } catch (Exception ex) {
            RabbitException.newException("提交出错");
        }
    }

    private RpcCallBaseModel sendSync(String calledQueueName, RpcCallBaseModel model) {
        if (channel == null || connection == null) {
            synchronized (TaskSenderImpl.class) {
                if (channel == null || connection == null) {
                    try {
                        init();
                    } catch (Exception e) {
                        return RpcCallBaseModel.buildModel("无法建立连接", RpcCallBaseModel.UNREACHABLE);
                    }
                }
            }
        }
        String queueName = null;
        try {

            queueName = channel.queueDeclare().getQueue();
            channel.basicQos(1);
            QueueingConsumer consumer = new QueueingConsumer(channel, new SynchronousQueue<QueueingConsumer.Delivery>());
            channel.basicConsume(queueName, false, consumer);
            String corelationId = UUID.randomUUID().toString();
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(corelationId)
                    .replyTo(queueName).contentEncoding(CHARACTER_ENCODING)
                    .build();
            String request = StringHelper.JsonHelper.toJson(model);
            channel.confirmSelect();
            channel.basicPublish("", calledQueueName, props, request.getBytes());
            channel.waitForConfirmsOrDie();
            int count = 0;
            while (count < MAX_FAIL_TIME) {
                Long deliverTag = null;
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery(2500);
                    if (delivery != null) {
                        if (delivery.getProperties().getCorrelationId().equals(corelationId)) {
                            String response = new String(delivery.getBody(), CHARACTER_ENCODING);
                            deliverTag = delivery.getEnvelope().getDeliveryTag();
                            if (response != null) {
                                RpcCallBaseModel ret = StringHelper.JsonHelper.fromJson(response, RpcCallBaseModel.class);
                                handle(ret);
                                return ret;
                            } else {
                                return RpcCallBaseModel.buildModel("无效返回", RpcCallBaseModel.EXCEPTION);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    return RpcCallBaseModel.buildModel("未知错误", -2);
                } finally {
                    if (deliverTag != null) {
                        channel.basicAck(deliverTag, false);
                    }
                    postHandle();
                }
                ++count;
            }
            return RpcCallBaseModel.buildModel("调用失败", RpcCallBaseModel.EXCEPTION);
        } catch (Exception e) {
            e.printStackTrace();
            return RpcCallBaseModel.buildModel("未知错误", RpcCallBaseModel.EXCEPTION);
        } finally {
            if (channel != null) {
                if (queueName != null) {
                    try {
                        channel.queueDelete(queueName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

//                try {
//                    channel.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (TimeoutException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }


    private void handle(RpcCallBaseModel ret) {
        if (impl != null) {
            impl.handle(ret);
        }
    }

    private void postHandle() {
        if (impl != null) {
            impl.postHandle();
        }
    }

    @Override
    public void close() {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        if (pool != null) {
//            try {
//                pool.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }


    //    @Override
    public void init() throws IOException {
        connection = pool.getConnection();
        channel = connection.createChannel();
    }
}
