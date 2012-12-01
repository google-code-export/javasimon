package org.javasimon.jdbc;

import org.javasimon.jdbc4.Driver;
import org.javasimon.jdbc4.DriverUrl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * JDBC Driver based on proxies
 */
public class ProxyDriver extends Driver {
    /**
     * Property name for configuring Proxy factory
     */
    public static final String SIMON_PROXY_FACTORY="simon_proxy_factory";
    /**
     * Prefix used for URLs
     */
    public static final String URL_PREFIX = "jdbc:simonp:";
    static {
        try {
            DriverManager.registerDriver(new ProxyDriver());
        } catch (Exception e) {
            // don't know what to do yet, maybe throw RuntimeException ???
            e.printStackTrace();
        }
    }
    @Override
    protected Connection wrapConnection(Connection realConnection, DriverUrl url) {
        final String proxyFactoryClassName=url.getProperty(SIMON_PROXY_FACTORY);
        JdbcProxyFactoryFactory proxyFactory;
        if (proxyFactoryClassName==null) {
            proxyFactory=new JdbcProxyFactoryFactory();
        } else {
            try {
                proxyFactory=(JdbcProxyFactoryFactory)
                        Class.forName(proxyFactoryClassName).newInstance();
            } catch (Exception e) {
                proxyFactory=new JdbcProxyFactoryFactory();
            }
        }
        return proxyFactory.wrapConnection(url.getPrefix(), realConnection);
    }
    public String getUrlPrefix() {
        return ProxyDriver.URL_PREFIX;
    }
}
