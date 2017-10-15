package com.arnold.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

public class CookieUtil {
    private static final String KEY = "jkdflsffff()kldkjapfdY=::$B+DUOWAN";

    private HttpServletRequest request;

    private HttpServletResponse response;

    private static String domain = "andy.com";

    public CookieUtil(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * 保存cookie
     * @param request
     * @param response
     * @param name cookie名称
     * @param value cookie值
     * @param seconds 过期时间(单位秒) -1代表关闭浏览器时cookie即过期
     */
    public static void setCookie(HttpServletRequest request,
            HttpServletResponse response, String name, String value, int seconds, String domain) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            return;
        }
        Cookie cookie = new Cookie(name, value);
        cookie.setDomain(domain);
        cookie.setMaxAge(seconds); 
        cookie.setPath("/");
        response.setHeader("P3P",
                "CP='IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT'");
        response.addCookie(cookie);
    }

    /**
     * 过去cookie中保存的值
     * @param name
     * @return
     * @throws UnsupportedEncodingException
     */
    public String getCookieValue(String name)
            throws UnsupportedEncodingException {
        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (name.equalsIgnoreCase(cookies[i].getName())) {
                    return cookies[i].getValue();
                }
            }
        }
        return "";
    }

    /**
     * 设置加密的cookie
     * @param name cookie名称
     * @param value cookie值
     * @param seconds 过期时间 -1代表关闭浏览器时cookie即过期
     */
    public void setCheckCodeCookie(String name, String value, int seconds) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            return;
        }
        String md5Value = MD5Utils.getMD5(KEY + value);
        Cookie cookie = new Cookie(name, md5Value);
        //cookie.setDomain(domain);
        cookie.setMaxAge(seconds);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    /**
     * 校验加密的cookie
     * @param name
     * @param value
     * @return
     */
    public boolean checkCodeCookie(String name, String value) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value)) {
            return false;
        }
        boolean result = false;
        String cookieValue = getCookieValue(request, name);
        if (MD5Utils.getMD5(KEY + value).equalsIgnoreCase(
                cookieValue)) {
            result = true;
        }
        return result;
    }

    /**
     * 获取cookie值
     * @param request
     * @param name
     * @return
     */
    public static String getCookieValue(HttpServletRequest request, String name) {
        try {
            Cookie cookies[] = request.getCookies();
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    if (name.equalsIgnoreCase(cookies[i].getName())) {
                        return cookies[i].getValue();
                    }
                }
            }
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * 移除客户端的cookie
     * @param request
     * @param response
     * @param name
     */
    public static void removeCookieValue(HttpServletRequest request,
            HttpServletResponse response, String name) {
        try {
            Cookie cookies[] = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                for (Cookie cookie : cookies) {
                    if (name.equalsIgnoreCase(cookie.getName())) {
                        cookie = new Cookie(name, null);
                        cookie.setMaxAge(0);
                        cookie.setPath("/");
                        //cookie.setDomain(domain);
                        response.addCookie(cookie);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}