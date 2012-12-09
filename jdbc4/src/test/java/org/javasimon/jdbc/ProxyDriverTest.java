package org.javasimon.jdbc;

import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.testng.Assert.*;

/**
 * Unit test for {@link ProxyDriver}
 */
public class ProxyDriverTest {
	@BeforeMethod
	public void before() throws SQLException {
		SimonManager.clear();
		H2DbUtil.loadDriver(ProxyDriver.class.getName());
	}

	@Test
    public void testConnectWithProperties() throws SQLException {
        Properties properties=new Properties();
        properties.setProperty("username","sa");
        properties.setProperty("password","");
        properties.setProperty("simon_prefix","org.simon.jdbc.test");
        Connection wrappedConnection=DriverManager.getConnection("jdbc:simonp:h2:mem:test", properties);
        Stopwatch stopwatch=((Stopwatch) SimonManager.getSimon("org.simon.jdbc.test.conn"));
        assertNotNull(stopwatch);
        assertEquals(stopwatch.getActive(), 1);
        H2DbUtil.close(wrappedConnection);
        assertEquals(stopwatch.getActive(), 0);
    }
	@Test
	public void testConnectWithUrl() throws SQLException {
		Connection wrappedConnection = DriverManager.getConnection("jdbc:simonp:h2:mem:test;simon_prefix=org.simon.jdbc.test", "sa", "");
		Stopwatch stopwatch = ((Stopwatch) SimonManager.getSimon("org.simon.jdbc.test.conn"));
		assertNotNull(stopwatch);
		assertEquals(stopwatch.getActive(), 1);
		H2DbUtil.close(wrappedConnection);
		assertEquals(stopwatch.getActive(), 0);
	}

	@Test
	public void testFull() throws SQLException {
		Connection wrappedConnection = DriverManager.getConnection("jdbc:simonp:h2:mem:test;simon_prefix=org.simon.jdbc.test", "sa", "");
		H2DbUtil.beforeData(wrappedConnection);
		for (Sample sample : Sample.loadAll(wrappedConnection)) {
			Sample.loadById(wrappedConnection, sample.getId());
		}
		H2DbUtil.afterData(wrappedConnection);
		H2DbUtil.close(wrappedConnection);
	}
}
