package com.arnold.coreV2;

import com.arnold.apiV2.ISessionService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class ShareSessionHttpServletRequestWrapper extends
        javax.servlet.http.HttpServletRequestWrapper {

    private HttpSession session;

    private HttpServletRequest request;

    private HttpServletResponse response;

    private String sid = "";

    private  ISessionService sessionService;


    public ShareSessionHttpServletRequestWrapper(String sid, HttpServletRequest request) {
        super(request);
        this.sid = sid;
    }

    public ShareSessionHttpServletRequestWrapper(String sid, HttpServletRequest request, HttpServletResponse response, ISessionService sessionService) {
        super(request);
        this.request = request;
        this.response = response;
        this.sid = sid;
        this.sessionService = sessionService;
        if (this.session == null) {
            this.session = new HttpSessionWrapper(sid, super.getSession(false), request, response, sessionService);
        }
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (this.session == null) {
            if (create) {
                this.session = new HttpSessionWrapper(this.sid, super.getSession(create), this.request, this.response,sessionService);
                return this.session;
            } else {
                return null;
            }
        }
        return this.session;
    }

    @Override
    public HttpSession getSession() {
        if (this.session == null) {
            this.session = new HttpSessionWrapper(this.sid, super.getSession(), this.request, this.response,sessionService);
        }
        return this.session;
    }

}