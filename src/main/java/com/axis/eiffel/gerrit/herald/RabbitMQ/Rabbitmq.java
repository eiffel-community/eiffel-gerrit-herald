/*
 * Copyright 2019 Axis Communications AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.axis.eiffel.gerrit.herald.RabbitMQ;

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

    /**
     * RabbitMQ connection.
     *
     * @param username RabbitMQ username
     * @param password RabbitMQ password
     * @param host RabbitMQ host address
     * @param virtualHost RabbitMQ virtual host
     */
    public Rabbitmq(String username, String password, String host, String virtualHost) {
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setHost(host);
        factory.setVirtualHost(virtualHost);
    }

    /**
     * Creates a connection to a RabbitMQ server.
     *
     * @return Connection: connection
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws TimeoutException
     * @throws IOException
     */
    public Connection createConnection()
            throws NoSuchAlgorithmException, KeyManagementException, TimeoutException, IOException {
        factory.useSslProtocol();
        return factory.newConnection();
    }

    /**
     * Close the connection.
     *
     * @throws IOException
     */
    public abstract void close() throws IOException;
}
