package com.cs.ss.lib.rabbitmq.rpc.server;

import com.shawn.ss.lib.tools.CollectionBuilder;
import com.shawn.ss.lib.tools.L;
import com.shawn.ss.lib.tools.StringHelper;
import com.cs.ss.lib.rabbitmq.rpc.RabbitException;
import com.cs.ss.lib.rabbitmq.rpc.api.HandlerInterface;
import com.cs.ss.lib.rabbitmq.rpc.model.RpcCallBaseModel;
import com.cs.ss.lib.rabbitmq.rpc.utils.ConnectionPool;
import com.cs.ss.lib.rabbitmq.rpc.utils.MqConfig;
import com.google.gson.JsonParseException;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by ss on 2016/5/25.
 */
final class TasksHandler {
    //    Runnable impl;
//    final String[] QUEUE_NAME;
    //    final HandlerInterface interfaceImpl;
    Map<String, HandlerInterface> interfaces;
    ConnectionPool conn;
    Connection connection;
    MqConfig config;
    List<Runnable> runners;

//    boolean async;

    TasksHandler(Map<String, HandlerInterface> interfaces) {
//        QUEUE_NAME = queueName;
//        config = config;
//        QUEUE = new LinkedBlockingDeque<QueueingConsumer.Delivery>();
//        this.interfaceImpl = interfaceImpl;
        this.interfaces = interfaces;
        runners = CollectionBuilder.newArrayList();
//        stop = false;
    }

    private Connection getConnection() {
        return conn.getConnection();
    }

    public TasksHandler setConn(ConnectionPool conn) {
        this.conn = conn;
        return this;
    }

    public MqConfig getConfig() {
        return config;
    }

    public TasksHandler setConfig(MqConfig config) {
        this.config = config;
        return this;
    }

    protected void returnConnection() {
        if (connection != null) {
            conn.returnConnection(connection);
        }
    }


    public void build(ExecutorService exec) {
        try {
            connection = getConnection();
//            Channel channel = null;
            L.w("connection:", connection, " ", connection.getAddress());
            QueueingConsumer qc = null;
            Set<String> set = interfaces.keySet();
            try {
                for (String qn : set) {
                    HandlerInterface interfaceImpl = interfaces.get(qn);
                    Channel c = connection.createChannel();
                    L.w("channel build:", c.getChannelNumber());
                    c.basicQos(config.getParallelCount());
                    TaskRunner runner = new TaskRunner(c, interfaceImpl, qn);
                    runners.add(runner);
                    Future<?> control = exec.submit(runner);
                    runner.setControl(control);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
//            returnConnection();
        }
    }

    public void close() {
        for (Runnable runner : runners) {
            if (runner instanceof TaskRunner) {
                TaskRunner r = (TaskRunner) runner;
                r.stop();
            }
        }
        returnConnection();
    }

    static class TaskRunner implements Runnable, HandlerInterface {
        final BlockingQueue<QueueingConsumer.Delivery> QUEUE;
        String consumerTag;
        Future control;
        final HandlerInterface interfaceImpl;
        final Channel channel;
        final String QUEUE_NAME;
        QueueingConsumer qc = null;
        boolean stop;

        public TaskRunner(Channel c, HandlerInterface interfaceImpl, String QUEUE_NAME) throws IOException {
            this.channel = c;
            this.interfaceImpl = interfaceImpl;
            this.QUEUE_NAME = QUEUE_NAME;
            QUEUE = new LinkedBlockingDeque<QueueingConsumer.Delivery>();
            c.queueDeclare(QUEUE_NAME, true, false, false, null);

            qc = new QueueingConsumer(c, QUEUE);
            consumerTag = c.basicConsume(QUEUE_NAME, false, qc);
        }

        @Override
        public void run() {
            try {
                L.w("channel:", QUEUE_NAME, " has been declared");
                boolean flag = true;
                Long deliverTag = null;
                RpcCallBaseModel ret = null;
                String replyTo = null;
                String correlationId = null;
                if (channel != null && qc != null) {
                    try {
                        while (!stop && flag) {
                            try {
//                        deliverTag = handle(channel, qc);
                                QueueingConsumer.Delivery delivery = qc.nextDelivery();
                                Envelope envelope = delivery.getEnvelope();
                                deliverTag = envelope.getDeliveryTag();
                                AMQP.BasicProperties properties = delivery.getProperties();
                                replyTo = properties.getReplyTo();
                                correlationId = properties.getCorrelationId();
                                String encoding = properties.getContentEncoding();
                                if (encoding == null) {
                                    encoding = "UTF-8";
                                }
                                byte[] body = delivery.getBody();
                                String content = new String(body, encoding);

                                ret = handleMsg(content);

                                flag = deliverTag != null;
//                            if (flag) {
//
//                            }
                            } catch (RabbitException ex) {
                                ex.printStackTrace();
                                ret = RpcCallBaseModel.buildModel(StringHelper.concat("执行出错：", ex.getMessage()), RpcCallBaseModel.BAD_REQUEST);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                                stop = true;
//                            ret = RpcCallBaseModel.buildModel(StringHelper.concat("执行出错：", ex.getMessage()), RpcCallBaseModel.EXCEPTION);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                ret = RpcCallBaseModel.buildModel(StringHelper.concat("执行出错：", ex.getMessage()), RpcCallBaseModel.EXCEPTION);
                            } finally {
                                if (deliverTag != null) {
                                    channel.basicAck(deliverTag, false);
                                } else {
                                    throw RabbitException.newException("no deliver tag ,system error");
                                }
                                if (replyTo != null && correlationId != null) {
                                    AMQP.BasicProperties replyProps =
                                            new AMQP.BasicProperties.Builder().correlationId(correlationId)
                                                    .build();
                                    String retMsg = StringHelper.JsonHelper.toJson(ret);
                                    channel.basicPublish("", replyTo, replyProps, retMsg.getBytes());
                                } else {
//                                throw RabbitException.newException("no reply queue or related properties ,system error");
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        channel.queueDelete(QUEUE_NAME);
                        channel.close();
                    }
                } else {
                    throw new IllegalAccessException("can't create channel or consumer");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                QUEUE.clear();
//                try {
//                    channel.queueDelete(QUEUE_NAME);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                returnConnection();
            }
        }

        protected RpcCallBaseModel handleMsg(String content) {
            try {
                RpcCallBaseModel model = StringHelper.JsonHelper.fromJson(content, RpcCallBaseModel.class);
                preHandle(model);
                return handle(model);
            } catch (JsonParseException ex) {
                ex.printStackTrace();
                throw RabbitException.newException("数据传输存在问题");
            } catch (Exception ex) {
                ex.printStackTrace();
                return handleException(ex);
            } finally {
                postHandle();
            }
        }

        //    protected abstract RpcCallBaseModel handle(RpcCallBaseModel model);
//
//    protected  void preHandle(RpcCallBaseModel model){
//
//    }
        public Future getControl() {
            return control;
        }

        public TaskRunner setControl(Future control) {
            this.control = control;
            return this;
        }

        @Override
        public RpcCallBaseModel handle(RpcCallBaseModel model) {
            return interfaceImpl.handle(model);
        }

        @Override
        public void preHandle(RpcCallBaseModel model) {
            interfaceImpl.preHandle(model);
        }

        @Override
        public void postHandle() {
            interfaceImpl.postHandle();
        }

        @Override
        public RpcCallBaseModel handleException(Exception ex) {
            return interfaceImpl.handleException(ex);
        }

        public void stop() {
            this.stop = true;
            if (control != null) {
                control.cancel(true);
            }
        }

        public String getConsumerTag() {
            return consumerTag;
        }

        public TaskRunner setConsumerTag(String consumerTag) {
            this.consumerTag = consumerTag;
            return this;
        }

        //    public Long handle(Channel channel, QueueingConsumer qc) {
//        try {
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return null;
//    }
    }


}