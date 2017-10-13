package com.arnold.core;

import com.alibaba.fastjson.JSON;
import com.arnold.api.Serializer;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Json Serializer
 */
public final class JsonSerializer implements Serializer {

    private final static Logger log = LoggerFactory.getLogger(JsonSerializer.class);


    @Override
    public String serialize(Object o) {
        return JSON.toJSONString(o);
    }

    @Override
    public Map<String,Object> deserialize(String o) {
        return JSON.parseObject(o, Map.class);
    }
}
