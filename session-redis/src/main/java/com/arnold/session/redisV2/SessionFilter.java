package com.arnold.session.redisV2;

import com.arnold.apiV2.GlobalConstant;
import com.arnold.apiV2.ISessionService;
import com.arnold.coreV2.ShareSessionHttpServletRequestWrapper;
import com.arnold.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class SessionFilter extends OncePerRequestFilter{

    //private static final Logger LOG = Logger.getLogger(SessionFilter.class);

    @Autowired
    private ISessionService iSessionService;

    private String cookieDomain;


    public String getCookieDomain() {
        return cookieDomain;
    }

    public void setCookieDomain(String cookieDomain) {
        this.cookieDomain = cookieDomain;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {



        //从cookie中获取sessionId，如果此次请求没有sessionId，重写为这次请求设置一个sessionId
        String sid = CookieUtil.getCookieValue(request, GlobalConstant.JSESSIONID);
        if(StringUtils.isEmpty(sid) || sid.length() != 36){
            sid = UUID.randomUUID().toString();
            CookieUtil.setCookie(request, response, GlobalConstant.JSESSIONID, sid, 60 * 60, cookieDomain);
        }

        //交给自定义的HttpServletRequestWrapper处理
        filterChain.doFilter(new ShareSessionHttpServletRequestWrapper(sid, request, response, iSessionService), response);
    }

}