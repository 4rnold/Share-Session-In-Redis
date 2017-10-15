package com.arnold.session.redis;

import com.arnold.api.SessionIdGenerator;
import com.arnold.core.AbstractSessionManager;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

public class RedisSessionManager extends AbstractSessionManager {

    private volatile RedisExecutor executor;

    private String sessionPrefix;

    private static final String SENTINEL_MODE = "sentinel";


    public RedisSessionManager() throws IOException {
    }

    @Override
    protected void init(Properties props) {
        this.sessionPrefix = props.getProperty("session.redis.prefix","rsession");
        initJedisPool(props);
    }

    private void initJedisPool(Properties props) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setTestOnBorrow(true);
        Integer maxIdle = Integer.parseInt(props.getProperty("session.redis.pool.max.idle", "2"));
        config.setMaxIdle(maxIdle);

        Integer maxTotal = Integer.parseInt(props.getProperty("session.redis.pool.max.total", "5"));
        config.setMaxTotal(maxTotal);

        final String mode = props.getProperty("session.redis.mode");
        if (Objects.equal(mode, SENTINEL_MODE)){
            // sentinel
            this.executor = new RedisExecutor(config, true, props);
        } else {
            // standalone
            this.executor = new RedisExecutor(config, false, props);
        }
    }

    /**
     * snapshot 持久化
     * @param id
     * @param snapshot
     * @param maxInactiveInterval
     * @return
     */
    @Override
    public Boolean persist(String id, Map<String, Object> snapshot, int maxInactiveInterval) {
        final String sid = sessionPrefix + ":" + id;
        try {
            this.executor.execute(new RedisCallback<Void>() {
                @Override
                public Void execute(Jedis jedis) {
                    if (snapshot.isEmpty()) {
                        jedis.del(sid);
                    } else {
                        jedis.setex(sid,maxInactiveInterval,serializer.serialize(snapshot));
                    }
                    return null;
                }
            });
            return Boolean.TRUE;
        } catch (Exception e){
            return Boolean.FALSE;
        }
    }

    @Override
    public Map<String, Object> loadById(String id) {
        final String sid = sessionPrefix + ":" + id;
        try {
            return this.executor.execute(new RedisCallback<Map<String, Object>>() {
                @Override
                public Map<String, Object> execute(Jedis jedis) {
                    String session = jedis.get(sid);
                    if (!Strings.isNullOrEmpty(session)) {
                        return serializer.deserialize(session);
                    }
                    return Collections.emptyMap();
                }
            });
        } catch (Exception e) {
//            log.error("failed to load session(key={}), cause:{}", sid, Throwables.getStackTraceAsString(e));
            throw new RuntimeException("load session failed", e);
        }
    }

    @Override
    public void deleteById(String id) {
        final String sid = sessionPrefix + ":" + id;
        try {
            this.executor.execute(new RedisCallback<Void>() {
                @Override
                public Void execute(Jedis jedis) {
                    jedis.del(sid);
                    return null;
                }
            });
        } catch (Exception e) {
//            log.error("failed to delete session(key={}) in redis,cause:{}", sid, Throwables.getStackTraceAsString(e));
        }
    }

    @Override
    public void expire(String sid, int maxInactiveInterval) {
        final String sessionId = sessionPrefix + ":" + sid;
        try {
            this.executor.execute(new RedisCallback<Void>() {
                @Override
                public Void execute(Jedis jedis) {
                    jedis.expire(sessionId, maxInactiveInterval);
                    return null;
                }
            });
        } catch (Exception e) {
//            log.error("failed to refresh expire time session(key={}) in redis,cause:{}",sessionId, Throwables.getStackTraceAsString(e));
        }
    }

    @Override
    public void destroy() {
        if (executor != null) {
            executor.getJedisPool().destroy();
        }
    }

    @Override
    public SessionIdGenerator getSessionIdGenerator() {
        return this.sessionIdGenerator;
    }
}
