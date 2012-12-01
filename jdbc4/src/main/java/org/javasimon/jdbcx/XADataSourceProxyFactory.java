package org.javasimon.jdbcx;

import org.javasimon.Split;
import org.javasimon.jdbc.JdbcProxyFactory;
import org.javasimon.jdbc.JdbcProxyFactoryFactory;
import org.javasimon.proxy.DelegatingMethodInvocation;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;

/**
 * Created with IntelliJ IDEA.
 * User: gquintana
 * Date: 27/11/12
 * Time: 22:51
 * To change this template use File | Settings | File Templates.
 */
public class XADataSourceProxyFactory extends JdbcProxyFactory<XADataSource> {
    public XADataSourceProxyFactory(XADataSource delegate, String name, JdbcProxyFactoryFactory proxyFactoryFactory) {
        super(delegate, XADataSource.class, name, proxyFactoryFactory, null);
    }
    @Override
    protected Object invoke(DelegatingMethodInvocation<XADataSource> methodInvocation) throws Throwable {
        final String methodName=methodInvocation.getMethodName();
        Object result;
        if (methodName.equals("getXAConnection")) {
            result=getXAConnection(methodInvocation);
        } else {
            result=methodInvocation.proceed();
        }
        return result;
    }

    private XAConnection getXAConnection(DelegatingMethodInvocation<XADataSource> methodInvocation) throws Throwable {
        XAConnection connection=(XAConnection) methodInvocation.proceed();
        connection=proxyFactoryFactory.wrapXAConnection(name, connection);
        return connection;
    }
}