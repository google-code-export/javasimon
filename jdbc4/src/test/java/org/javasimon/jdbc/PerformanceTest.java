package org.javasimon.jdbc;

import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.javasimon.jdbc4.Driver;

/**
 * Performance test
 */
public class PerformanceTest {
	private static final Logger LOGGER= LoggerFactory.getLogger(PerformanceTest.class);
	@BeforeClass
	public static void beforeClass() throws SQLException {
		H2DbUtil.loadDriver(H2DbUtil.DRIVER_CLASS_NAME);
		H2DbUtil.loadDriver(Driver.class.getName());
		H2DbUtil.loadDriver(ProxyDriver.class.getName());
	}
	@Test
	public void testPerformanceNoSimon() throws SQLException {
		testPerformance("nosimon","jdbc:h2:mem:test");
	}
	@Test
	public void testPerformanceNoProxy() throws SQLException {
		testPerformance("noproxy", "jdbc:simon:h2:mem:test;simon_prefix=org.simon.jdbc.test");
	}
	@Test
	public void testPerformanceProxyDefault() throws SQLException {
		testPerformanceProxy("default");
	}
	@Test
	public void testPerformanceProxyCache() throws SQLException{
		testPerformanceProxy("cache");
	}
	@Test
	public void testPerformanceProxyCGLib() throws SQLException{
		testPerformanceProxy("cglib");
	}

	private void testPerformanceProxy(String proxyMode) throws SQLException {
		testPerformance(proxyMode, "jdbc:simonp:h2:mem:test;simon_prefix=org.simon.jdbc.test;simon_sqlcache_size=100;simon_proxy_mode="+proxyMode);
	}
	private void testPerformance(String testId, String jdbcUrl) throws SQLException {
		Split split= SimonManager.getStopwatch("org.simon.jdbc.proxy." + testId).start();
		for (int i = 0; i < 10; i++) {
			Connection wrappedConnection = DriverManager.getConnection(jdbcUrl, "sa", "");
			H2DbUtil.beforeData(wrappedConnection);
			H2DbUtil.fillData(wrappedConnection, 1000);
			for (Sample sample : Sample.loadAll(wrappedConnection)) {
				Sample.loadById(wrappedConnection, sample.getId());
			}
			H2DbUtil.afterData(wrappedConnection);
			H2DbUtil.close(wrappedConnection);
		}
		split.stop();
		LOGGER.info(split.toString());
	}

}
