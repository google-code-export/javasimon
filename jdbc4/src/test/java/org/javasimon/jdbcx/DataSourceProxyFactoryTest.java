package org.javasimon.jdbcx;

import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.javasimon.jdbc.H2DbUtil;
import org.javasimon.jdbc.JdbcProxyFactoryFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test for {@link org.javasimon.jdbcx.DataSourceProxyFactory}
 */
public class DataSourceProxyFactoryTest {
    private DataSource dataSource;
    private final JdbcProxyFactoryFactory proxyFactoryFactory=new JdbcProxyFactoryFactory();
    private DataSource wrappedDataSource;
    private Connection wrappedConnection;
    @BeforeMethod
    public void before() throws SQLException {
        dataSource=H2DbUtil.createDataSource();
        wrappedDataSource=proxyFactoryFactory.wrapDataSource("org.simon.jdbc.test", dataSource);
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
        wrappedConnection=null;
    }
}
