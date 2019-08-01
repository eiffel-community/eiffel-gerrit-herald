/*
 * Copyright (c) 2019, Axis Communications AB. All rights reserved.
 */
package com.axis.eiffel.rabbitmq.service.Jedis;

import com.axis.eiffel.rabbitmq.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author Christian Bilevits, christian.bilevits@axis.com
 * @since 2019-07-31
 */
public class RelationJedis {

    private final static Logger log = LoggerFactory.getLogger(Service.class);

    private JedisPool jedisPool;

    public RelationJedis(String url, int port) {
        jedisPool = new JedisPool(new JedisPoolConfig(), url, port);
    }

    public String getEiffelId(String changeKey) {
        Jedis jedis = null;
        String eiffelId;
        try {
            jedis = getJedis();
            eiffelId = jedis.get(changeKey);
        } finally {
            assert jedis != null;
            jedis.close();
        }
        return eiffelId;
    }

    public void insert(String changeKey, String eiffelId) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.append(changeKey, eiffelId);
            save(jedis);
        } finally {
            assert jedis != null;
            jedis.close();
        }
    }

    public void remove(String changeKey) {
        Jedis jedis = null;
        try {
            jedis = getJedis();
            jedis.del(changeKey);
            save(jedis);
        } finally {
            assert jedis != null;
            jedis.close();
        }
    }

    public boolean contains(String changeKey) {
        Jedis jedis = null;
        boolean exist;
        try {
            jedis = getJedis();
            exist = jedis.exists(changeKey);
        } finally {
            assert jedis != null;
            jedis.close();
        }
        return exist;
    }

    public void close() throws InterruptedException {
        log.info("Saving before closing...");
        Thread.sleep(1000);
        getJedis().save();
        jedisPool.close();
    }

    private Jedis getJedis() {
        return jedisPool.getResource();
    }

    private void save(Jedis jedis) {
        if (jedis.lastsave() < (System.currentTimeMillis() / 1000L)) {
            jedis.bgsave();
            log.info("Background save started... Last time saved: " + jedis.lastsave());
        }
    }
}
