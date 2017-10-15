package com.arnold.session.redis;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.util.Pool;

import java.util.HashSet;
import java.util.Properties;

public class RedisExecutor {

    private volatile Pool<Jedis> jedisPool;

    public RedisExecutor(JedisPoolConfig config, boolean sentinel, Properties props) {
        if (sentinel){
            String sentinelProp = props.getProperty("session.redis.sentinel.hosts");
            Iterable<String> hosts = Splitter.on(',').trimResults().omitEmptyStrings().split(sentinelProp);
            HashSet<String> sentinelHosts = Sets.newHashSet(hosts);
            String masterName = props.getProperty("session.redis.sentinel.master.name");
            this.jedisPool = new JedisSentinelPool(masterName, sentinelHosts, config);
        } else {
            String redisHost = props.getProperty("session.redis.host");
            int redisPort = Integer.parseInt(props.getProperty("session.redis.port"));
            this.jedisPool = new JedisPool(config, redisHost, redisPort);
        }
    }

    /**
     * execute a redis operation
     * @param cb callback
     * @param <V> return type
     * @return result
     */
    public <V> V execute(RedisCallback<V> cb) {
        Jedis jedis = jedisPool.getResource();
        boolean success = true;
        try {
            //将jedis传入 回调方法中。
            return cb.execute(jedis);
        } catch (JedisException e) {
            success = false;
            if (jedis != null) {
                jedisPool.returnBrokenResource(jedis);
            }
            throw e;
        } finally {
            if (success) {
                jedisPool.returnResource(jedis);
            }
        }
    }

    public Pool<Jedis> getJedisPool() {
        return jedisPool;
    }
}
