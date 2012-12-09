package org.javasimon.jdbcx;

import org.javasimon.jdbc.JdbcProxyHandler;
import org.javasimon.jdbc.JdbcProxyFactory;
import org.javasimon.proxy.DelegatingMethodInvocation;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * JDBC Proxy handler for {@link DataSource}
 */
public class DataSourceProxyHandler extends JdbcProxyHandler<DataSource> {
    public DataSourceProxyHandler(DataSource delegate, String name, JdbcProxyFactory proxyFactory) {
        super(delegate, DataSource.class, name, proxyFactory, null);
    }
    @Override
    protected Object invoke(DelegatingMethodInvocation<DataSource> methodInvocation) throws Throwable {
        final String methodName=methodInvocation.getMethodName();
        Object result;
        if (methodName.equals("getConnection")) {
            result=getConnection(methodInvocation);
        } else {
            result=methodInvocation.proceed();
        }
        return result;
    }

    private Connection getConnection(DelegatingMethodInvocation<DataSource> methodInvocation) throws Throwable {
        Connection connection=(Connection) methodInvocation.proceed();
        connection= proxyFactory.wrapConnection(name,connection);
        return connection;
    }
}
