package org.javasimon.jdbc;

import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test for {@link PreparedStatementProxyHandler}
 */
public class PreparedStatementProxyHandlerTest {
    private Connection connection;
    private final JdbcProxyFactory proxyFactory =new JdbcProxyFactory();
    private Connection wrappedConnection;
    private PreparedStatement wrappedStatement;
    @BeforeMethod
    public void before() throws SQLException {
        connection=H2DbUtil.before();
        H2DbUtil.beforeData(connection);
        wrappedConnection= proxyFactory.wrapConnection("org.simon.jdbc.test", connection);
    }
    @Test
    public void testClose()throws SQLException {
        wrappedStatement =wrappedConnection.prepareStatement("select * from sample where id=?");
        Stopwatch stopwatch=((Stopwatch) SimonManager.getSimon("org.simon.jdbc.test.select_0efff369e5047a4b9fe9379c3b929b01dbca35a4.stmt"));
        assertNotNull(stopwatch);
        assertEquals(stopwatch.getActive(), 1);
        H2DbUtil.close(wrappedStatement);
        assertEquals(stopwatch.getActive(), 0);
        wrappedStatement=null;
    }
    @Test
    public void testExecute()throws SQLException {
        wrappedStatement =wrappedConnection.prepareStatement("select * from sample where id=?");
        wrappedStatement.setInt(1,1);
        H2DbUtil.close(wrappedStatement.executeQuery());
        Stopwatch stopwatch=((Stopwatch) SimonManager.getSimon("org.simon.jdbc.test.select_0efff369e5047a4b9fe9379c3b929b01dbca35a4.exec"));
        assertNotNull(stopwatch);
        assertEquals(stopwatch.getCounter(), 1);
        wrappedStatement.setInt(1,1);
        H2DbUtil.close(wrappedStatement.executeQuery());
        assertEquals(stopwatch.getCounter(), 2);
    }

    @AfterMethod
    public void after() throws SQLException {
        H2DbUtil.close(wrappedStatement);
        H2DbUtil.afterData(connection);
        H2DbUtil.after(connection);
        connection=null;
    }
}
