/*
 * Copyright (c) 2019, Axis Communications AB. All rights reserved.
 */
package com.axis.eiffel.rabbitmq.service.RabbitMQ;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

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

    /**
     * Sets the queue to the channel.
     *
     * @param queueName Name of the queue
     * @param exchangeName Name of the exchange
     * @param routingKey Routing key
     * @throws IOException
     */
    public void setQueue(String queueName, String exchangeName, String routingKey) throws IOException {
        queueDeclare(queueName);
        queueBind(exchangeName, routingKey);
    }

    /**
     * Sets the queue to the channel with multiple routing keys.
     *
     * @param queueName Name of the queue
     * @param exchangeName Name of the exchange
     * @param routingKeys Routing keys
     * @throws IOException
     */
    public void setQueue(String queueName, String exchangeName, List<String> routingKeys) throws IOException {
        queueDeclare(queueName);
        for (String routingKey : routingKeys) {
            queueBind(exchangeName, routingKey);
        }
    }

    private void queueDeclare(String queueName) throws IOException {
        this.queueName = queueName;
        channel.queueDeclare(this.queueName, true, false, false, null);
        channel.basicQos(1);
    }

    private void queueBind(String exchangeName, String routingKey) throws IOException {
        channel.queueBind(this.queueName, exchangeName, routingKey);
    }

    /**
     * Starts consuming from a channel.
     *
     * @param autoAck true if channel should automatic send ack
     * @param deliverCallback How to handle the delivery
     * @param cancelCallback What to do if consumer cancel
     * @throws IOException
     */
    public void consume(boolean autoAck, DeliverCallback deliverCallback,
                        CancelCallback cancelCallback)
            throws IOException {
        channel.basicConsume(this.queueName, autoAck, deliverCallback, cancelCallback);
    }

    /**
     * Ack a delivery
     *
     * @param delivery delivery to send ack on
     * @throws IOException
     */
    public void ack(Delivery delivery) throws IOException {
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
