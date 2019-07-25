/*
 * Copyright (c) 2019, Axis Communications AB. All rights reserved.
 */

package com.axis.eiffel.rabbitmq.service;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Christian Bilevits, christian.bilevits@axis.com
 * @since 2019-07-24
 */
public enum ServiceProperties {
    R_EXCHANGE_NAME,
    R_EXCHANGE_TYPE,
    R_QUEUE_NAME,
    R_ROUTING_KEY,
    R_USERNAME,
    R_PASSWORD,
    R_HOST,
    R_VIRTUALHOST,
    S_EXCHANGE_NAME,
    S_EXCHANGE_TYPE,
    S_USERNAME,
    S_PASSWORD,
    S_HOST,
    S_VIRTUALHOST;

    private final Properties properties;

    private Object value;

    ServiceProperties() {
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            value = Objects.requireNonNull(properties.get(this.toString()));
        } catch (IOException e) {
            System.out.println("Unable to load config file.");
            System.exit(1);
        } catch (NullPointerException e) {
            System.out.println("All values in the config file must be set.");
            System.exit(1);
        }
    }

    public String getValue() {
        return (String) value;
    }
}
