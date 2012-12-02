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
 *
 */
public class CallableStatementProxyFactoryTest {
    private Connection connection;
    private final JdbcProxyFactoryFactory proxyFactoryFactory=new JdbcProxyFactoryFactory();
    private Connection wrappedConnection;
    private CallableStatement wrappedStatement;
    @BeforeMethod
    public void before() throws SQLException {
        connection=H2DbUtil.before();
        H2DbUtil.beforeData(connection);
        H2DbUtil.execute(connection, "CREATE ALIAS INSERT_SAMPLE FOR \""+Sample.class.getName()+".insert\";");
        wrappedConnection=proxyFactoryFactory.wrapConnection("org.simon.jdbc.test", connection);
    }
    @Test
    public void testClose()throws SQLException {
        wrappedStatement =wrappedConnection.prepareCall("{call INSERT_SAMPLE(?,?)}");
        Stopwatch stopwatch=((Stopwatch) SimonManager.getSimon("org.simon.jdbc.test.call_6f3e6f742a8d21e69aa9fa604e6623da814e8580.stmt"));
        assertNotNull(stopwatch);
        assertEquals(stopwatch.getActive(), 1);
        H2DbUtil.close(wrappedStatement);
        assertEquals(stopwatch.getActive(), 0);
        wrappedStatement=null;
    }
    @Test
    public void testExecute()throws SQLException {
        wrappedStatement =wrappedConnection.prepareCall("{call INSERT_SAMPLE(?,?)}");
        wrappedStatement.setInt(1,4);
        wrappedStatement.setString(2,"Be bop");
        wrappedStatement.execute();
        Stopwatch stopwatch=((Stopwatch) SimonManager.getSimon("org.simon.jdbc.test.call_6f3e6f742a8d21e69aa9fa604e6623da814e8580.exec"));
        assertNotNull(stopwatch);
        assertEquals(stopwatch.getCounter(), 1);
        wrappedStatement.setInt(1,5);
        wrappedStatement.setString(2,"a lula");
        wrappedStatement.execute();
        assertEquals(stopwatch.getCounter(), 2);
    }

    @AfterMethod
    public void after() throws SQLException {
        H2DbUtil.close(wrappedStatement);
        H2DbUtil.execute(connection, "DROP ALIAS INSERT_SAMPLE");
        H2DbUtil.afterData(connection);
        H2DbUtil.after(connection);
        connection=null;
    }
}
