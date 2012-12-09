package org.javasimon.jdbcx;

import org.javasimon.Split;
import org.javasimon.jdbc.JdbcProxyHandler;
import org.javasimon.jdbc.JdbcProxyFactory;
import org.javasimon.proxy.DelegatingMethodInvocation;

import javax.sql.PooledConnection;
import java.sql.Connection;

/**
 * JDBC proxy handler for {@link PooledConnection} and its subclasses.
 */
public class PooledConnectionProxyHandler<T extends PooledConnection> extends JdbcProxyHandler<T> {
    public PooledConnectionProxyHandler(T delegate, Class<T> delegateType, String name, JdbcProxyFactory proxyFactory, Split lifeSplit) {
        super(delegate, delegateType, name, proxyFactory, lifeSplit);
    }

    @Override
    protected Object invoke(DelegatingMethodInvocation<T> methodInvocation) throws Throwable {
        final String methodName=methodInvocation.getMethodName();
        Object result;
        if (methodName.equals("getConnection")) {
            result=getConnection(methodInvocation);
        } else {
            result=methodInvocation.proceed();
        }
        return result;
    }

    private Connection getConnection(DelegatingMethodInvocation<T> methodInvocation) throws Throwable {
        Connection connection=(Connection) methodInvocation.proceed();
        connection= proxyFactory.wrapConnection(name,connection);
        return connection;
    }
}
