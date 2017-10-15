package com.arnold.session.redis;

import com.arnold.api.SessionManager;
import com.arnold.core.BaseSessionFilter;

import java.io.IOException;

public class RedisSessionFilter  extends BaseSessionFilter{
    @Override
    protected SessionManager createSessionManager() throws IOException {
        return new RedisSessionManager();
    }
}
