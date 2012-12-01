package org.javasimon.jdbcx;

import org.javasimon.Split;
import org.javasimon.jdbc.JdbcProxyFactory;
import org.javasimon.jdbc.JdbcProxyFactoryFactory;
import org.javasimon.proxy.DelegatingMethodInvocation;

import javax.sql.PooledConnection;
import java.sql.Connection;

/**
 * Created with IntelliJ IDEA.
 * User: gquintana
 * Date: 27/11/12
 * Time: 22:39
 * To change this template use File | Settings | File Templates.
 */
public class PooledConnectionProxyFactory<T extends PooledConnection> extends JdbcProxyFactory<T> {
    public PooledConnectionProxyFactory(T delegate, Class<T> delegateType, String name, JdbcProxyFactoryFactory proxyFactoryFactory, Split lifeSplit) {
        super(delegate, delegateType, name, proxyFactoryFactory, lifeSplit);
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
        connection=proxyFactoryFactory.wrapConnection(name,connection);
        return connection;
    }
}
