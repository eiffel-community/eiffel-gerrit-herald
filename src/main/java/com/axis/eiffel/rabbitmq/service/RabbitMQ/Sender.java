/*
 * Copyright (c) 2019, Axis Communications AB. All rights reserved.
 */
package com.axis.eiffel.rabbitmq.service.RabbitMQ;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Christian Bilevits, christian.bilevits@axis.com
 * @since 2019-07-22
 */
public class Sender extends Rabbitmq {

    private Connection connection;
    private Channel channel;

    private String exchangeName = "";

    public Sender(String username, String password, String host, String virtualHost)
            throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException {
        super(username, password, host, virtualHost);
        this.connection = super.createConnection();
        this.channel = createChannel();
    }

    private Channel createChannel() throws IOException {
        return connection.createChannel();
    }

    public void setExchange(String exchangeName, String type) throws IOException {
        this.exchangeName = exchangeName;
        channel.exchangeDeclare(this.exchangeName, type, true);
    }

    public void send(String routingKey, String message) throws IOException {
        channel.basicPublish(this.exchangeName, routingKey, null, message.getBytes(UTF_8));
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
