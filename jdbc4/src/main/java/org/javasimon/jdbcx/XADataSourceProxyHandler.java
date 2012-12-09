package org.javasimon.jdbcx;

import org.javasimon.jdbc.JdbcProxyHandler;
import org.javasimon.jdbc.JdbcProxyFactory;
import org.javasimon.proxy.DelegatingMethodInvocation;

import javax.sql.XAConnection;
import javax.sql.XADataSource;

/**
 * JDBC proxy handler for {@link XADataSource}
 */
public class XADataSourceProxyHandler extends JdbcProxyHandler<XADataSource> {
    public XADataSourceProxyHandler(XADataSource delegate, String name, JdbcProxyFactory proxyFactory) {
        super(delegate, XADataSource.class, name, proxyFactory, null);
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
        connection= proxyFactory.wrapXAConnection(name, connection);
        return connection;
    }
}