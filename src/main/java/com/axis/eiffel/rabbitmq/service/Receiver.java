/*
 * Copyright (c) 2019, Axis Communications AB. All rights reserved.
 */
package com.axis.eiffel.rabbitmq.service;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Bilevits, christian.bilevits@axis.com
 * @since 2019-07-22
 */
public class Receiver extends Rabbitmq {

    private Connection connection;
    private Channel channel;

    private String queueName = "";

    public Receiver(String username, String password, String host, String virtualHost)
            throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException {
        super(username, password, host, virtualHost);
        this.connection = super.createConnection();
        this.channel = createChannel();
    }

    private Channel createChannel() throws IOException {
        return connection.createChannel();
    }

    public void setQueue(String queueName, String exchangeName, String routingKey) throws IOException {
        queueDeclare(queueName);
        queueBind(exchangeName, routingKey);
    }

    public void setQueue(String queueName, String exchangeName, List<String> routingKeys) throws IOException {
        queueDeclare(queueName);
        for (String routingKey : routingKeys) {
            queueBind(exchangeName, routingKey);
        }
    }

    private void queueDeclare(String queueName) throws IOException {
        this.queueName = queueName;
        channel.queueDeclare(this.queueName, true, false, false, null);
    }

    private void queueBind(String exchangeName, String routingKey) throws IOException {
        channel.queueBind(this.queueName, exchangeName, routingKey);
    }

    public void consume(boolean autoAck, DeliverCallback deliverCallback,
                        CancelCallback cancelCallback)
            throws IOException {
        channel.basicConsume(this.queueName, autoAck, deliverCallback, cancelCallback);
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
