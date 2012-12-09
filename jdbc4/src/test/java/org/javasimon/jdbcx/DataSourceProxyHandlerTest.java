package org.javasimon.jdbcx;

import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.javasimon.jdbc.H2DbUtil;
import org.javasimon.jdbc.JdbcProxyFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test for {@link DataSourceProxyHandler}
 */
public class DataSourceProxyHandlerTest {
    private DataSource dataSource;
    private final JdbcProxyFactory proxyFactory =new JdbcProxyFactory();
    private DataSource wrappedDataSource;
    private Connection wrappedConnection;
    @BeforeMethod
    public void before() throws SQLException {
        dataSource=H2DbUtil.createDataSource();
        wrappedDataSource= proxyFactory.wrapDataSource("org.simon.jdbc.test", dataSource);
        SimonManager.clear();
    }
    @Test
    public void testOpenClose()throws SQLException {
        wrappedConnection=wrappedDataSource.getConnection();
        Stopwatch stopwatch=((Stopwatch) SimonManager.getSimon("org.simon.jdbc.test.conn"));
        assertNotNull(stopwatch);
        assertEquals(stopwatch.getActive(),1);
        H2DbUtil.close(wrappedConnection);
        assertEquals(stopwatch.getActive(),0);
        wrappedConnection=null;
    }
    @AfterMethod
    public void after() throws SQLException {
        H2DbUtil.after(wrappedConnection);
		H2DbUtil.close(dataSource);
        wrappedConnection=null;
    }
}
