package com.arnold.core;

import com.arnold.api.SessionManager;
import com.arnold.coreV2.ShareSessionHttpServletRequestWrapper;
import com.arnold.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.*;

public class DistributedSessionHttpRequestWrapper extends HttpServletRequestWrapper {

    private final static Logger log = LoggerFactory.getLogger(DistributedSessionHttpRequestWrapper.class);

    //覆盖ServletRequestWrapper.request
    private final HttpServletRequest request;

    //持有response对象是为了写cookie
    private final HttpServletResponse response;

    //session的一系列操作
    private final SessionManager sessionManager;

    //自定义session
    private DistributedSessionHttpSessionWrapper session;

    private String sessionCookieName;

    private String cookieDomain;

    private String cookieContextPath;

    private int maxInactiveInterval;

    private int cookieMaxAge;



    public DistributedSessionHttpRequestWrapper(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, SessionManager sessionManager) {
        super(httpServletRequest);
        this.request = httpServletRequest;
        this.response = httpServletResponse;
        this.sessionManager = sessionManager;
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    @Override
    public HttpSession getSession() {
        return doGetSession(true);
    }

    /**
     * 父类调用此函数
     * @param create
     * @return
     */
    @Override
    public HttpSession getSession(boolean create) {
        return doGetSession(create);
    }

    public DistributedSessionHttpSessionWrapper currentSession() {
        return session;
    }

    private HttpSession doGetSession(boolean create) {
        if (session == null) {
            Cookie cookie = WebUtil.findCookie(this, getSessionCookieName());
            if (cookie != null) {
                String value = cookie.getValue();
                log.debug("discovery session id from cookie: {}", value);
                session = buildSession(value, false);
            } else {
                session = buildSession(create);
            }
        } else {
            log.debug("Session[{}] was existed.", session.getId());
        }
        return session;
    }

    private DistributedSessionHttpSessionWrapper buildSession(String id, boolean refresh) {
        DistributedSessionHttpSessionWrapper session = new DistributedSessionHttpSessionWrapper(id, sessionManager, request.getServletContext());
        session.setMaxInactiveInterval(maxInactiveInterval);
        if (refresh) {
            WebUtil.addCookie(this, response, getSessionCookieName(), id,
                    getCookieDomain(), getCookieContextPath(), cookieMaxAge, true);
        }
        return session;
    }

    /**
     * build a new session
     * @param create create session or not
     * @return session
     */
    private DistributedSessionHttpSessionWrapper buildSession(boolean create) {
        if (create) {
            session = buildSession(sessionManager.getSessionIdGenerator().generate(request), true);
            log.debug("Build new session[{}].", session.getId());
            return session;
        } else {
            return null;
        }
    }

    //    public void setSession(DistributedSessionHttpSessionWrapper session) {
//        this.session = session;
//    }

    public String getSessionCookieName() {
        return sessionCookieName;
    }

    public void setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
    }

    public String getCookieDomain() {
        return cookieDomain;
    }

    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    public String getCookieContextPath() {
        return cookieContextPath;
    }

    public void setCookieContextPath(String cookieContextPath) {
        this.cookieContextPath = cookieContextPath;
    }

    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public void setMaxInactiveInterval(int maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public int getCookieMaxAge() {
        return cookieMaxAge;
    }

    public void setCookieMaxAge(int cookieMaxAge) {
        this.cookieMaxAge = cookieMaxAge;
    }


}
