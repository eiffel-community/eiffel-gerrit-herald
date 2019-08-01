/*
 * Copyright (c) 2019, Axis Communications AB. All rights reserved.
 */
package com.axis.eiffel.rabbitmq.service.RabbitMQ;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * @author Christian Bilevits, christian.bilevits@axis.com
 * @since 2019-07-22
 */
abstract class Rabbitmq implements AutoCloseable {

    private ConnectionFactory factory = new ConnectionFactory();

    public Rabbitmq(String username, String password, String host, String virtualHost) {
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setHost(host);
        factory.setVirtualHost(virtualHost);
    }

    public Connection createConnection()
            throws NoSuchAlgorithmException, KeyManagementException, TimeoutException, IOException {
        factory.useSslProtocol();
        return factory.newConnection();
    }

    public abstract void close() throws IOException;
}
