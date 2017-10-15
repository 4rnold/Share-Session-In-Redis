package com.arnold.core;

import com.arnold.api.Serializer;
import com.arnold.api.SessionIdGenerator;
import com.arnold.api.SessionManager;
import com.arnold.util.AbstractPropertiesReader;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public abstract class AbstractSessionManager implements SessionManager{
    private final static Logger log = LoggerFactory.getLogger(AbstractSessionManager.class);

    private static final String DEFAULT_PROPERTIES = "session.properties";

    protected SessionIdGenerator sessionIdGenerator;

    protected Serializer serializer;


    public AbstractSessionManager() throws IOException {
        this(DEFAULT_PROPERTIES);
    }
    public AbstractSessionManager(String propertiesFile) throws IOException {
        Properties props = AbstractPropertiesReader.read(propertiesFile);

        //初始化组件
        initSessionIdGenerator(props);
        initSerializer(props);

        //供子类初始化所需组件
        init(props);
    }

    private void initSessionIdGenerator(Properties props) {
        String sessionIdGeneratorClazz = (String) props.get("session.id.generator");
        if (Strings.isNullOrEmpty(sessionIdGeneratorClazz)) {
            sessionIdGenerator = new DefaultSessionIdGenerator();
        } else {
            try {
                sessionIdGenerator = (SessionIdGenerator) Class.forName(sessionIdGeneratorClazz).newInstance();
            } catch (Exception e) {
                log.error("failed to init session id generator: {}", Throwables.getStackTraceAsString(e));
            } finally {
                if (sessionIdGenerator == null) {
                    log.info("use default session id generator[DefaultSessionIdGenerator]");
                    sessionIdGenerator = new DefaultSessionIdGenerator();
                }
            }
        }
    }

    protected void init(Properties props){}

    protected void initSerializer(Properties props) {
        String sessionSerializer = (String)props.get("session.serializer");
        if (Strings.isNullOrEmpty(sessionSerializer)){
            serializer = new JsonSerializer();
        } else {
            try {
                serializer = (Serializer)(Class.forName(sessionSerializer).newInstance());
            } catch (Exception e) {
//                log.error("failed to init json generator: {}", Throwables.getStackTraceAsString(e));
            } finally {
                if (sessionIdGenerator == null){
//                    log.info("use default json serializer [JsonSerializer]");
                    serializer = new JsonSerializer();
                }
            }
        }
    }

}
