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
package com.axis.eiffel.gerrit.herald;

import com.axis.eiffel.gerrit.herald.Jedis.RelationJedis;
import com.axis.eiffel.gerrit.herald.RabbitMQ.Receiver;
import com.axis.eiffel.gerrit.herald.RabbitMQ.Sender;
import com.axis.eiffel.gerrit.lib.formatter.EiffelEventService;
import com.axis.eiffel.gerrit.lib.formatter.EventException;
import com.ericsson.eiffel.remrem.semantics.validator.EiffelValidationException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.DeliverCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

import static com.axis.eiffel.gerrit.herald.ServiceProperties.DATABASE_PORT;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.DATABASE_URL;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.R_EXCHANGE_NAME;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.R_HOST;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.R_PASSWORD;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.R_QUEUE_NAME;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.R_ROUTING_KEY;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.R_USERNAME;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.R_VIRTUALHOST;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.S_EXCHANGE_ACTIVE;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.S_EXCHANGE_NAME;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.S_EXCHANGE_TYPE;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.S_HOST;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.S_PASSWORD;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.S_USERNAME;
import static com.axis.eiffel.gerrit.herald.ServiceProperties.S_VIRTUALHOST;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Runnable service for receiving Gerrit events, converting to eiffel and sending back to a RabbitMQ exchange.
 *
 * @author Christian Bilevits, christian.bilevits@axis.com
 * @since 2019-07-22
 */
public class Service {

    private final static Logger log = LoggerFactory.getLogger(Service.class);

    private final static EiffelEventService eiffelEventService = new EiffelEventService();
    private static Sender sender;
    private static Receiver receiver;
    private static Gson gson = new Gson();
    private static RelationJedis relationJedis;

    public static void main(String[] args) {
        initConnections();

        relationJedis = new RelationJedis(DATABASE_URL.getValue(), Integer.parseInt(DATABASE_PORT.getValue()));

        DeliverCallback deliverCallback = ((consumerTag, delivery) -> {
            JsonObject message = new JsonParser().parse(new String(delivery.getBody(), UTF_8)).getAsJsonObject();
            JsonObject eiffelEvent = null;
            try {
                eiffelEvent = generateEiffel(message);
            } catch (Exception ignored) {
            } finally {
                if (eiffelEvent != null) {
                    sender.send(eiffelEventService.getEiffelType(eiffelEvent), gson.toJson(eiffelEvent));
                }
                receiver.ack(delivery);
            }
        });


        try {
            receiver.consume(false, deliverCallback, consumerTag -> {
            });
        } catch (IOException e) {
            log.error("Consumer failed: " + Arrays.toString(e.getStackTrace()));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Closing receiver RabbitMQ connection to " + R_HOST.getValue() + "...");
                receiver.close();
                log.info("Closing sender RabbitMQ connection to " + S_HOST.getValue() + "...");
                sender.close();
                log.info("Done.");
                log.info("Closing Redis connection...");
                relationJedis.close();
            } catch (IOException e) {
                log.warn("Failed to close RabbitMQ: " + e.getMessage() + "\nCause: " + e.getCause());
            } catch (InterruptedException e) {
                log.warn("Failed to close Redis " + e.getMessage() + "\nCause: " + e.getCause());
            }
            log.info("Done. Closing service...");
        }));
    }

    private static JsonObject generateEiffel(JsonObject gerritEvent)
            throws EiffelValidationException, EventException {
        String changeKey = gerritEvent.get("changeKey").toString();
        JsonObject eiffelEvent;
        if (relationJedis.contains(changeKey)) {
            eiffelEvent = eiffelEventService.convertToEiffel(gerritEvent, relationJedis.getEiffelId(changeKey));
            relationJedis.remove(changeKey);
        } else {
            eiffelEvent = eiffelEventService.convertToEiffel(gerritEvent);
            relationJedis.insert(changeKey, eiffelEventService.getEiffelEventId(eiffelEvent));
        }
        return eiffelEvent;
    }


    private static void initConnections() {
        try {
            sender = new Sender(S_USERNAME.getValue(), S_PASSWORD.getValue(), S_HOST.getValue(),
                    S_VIRTUALHOST.getValue());
            receiver = new Receiver(R_USERNAME.getValue(), R_PASSWORD.getValue(), R_HOST.getValue(),
                    R_VIRTUALHOST.getValue());
            sender.setExchange(S_EXCHANGE_NAME.getValue(), S_EXCHANGE_TYPE.getValue(),
                    Boolean.parseBoolean(S_EXCHANGE_ACTIVE.getValue()));
            receiver.setQueue(R_QUEUE_NAME.getValue(), R_EXCHANGE_NAME.getValue(), R_ROUTING_KEY.getValue());
        } catch (IOException | TimeoutException | NoSuchAlgorithmException | KeyManagementException e) {
            log.error("Could not start connection to RabbitMQ: " + e.getMessage() + "\nCause: " + e.getCause());
        }
    }
}