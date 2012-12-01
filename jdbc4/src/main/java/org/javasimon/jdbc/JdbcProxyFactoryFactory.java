package org.javasimon.jdbc;

import org.javasimon.*;
import org.javasimon.jdbc4.SqlNormalizer;
import org.javasimon.jdbcx.DataSourceProxyFactory;
import org.javasimon.jdbcx.PooledConnectionProxyFactory;
import org.javasimon.jdbcx.XADataSourceProxyFactory;

import javax.sql.*;
import javax.sql.rowset.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

/**
 * Factory of {@code JdbcProxyFactory} sub classes, central class of the JDBC module.
 * It can be used to wrap any JDBC component (connection, statement, result set...).
 */
public class JdbcProxyFactoryFactory {
    private final Manager manager;

    public JdbcProxyFactoryFactory() {
        this.manager= SimonManager.manager();
    }

    public JdbcProxyFactoryFactory(Manager manager) {
        this.manager = manager;
    }

    public Split startStopwatch(String name) {
        return manager.getStopwatch(name).start();
    }

    private Split startPooledConnectionStopwatch(String connectionFactoryName) {
        return startStopwatch(connectionFactoryName + ".pooledconn");
    }

    public DataSource wrapDataSource(String connectionFactoryName, DataSource wrappedDataSource) {
        return new DataSourceProxyFactory(wrappedDataSource, connectionFactoryName, this).newProxy();
    }

    public XADataSource wrapXADataSource(String connectionFactoryName, XADataSource wrappedDataSource) {
        return new XADataSourceProxyFactory(wrappedDataSource, connectionFactoryName, this).newProxy();
    }

    public PooledConnection wrapPooledConnection(String connectionFactoryName, PooledConnection wrappedConnection) {
        return new PooledConnectionProxyFactory<PooledConnection>(wrappedConnection, PooledConnection.class, connectionFactoryName, this, startPooledConnectionStopwatch(connectionFactoryName)).newProxy();
    }

    public XAConnection wrapXAConnection(String connectionFactoryName, XAConnection wrappedConnection) {
        return new PooledConnectionProxyFactory<XAConnection>(wrappedConnection, XAConnection.class, connectionFactoryName, this, startPooledConnectionStopwatch(connectionFactoryName)).newProxy();
    }

    private Split startConnectionStopwatch(String connectionFactoryName, String suffix) {
        return startStopwatch(connectionFactoryName + suffix);
    }
    private Split startConnectionStopwatch(String connectionFactoryName) {
        return startConnectionStopwatch(connectionFactoryName, ".conn");
    }

    public Connection wrapConnection(String connectionFactoryName, Connection wrappedConnection) {
        return new ConnectionProxyFactory(wrappedConnection, connectionFactoryName, this, startConnectionStopwatch(connectionFactoryName)).newProxy();
    }

    private Split startStatementStopwatch(String connectionFactoryName) {
        return startConnectionStopwatch(connectionFactoryName,".stmt");
    }
    private static void toHexa(byte[] input, StringBuilder outputBuilder) {
        for(byte b:input) {
            String h=Integer.toString(( b & 0xff ) + 0x100,16).substring(1);
            outputBuilder.append(h);
        }
    }
    private static void toSha1(String input, StringBuilder outputBuilder) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            toHexa(md.digest(input.getBytes("US-ASCII")), outputBuilder);
        } catch(Exception e) {
            outputBuilder.append(Integer.toHexString(input.hashCode()));
        }
    }
    /**
     * Normalise SQL and generate a unique ID
     * @param sql SQL
     * @return SQL ID
     */
    public String buildSqlId(String sql) {
        final SqlNormalizer sqlNormalizer = new SqlNormalizer(sql);
        StringBuilder stringBuilder=new StringBuilder(sqlNormalizer.getType()).append('_');
        toSha1(sqlNormalizer.getNormalizedSql(), stringBuilder);
        return stringBuilder.toString();
    }

    private Split startStatementStopwatch(String connectionFactoryName, String sql, String sqlId, String suffix) {
        Stopwatch stopwatch = manager.getStopwatch(connectionFactoryName + "." + sqlId + suffix);
        if (stopwatch.getNote() == null) {
            stopwatch.setNote(sql);
        }
        return stopwatch.start();
    }

    private Split startStatementStopwatch(String connectionFactoryName, String sql, String sqlId) {
        return startStatementStopwatch(connectionFactoryName, sql, sqlId, ".stmt");
    }

    public Split startStatementExecuteStopwatch(String connectionFactoryName, String sql, String sqlId) {
        return startStatementStopwatch(connectionFactoryName, sql, sqlId, ".exec");
    }
    private Split startResultSetStopwatch(String connectionFactoryName, String sql, String sqlId) {
        return startStatementStopwatch(connectionFactoryName, sql, sqlId, ".rset");
    }

    public Statement wrapStatement(String connectionFactoryName, Statement statement) {
        return new StatementProxyFactory(statement, connectionFactoryName, this,
                startStatementStopwatch(connectionFactoryName)
        ).newProxy();
    }

    public PreparedStatement wrapPreparedStatement(String connectionFactoryName, PreparedStatement preparedStatement, String sql) {
        final String sqlId = buildSqlId(sql);
        return new PreparedStatementProxyFactory(preparedStatement, connectionFactoryName, this,
                startStatementStopwatch(connectionFactoryName, sql, sqlId),
                sql, sqlId
        ).newProxy();
    }

    public CallableStatement wrapCallableStatement(String connectionFactoryName, CallableStatement callableStatement, String sql) {
        final String sqlId = buildSqlId(sql);
        return new CallableStatementProxyFactory(callableStatement, connectionFactoryName, this,
                startStatementStopwatch(connectionFactoryName, sql, sqlId),
                sql, sqlId
        ).newProxy();
    }

    public Object wrapResultSet(String connectionFactoryName, ResultSet resultSet, String sql, String sqlId) {
        return new ResultSetProxyFactory(resultSet, getResultSetType(resultSet),connectionFactoryName, this,
                startResultSetStopwatch(connectionFactoryName, sql, sqlId)
        ).newProxy();
    }

    /**
     * Determine the interface implemented by this result set
     * @param resultSet
     */
    private Class<? extends ResultSet> getResultSetType(ResultSet resultSet) {
        Class<? extends ResultSet> resultSetType;
        if (resultSet instanceof RowSet) {
            if (resultSet instanceof CachedRowSet) {
                if (resultSet instanceof WebRowSet) {
                    if (resultSet instanceof FilteredRowSet) {
                        resultSetType=FilteredRowSet.class;
                    } else if (resultSet instanceof JoinRowSet) {
                        resultSetType=JoinRowSet.class;
                    } else {
                        resultSetType=WebRowSet.class;
                    }
                } else {
                    resultSetType=CachedRowSet.class;
                }
            } else if (resultSet instanceof JdbcRowSet) {
                resultSetType=JdbcRowSet.class;
            } else {
                resultSetType=RowSet.class;
            }
        } else {
            resultSetType=ResultSet.class;
        }
        return resultSetType;
    }
}