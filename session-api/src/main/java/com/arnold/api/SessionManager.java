package com.arnold.api;

import java.util.Map;

public interface SessionManager {

    Boolean persist(final String id, final Map<String,Object> snapshot, final int maxInactiveInterval);

    Map<String,Object> loadById(String id);

    void deleteById(String id);

    void expire(String sid, final int maxInactiveInterval);

    void destroy();

    SessionIdGenerator getSessionIdGenerator();
}
