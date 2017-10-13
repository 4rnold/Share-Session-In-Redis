package com.arnold.core;

import com.arnold.api.SessionManager;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.*;

public class DistributedSessionHttpSessionWrapper implements HttpSession{

    private final String id;

    private final long createAt;

    private volatile long lastAccessedAt;

    /**
     * session max active
     */
    private int maxInactiveInterval;

    private final ServletContext servletContext;

    /**
     * the session manager
     */
    private final SessionManager sessionManager;

    /**
     * the new attributes of the current request
     */
    private final Map<String, Object> newAttributes = Maps.newHashMap();

    /**
     * the deleted attributes of the current request
     */
    private final Set<String> deleteAttribute = Sets.newHashSet();

    /**
     * session attributes store
     */
    private final Map<String, Object> sessionStore;

    /**
     * true if session invoke invalidate()
     */
    private volatile boolean invalid;

    /**
     * true if session attrs updated
     */
    private volatile boolean dirty;

    public DistributedSessionHttpSessionWrapper(String id, SessionManager sessionManager, ServletContext servletContext) {
        this.id = id;
        this.createAt = System.currentTimeMillis();
        this.servletContext = servletContext;
        this.sessionManager = sessionManager;
        this.sessionStore = sessionManager.loadById(id);
    }

    @Override
    public long getCreationTime() {
        return createAt;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public long getLastAccessedTime() {
        return lastAccessedAt;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    @Override
    public HttpSessionContext getSessionContext() {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        checkValid();
        //先从newAttributes中get速度快
        if (newAttributes.containsKey(name)){
            return newAttributes.get(name);
        } else if (deleteAttribute.contains(name)){
            return null;
        }
        return sessionStore.get(name);
    }

    private void checkValid() {
        if (invalid){
            throw new IllegalStateException();
        }
    }

    @Override
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkValid();
        Set<String> names = Sets.newHashSet(sessionStore.keySet());
        names.addAll(newAttributes.keySet());
        names.removeAll(deleteAttribute);
        return Collections.enumeration(names);
    }

    @Override
    public String[] getValueNames() {
        checkValid();
        Set<String> names = Sets.newHashSet(sessionStore.keySet());
        names.addAll(newAttributes.keySet());
        names.removeAll(deleteAttribute);
        return names.toArray(new String[0]);
    }

    @Override
    public void setAttribute(String name, Object value) {
        checkValid();
        if (value != null) {
            newAttributes.put(name, value);
            deleteAttribute.remove(name);
        } else {
            deleteAttribute.add(name);
            newAttributes.remove(name);
        }
        dirty = true;
    }

    @Override
    public void putValue(String name, Object value) {
        setAttribute(name,value);
    }

    @Override
    public void removeAttribute(String name) {
        checkValid();
        deleteAttribute.add(name);
        newAttributes.remove(name);
        dirty = true;
    }

    @Override
    public void removeValue(String name) {
        removeAttribute(name);
    }

    @Override
    public void invalidate() {
        invalid = true;
        dirty = true;
        sessionManager.deleteById(this.getId());
    }

    @Override
    public boolean isNew() {
        return true;
    }


    public boolean isValid(){
        return !invalid;
    }

    public boolean isDirty(){
        return dirty;
    }

    /**
     * get session attributes' snapshot
     * @return session attributes' map object
     */
    public Map<String, Object> snapshot() {
        Map<String, Object> snap = Maps.newHashMap();
        snap.putAll(sessionStore);
        snap.putAll(newAttributes);
        for (String name : deleteAttribute) {
            snap.remove(name);
        }
        return snap;
    }
}
