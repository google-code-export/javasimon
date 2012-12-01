package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.DelegatingMethodInvocation;

import java.sql.ResultSet;

/**
 * Created with IntelliJ IDEA.
 * User: gquintana
 * Date: 30/11/12
 * Time: 21:48
 * To change this template use File | Settings | File Templates.
 */
public class ResultSetProxyFactory<T extends ResultSet> extends JdbcProxyFactory<T> {
    public ResultSetProxyFactory(T delegate, Class<T> delegateType, String name, JdbcProxyFactoryFactory proxyFactoryFactory, Split lifeSplit) {
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
        } else {
            result=delegatingMethodInvocation.proceed();
        }
        return result;
    }
}
