package com.arnold.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebUtil {
    private final static Logger log = LoggerFactory.getLogger(WebUtil.class);

    /**
     * Headers about client's IP
     */
    private static final String[] HEADERS_ABOUT_CLIENT_IP = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static void failureCookie(HttpServletRequest request, HttpServletResponse response, String name, String domain, String contextPath) {
        if (request != null && response != null) {
            addCookie(request, response, name, null, domain, contextPath, 0, true);
        }
    }

    /**
     * find cookie from request
     * @param request current request
     * @param name cookie name
     * @return cookie value or null
     */
    public static Cookie findCookie(HttpServletRequest request, String name) {
        if (request != null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals(name)) {
                        return cookie;
                    }
                }
            }
        }
        return null;
    }

    /**
     * add a cookie
     */
    public static void addCookie(HttpServletRequest request, HttpServletResponse response,
                                 String name, String value, String domain, String contextPath, int maxAge, boolean httpOnly) {
        if (request != null && response != null) {
            Cookie cookie = new Cookie(name, value);
            cookie.setMaxAge(maxAge);
            cookie.setSecure(request.isSecure());

            if (contextPath == null || contextPath.isEmpty()) {
                cookie.setPath("/");
            } else {
                cookie.setPath(contextPath);
            }

            if (domain != null && !domain.isEmpty()) {
                cookie.setDomain(domain);
            }

            if (httpOnly) {
                cookie.setHttpOnly(true);
            }

            response.addCookie(cookie);
            log.debug("Cookie update the sessionID.[name={},value={},maxAge={},httpOnly={},path={},domain={}]",
                    cookie.getName(), cookie.getValue(), cookie.getMaxAge(), httpOnly, cookie.getPath(),
                    cookie.getDomain());
        }
    }

    public static String getClientIpAddr(HttpServletRequest request) {
        for (String header : HEADERS_ABOUT_CLIENT_IP) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)){
                //return ip;
                String[] ips = ip.split(",");
                return ips[0];
            }
        }
        return request.getRemoteAddr();
    }
}
