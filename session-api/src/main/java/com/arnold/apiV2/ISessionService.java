package com.arnold.apiV2;

import java.util.Map;

public interface ISessionService {

    Map<String, Object> getSession(String sid);

    void saveSession(String sid, Map<String, Object> sessionMap);

    void removeSession(String sid);
}
