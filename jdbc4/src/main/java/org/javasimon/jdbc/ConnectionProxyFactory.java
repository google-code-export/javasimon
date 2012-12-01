package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.DelegatingMethodInvocation;

import java.sql.*;

/**
 * Connection Proxy factory wraps JDBC connections using dynamic proxies
 * to monitor them
 */
public class ConnectionProxyFactory extends JdbcProxyFactory<Connection> {
    /**
     * Main constructor
     *
     * @param delegate Wrapped connection
     */
    public ConnectionProxyFactory(Connection delegate, String connectionFactoryName, JdbcProxyFactoryFactory proxyFactoryFactory, Split lifeSplit) {
        super(delegate, Connection.class, connectionFactoryName, proxyFactoryFactory, lifeSplit);
    }

    @Override
    protected Object invoke(DelegatingMethodInvocation<Connection> delegatingMethodInvocation) throws Throwable {
        final String methodName=delegatingMethodInvocation.getMethodName();
        Object result;
        if (methodName.equals("isWrapperFor")) {
            result=isWrapperFor(delegatingMethodInvocation);
        } else if (methodName.equals("unwrap")) {
            result=unwrap(delegatingMethodInvocation);
        } else if (methodName.equals("close")) {
            result=close(delegatingMethodInvocation);
        } else if (methodName.equals("createStatement")) {
            result=createStatement(delegatingMethodInvocation);
        } else if (methodName.equals("prepareStatement")) {
            result=prepareStatement(delegatingMethodInvocation);
        } else if (methodName.equals("prepareCall")) {
            result=prepareCall(delegatingMethodInvocation);
        } else {
            result=delegatingMethodInvocation.proceed();
        }
        return result;
    }
    private Statement createStatement(DelegatingMethodInvocation<Connection> methodInvocation) throws Throwable {
        Statement result=(Statement) methodInvocation.proceed();
        result=proxyFactoryFactory.wrapStatement(name, result);
        return result;
    }
    private PreparedStatement prepareStatement(DelegatingMethodInvocation<Connection> methodInvocation) throws Throwable {
        PreparedStatement result=(PreparedStatement) methodInvocation.proceed();
        result=proxyFactoryFactory.wrapPreparedStatement(name, result, methodInvocation.getArgAt(0, String.class));
        return result;
    }
    private CallableStatement prepareCall(DelegatingMethodInvocation<Connection> methodInvocation) throws Throwable {
        CallableStatement result=(CallableStatement) methodInvocation.proceed();
        result=proxyFactoryFactory.wrapCallableStatement(name, result, methodInvocation.getArgAt(0, String.class));
        return result;
    }
}
