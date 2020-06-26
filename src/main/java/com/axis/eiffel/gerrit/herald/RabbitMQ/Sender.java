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

    /**
     * Set the exchange on the channel
     *
     * @param exchangeName Name of the exchange
     * @param type Exchange type
     * @param activeDeclare Whether the exchange should be actively declared
     * @throws IOException
     */
    public void setExchange(String exchangeName, String type, boolean activeDeclare) throws IOException {
        this.exchangeName = exchangeName;
        if (activeDeclare) {
            channel.exchangeDeclare(this.exchangeName, type, true);
        } else {
            channel.exchangeDeclarePassive(this.exchangeName);
        }
    }

    /**
     * Sends the message on the channel.
     *
     * @param routingKey Routing key
     * @param message Message
     * @throws IOException
     */
    public void send(String routingKey, String message) throws IOException {
        channel.basicPublish(this.exchangeName, routingKey, null, message.getBytes(UTF_8));
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
