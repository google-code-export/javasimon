package org.javasimon.jdbc;

import org.javasimon.SimonManager;
import org.javasimon.jdbc4.Driver;
import org.javasimon.jdbc4.DriverUrl;
import org.javasimon.proxy.CGLibProxyFactory;
import org.javasimon.proxy.CacheReflectProxyFactory;
import org.javasimon.proxy.ProxyFactory;
import org.javasimon.proxy.ReflectProxyFactory;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * JDBC Driver based on proxies
 */
public class ProxyDriver extends Driver {
	/**
	 * Property name for configuring Proxy factory
	 */
	public static final String SIMON_PROXY_FACTORY = "simon_proxy_factory";
	/**
	 * Property name for configuring Proxy factory
	 */
	public static final String SIMON_PROXY_MODE = "simon_proxy_mode";
	/**
	 * Property name for configuring SQL Cache Size
	 */
	public static final String SIMON_SQL_CACHE_SIZE = "simon_sqlcache_size";
	/**
	 * Property name for configuring SQL Cache Size
	 */
	public static final String SIMON_SQL_CACHE_TTL = "simon_sqlcache_ttl";
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
		final String proxyFactoryClassName = url.getProperty(SIMON_PROXY_FACTORY);
		JdbcProxyFactory proxyFactory;
		if (proxyFactoryClassName == null) {
			// Choose proxy factory from proxy mode
			final String proxyMode = url.getProperty(SIMON_PROXY_MODE);
			ProxyFactory proxyFactory1;
			if (proxyMode == null || proxyMode.equalsIgnoreCase("default")) {
				proxyFactory1 = new ReflectProxyFactory();
			} else if (proxyMode.equalsIgnoreCase("cache")) {
				proxyFactory1 = new CacheReflectProxyFactory();
			} else if (proxyMode.equalsIgnoreCase("cglib")) {
				proxyFactory1 = new CGLibProxyFactory();
			} else {
				throw new IllegalArgumentException("Invalid simon_proxy_mode. Expected default, cache or cglib was " + proxyMode);
			}
			// Configure SQL Cache
			String sqlCacheSizeS = url.getProperty(SIMON_SQL_CACHE_SIZE);
			Integer sqlCacheSizeI = sqlCacheSizeS == null ? null : Integer.valueOf(sqlCacheSizeS);
			String sqlCacheTtlS = url.getProperty(SIMON_SQL_CACHE_TTL);
			Long sqlCacheTtlL = sqlCacheTtlS == null ? null : Long.valueOf(sqlCacheTtlS);
			proxyFactory = new JdbcProxyFactory(SimonManager.manager(), proxyFactory1, sqlCacheSizeI, sqlCacheTtlL);
		} else {
			try {
				proxyFactory = (JdbcProxyFactory)
						Class.forName(proxyFactoryClassName).newInstance();
			} catch (Exception e) {
				proxyFactory = new JdbcProxyFactory();
			}
		}
		return proxyFactory.wrapConnection(url.getPrefix(), realConnection);
	}

	public String getUrlPrefix() {
		return ProxyDriver.URL_PREFIX;
	}
}
