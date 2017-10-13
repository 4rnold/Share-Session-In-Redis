package com.arnold.session.redis;

import com.arnold.api.SessionManager;
import com.arnold.core.SessionFilter;

import java.io.IOException;

public class RedisSessionFilter  extends SessionFilter{
    @Override
    protected SessionManager createSessionManager() throws IOException {
        return new RedisSessionManager();
    }
}
