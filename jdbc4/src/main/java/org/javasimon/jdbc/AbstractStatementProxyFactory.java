package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.DelegatingMethodInvocation;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created with IntelliJ IDEA.
 * User: gquintana
 * Date: 30/11/12
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractStatementProxyFactory<T extends Statement> extends JdbcProxyFactory<T> {
    public AbstractStatementProxyFactory(T delegate, Class<T> delegateType, String name, JdbcProxyFactoryFactory proxyFactoryFactory, Split lifeSplit) {
        super(delegate, delegateType, name, proxyFactoryFactory, lifeSplit);
    }
    @Override
    protected Object invoke(DelegatingMethodInvocation<T> delegatingMethodInvocation) throws Throwable {
        final String methodName=delegatingMethodInvocation.getMethodName();
        Object result;
        if (methodName.equals("isWrapperFor")) {
            result=isWrapperFor(delegatingMethodInvocation);
        } else if (methodName.equals("unwrap")) {
            result=unwrap(delegatingMethodInvocation);
        } else if (methodName.equals("close")) {
            result=close(delegatingMethodInvocation);
        } else if (methodName.equals("execute")||methodName.equals("executeQuery")||methodName.equals("executeUpdate")) {
            result=execute(delegatingMethodInvocation);
        } else {
            result=delegatingMethodInvocation.proceed();
        }
        return result;
    }

    protected abstract Object execute(DelegatingMethodInvocation<T> delegatingMethodInvocation) throws Throwable;

    protected final Object execute(DelegatingMethodInvocation<T> methodInvocation, String sql, String sqlId) throws Throwable {
        Object result;
        // Execute and measure
        final Split executeSplit=proxyFactoryFactory.startStatementExecuteStopwatch(name, sql, sqlId);
        result=methodInvocation.proceed();
        stop(executeSplit);
        // Wrap result set
        if (result instanceof ResultSet) {
            result=proxyFactoryFactory.wrapResultSet(name, (ResultSet) result, sql, sqlId);
        }
        return result;
    }
}
