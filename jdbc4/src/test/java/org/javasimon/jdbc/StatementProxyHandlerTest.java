package org.javasimon.jdbc;

import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit test for {@link StatementProxyHandler}
 */
public class StatementProxyHandlerTest {
    private Connection connection;
    private final JdbcProxyFactory proxyFactory =new JdbcProxyFactory();
    private Connection wrappedConnection;
    private Statement wrappedStatement;
    @BeforeMethod
    public void before() throws SQLException {
        connection=H2DbUtil.before();
        H2DbUtil.beforeData(connection);
        wrappedConnection= proxyFactory.wrapConnection("org.simon.jdbc.test", connection);
    }
    @Test
    public void testClose()throws SQLException {
        wrappedStatement =wrappedConnection.createStatement();
        Stopwatch stopwatch=((Stopwatch) SimonManager.getSimon("org.simon.jdbc.test.stmt"));
        assertNotNull(stopwatch);
        assertEquals(stopwatch.getActive(),1);
        H2DbUtil.close(wrappedStatement);
        assertEquals(stopwatch.getActive(),0);
        wrappedStatement=null;
    }
    @Test
    public void testExecute()throws SQLException {
        wrappedStatement =wrappedConnection.createStatement();
        wrappedStatement.execute("select * from sample");
        Stopwatch stopwatch=((Stopwatch) SimonManager.getSimon("org.simon.jdbc.test.select_d90991bb8c08a7c17c78439f05c47413a4ceb7cb.exec"));
        assertNotNull(stopwatch);
        assertEquals(stopwatch.getNote(), "select * from sample");
        assertEquals(stopwatch.getCounter(), 1);
    }
    @AfterMethod
    public void after() throws SQLException {
        H2DbUtil.close(wrappedStatement);
        H2DbUtil.afterData(connection);
        H2DbUtil.after(connection);
        connection=null;
    }
}
