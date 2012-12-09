package org.javasimon.jdbc;

import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test for {@link ResultSetProxyHandler}
 */
public class ResultSetProxyHandlerTest {
    private Connection connection;
    private final JdbcProxyFactory proxyFactory = new JdbcProxyFactory();
    private Connection wrappedConnection;
    private PreparedStatement wrappedStatement;

    @BeforeMethod
    public void before() throws SQLException {
        connection = H2DbUtil.before();
        H2DbUtil.beforeData(connection);
        wrappedConnection = proxyFactory.wrapConnection("org.simon.jdbc.test", connection);
        SimonManager.clear();
    }

    @Test
    public void testClose() throws SQLException {
        wrappedStatement = wrappedConnection.prepareStatement("select * from sample where id=?");
        wrappedStatement.setInt(1, 2);
        ResultSet resultSet = wrappedStatement.executeQuery();
        Stopwatch stopwatch = ((Stopwatch) SimonManager.getSimon("org.simon.jdbc.test.select_0efff369e5047a4b9fe9379c3b929b01dbca35a4.rset"));
        assertNotNull(stopwatch);
        assertEquals(stopwatch.getActive(), 1);
        H2DbUtil.close(resultSet);
        assertEquals(stopwatch.getActive(), 0);
        wrappedStatement = null;
    }

    @Test
    public void testNext() throws SQLException {
        Sample.loadAll(wrappedConnection);
        Stopwatch stopwatch = ((Stopwatch) SimonManager.getSimon("org.simon.jdbc.test.select_0e6401e07280dd48ed998834c17ebe0db1d31e51.rset"));
        assertNotNull(stopwatch);
        assertEquals(stopwatch.getNote(), "select id,name from sample order by id asc");
        assertEquals(stopwatch.getCounter(), 1);
    }

    @AfterMethod
    public void after() throws SQLException {
        H2DbUtil.close(wrappedStatement);
        H2DbUtil.afterData(connection);
        H2DbUtil.after(connection);
        connection = null;
    }

}
