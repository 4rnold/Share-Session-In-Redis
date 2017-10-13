package com.arnold.core;


import com.arnold.api.SessionManager;
import com.arnold.util.WebUtil;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.html.HTMLParagraphElement;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public abstract class SessionFilter implements Filter {
    private final static Logger log = LoggerFactory.getLogger(SessionFilter.class);

    protected final static String SESSION_COOKIE_NAME = "sessionCookieName";

    protected final static String DEFAULT_SESSION_COOKIE_NAME = "sfid";

    /**
     * session cookie name
     */
    protected String sessionCookieName;

    protected final static String MAX_INACTIVE_INTERVAL = "maxInactiveInterval";

    /**
     * default 30 mins
     */
    protected final static int DEFAULT_MAX_INACTIVE_INTERVAL = 30 * 60;

    /**
     * max inactive interval
     */
    protected int maxInactiveInterval;

    /**
     * cookie domain
     */
    protected final static String COOKIE_DOMAIN = "cookieDomain";

    /**
     * cookie name
     */
    protected String cookieDomain;

    /**
     * cookie context path
     */
    protected final static String COOKIE_CONTEXT_PATH = "cookieContextPath";

    /**
     * default cookie context path
     */
    protected final static String DEFAULT_COOKIE_CONTEXT_PATH = "/";

    /**
     * cookie's context path
     */
    protected String cookieContextPath;

    /**
     * cookie max age
     */
    protected final static String COOKIE_MAX_AGE = "cookieMaxAge";

    /**
     * default cookie max age
     */
    protected final static int DEFAULT_COOKIE_MAX_AGE = -1;

    /**
     * cookie's life
     */
    protected int cookieMaxAge;

    protected SessionManager sessionManager;



    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            //模版方法
            sessionManager = createSessionManager();
            initAttrs(filterConfig);
        } catch (IOException e) {
            log.error("failed to init session filter.", e);
            throw new ServletException(e);
        }
    }

    /**
     * 设置属性
     * @param config
     */
    protected void initAttrs(FilterConfig config){
        String param  = config.getInitParameter(SESSION_COOKIE_NAME);
        sessionCookieName = Strings.isNullOrEmpty(param) ? DEFAULT_SESSION_COOKIE_NAME : param;

        param = config.getInitParameter(MAX_INACTIVE_INTERVAL);
        maxInactiveInterval = Strings.isNullOrEmpty(param) ? DEFAULT_MAX_INACTIVE_INTERVAL : Integer.parseInt(param);

        cookieDomain = config.getInitParameter(COOKIE_DOMAIN);

        param = config.getInitParameter(COOKIE_CONTEXT_PATH);
        cookieContextPath = Strings.isNullOrEmpty(param) ? DEFAULT_COOKIE_CONTEXT_PATH : param;

        param = config.getInitParameter(COOKIE_MAX_AGE);
        cookieMaxAge = Strings.isNullOrEmpty(param) ? DEFAULT_COOKIE_MAX_AGE : Integer.parseInt(param);

        log.info("SessionFilter (sessionCookieName={},maxInactiveInterval={},cookieDomain={})", sessionCookieName, maxInactiveInterval, cookieDomain);
    }


    protected abstract SessionManager createSessionManager() throws IOException;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof DistributedSessionHttpRequestWrapper){
            chain.doFilter(request,response);
            return;
        }

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        DistributedSessionHttpRequestWrapper httpRequest = new DistributedSessionHttpRequestWrapper(httpServletRequest,httpServletResponse,sessionManager);
        httpRequest.setSessionCookieName(sessionCookieName);
        httpRequest.setMaxInactiveInterval(maxInactiveInterval);
        httpRequest.setCookieDomain(cookieDomain);
        httpRequest.setCookieContextPath(cookieContextPath);
        httpRequest.setCookieMaxAge(cookieMaxAge);

        chain.doFilter(httpRequest,response);

        DistributedSessionHttpSessionWrapper session = httpRequest.currentSession();
        if (session != null) {
            if (!session.isValid()){
                //删除cookie
                log.debug("session is invalid, will delete.");
                WebUtil.failureCookie(httpRequest, httpServletResponse, sessionCookieName, cookieDomain, cookieContextPath);
            } else if (session.isDirty()) {
                //session有修改，和redis中不一致，所以dirty
                // should flush to store
                log.debug("try to flush session to session store");
                //snapshot用来持久化
                Map<String, Object> snapshot = session.snapshot();
                if (sessionManager.persist(session.getId(), snapshot, maxInactiveInterval)){
                    //成功
                } else {
                    //失败，清空cookie
                    WebUtil.failureCookie(httpRequest, httpServletResponse, sessionCookieName, cookieDomain, cookieContextPath);
                }
            } else {
                //新的访问，刷新过期时间
                sessionManager.expire(session.getId(),maxInactiveInterval);
            }
        }
    }

    @Override
    public void destroy() {
        sessionManager.destroy();
        log.debug("filter is destroy.");
    }
}
