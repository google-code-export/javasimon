package org.javasimon.jdbcx;

import org.javasimon.Split;
import org.javasimon.jdbc.JdbcProxyFactory;
import org.javasimon.jdbc.JdbcProxyFactoryFactory;
import org.javasimon.proxy.DelegatingMethodInvocation;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Created with IntelliJ IDEA.
 * User: gquintana
 * Date: 27/11/12
 * Time: 22:51
 * To change this template use File | Settings | File Templates.
 */
public class DataSourceProxyFactory extends JdbcProxyFactory<DataSource> {
    public DataSourceProxyFactory(DataSource delegate, String name, JdbcProxyFactoryFactory proxyFactoryFactory) {
        super(delegate, DataSource.class, name, proxyFactoryFactory, null);
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
        connection=proxyFactoryFactory.wrapConnection(name,connection);
        return connection;
    }
}
