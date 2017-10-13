package com.arnold.core;

import com.arnold.api.SessionIdGenerator;
import com.arnold.util.WebUtil;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Default Session ID Generator
 */
public class DefaultSessionIdGenerator implements SessionIdGenerator {

    public static final Character SEPARATOR ='Z';

    private final String hostIpMd5;

    public DefaultSessionIdGenerator() {
        String hostIp;
        try {
            hostIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            hostIp = UUID.randomUUID().toString();
        }
        hostIpMd5 = Hashing.md5().hashString(hostIp, Charsets.UTF_8).toString().substring(0,8);
    }

    /**
     * md5客户端 + md5服务端 + 时间16进制 + uuid
     * @param request 请求对象
     * @return
     */
    @Override
    public String generate(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder(30);
        //ip做md5 截取8个字符
        String remoteIpMd5 =  Hashing.md5().hashString(WebUtil.getClientIpAddr(request), Charsets.UTF_8).toString().substring(0,8);
        builder.append(remoteIpMd5).append(SEPARATOR)
               .append(hostIpMd5).append(SEPARATOR)
               .append(Long.toHexString(System.currentTimeMillis())).append(SEPARATOR)
               .append(UUID.randomUUID().toString().substring(0,4));
        return builder.toString();
    }
}