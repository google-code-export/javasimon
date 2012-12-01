package org.javasimon.jdbc;

import org.h2.jdbc.JdbcConnection;
import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

import java.sql.*;

/**
 * Unit test for {@link ConnectionProxyFactory}
 */
public class ConnectionProxyFactoryTest {
    private Connection connection;
    private final JdbcProxyFactoryFactory proxyFactoryFactory=new JdbcProxyFactoryFactory();
    private Connection wrappedConnection;
    @BeforeMethod
    public void before() throws SQLException {
        connection=H2DbUtil.before();
        H2DbUtil.beforeData(connection);
        SimonManager.clear();
        wrappedConnection=proxyFactoryFactory.wrapConnection("org.simon.jdbc.test", connection);
    }
// H2 Doesn't support JDBC4 operations
//    @Test
//    public void testIsWrapperFor() throws SQLException {
//        assertFalse(wrappedConnection.isWrapperFor(Statement.class));
//        assertFalse(wrappedConnection.isWrapperFor(Connection.class));
//    }
//    @Test
//    public void testUnwrap() throws SQLException {
//        assertTrue(wrappedConnection.unwrap(Connection.class) instanceof JdbcConnection);
//    }
@Test
    public void testClose()throws SQLException {
        Stopwatch stopwatch=((Stopwatch) SimonManager.getSimon("org.simon.jdbc.test.conn"));
        assertNotNull(stopwatch);
        assertEquals(stopwatch.getActive(),1);
        H2DbUtil.close(wrappedConnection);
        assertEquals(stopwatch.getActive(),0);
        connection=null;
    }
    @Test
    public void testCreateStatement()throws SQLException {
        Statement statement=wrappedConnection.createStatement();
        assertNotNull(statement);
    }
    @Test
    public void testPrepareStatement()throws SQLException {
        PreparedStatement statement=wrappedConnection.prepareStatement("select * from sample");
        assertNotNull(statement);
    }
    @AfterMethod
    public void after() throws SQLException {
        if (connection!=null) {
            H2DbUtil.afterData(connection);
            H2DbUtil.after(connection);
            connection=null;
        }
    }
}
