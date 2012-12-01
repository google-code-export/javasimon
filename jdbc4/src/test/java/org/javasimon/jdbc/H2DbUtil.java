package org.javasimon.jdbc;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import org.h2.Driver;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;

/**
 * Util methods to manage test database: open/release connections...
 */
public class H2DbUtil {

    public static final String DRIVER_CLASS_NAME = "org.h2.Driver";
    public static final String URL = "jdbc:h2:mem:test";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";

    private static void loadDriver() throws SQLException {
        loadDriver(DRIVER_CLASS_NAME);
    }
    public static void loadDriver(String driverClassName) throws SQLException {
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver not found");
        }
    }

    public static DataSource createDataSource() throws SQLException {
        loadDriver();
        BoneCPConfig config = new BoneCPConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        return new BoneCPDataSource(config);
    }

    private static Connection createConnection() throws SQLException {
        loadDriver();
        Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        return connection;
    }

    public static Connection before() throws SQLException {
        return createConnection();
    }

    public static void beforeData(Connection connection) throws SQLException {
        Sample.createTable(connection);
        new Sample(1, "Foo").insert(connection);
        new Sample(2, "Bar").insert(connection);
        new Sample(3, "Qix").insert(connection);
    }

    public static void afterData(Connection connection) throws SQLException {
        Connection lConnection=connection;
        boolean newConnection=false;
        if (lConnection==null) {
            lConnection=createConnection();
        }
        Sample.dropTable(lConnection);
        if (newConnection) {
            close(lConnection);
        }
    }

    public static void after(Connection connection) throws SQLException {
        close(connection);
    }

    public static void close(Object... objects) {
        for (Object object : objects) {
            if (object != null) {
                try {
                    Method method = object.getClass().getMethod("close", new Class[0]);
                    method.invoke(object);
                } catch (Exception e) {
                }
            }
        }
    }
}
