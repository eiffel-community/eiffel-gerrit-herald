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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
    S_EXCHANGE_ACTIVE,
    S_USERNAME,
    S_PASSWORD,
    S_HOST,
    S_VIRTUALHOST,
    DATABASE_URL,
    DATABASE_PORT;

    private final Properties properties;
    private final Logger log = LoggerFactory.getLogger(Service.class);
    private Object value;

    ServiceProperties() {
        properties = new Properties();
        try {
            InputStream propertyStream;
            String customPropertyFileLocation = System.getProperty("herald.properties");
            if (customPropertyFileLocation != null) {
                propertyStream = new FileInputStream(customPropertyFileLocation);
            } else {
                propertyStream = getClass().getClassLoader().getResourceAsStream("config.properties");
            }
            properties.load(propertyStream);
            value = Objects.requireNonNull(properties.get(this.toString()));
        } catch (IOException e) {
            log.error("Unable to load config file. " + e.getMessage() + "\nCause: " + e.getCause());
            System.exit(1);
        } catch (NullPointerException e) {
            log.warn("All values in the config file must be set. " + e.getMessage() + "\nCause: " + e.getCause());
            System.exit(1);
        }
    }

    /**
     * Get value of property
     *
     * @return String: Property value
     */
    public String getValue() {
        return (String) value;
    }
}
