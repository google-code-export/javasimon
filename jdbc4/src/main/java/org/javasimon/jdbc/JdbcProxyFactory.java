package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.Delegating;
import org.javasimon.proxy.DelegatingMethodInvocation;
import org.javasimon.proxy.DelegatingProxyFactory;
import org.javasimon.source.StopwatchTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all JDBC Proxy factories.
 */
public abstract class JdbcProxyFactory<T> extends DelegatingProxyFactory<T> {
    /**
     * JDBC Interface class
     */
    private final Class<T> delegateType;
    /**
     * Split corresponding to measure this proxy lifetime
     */
    private final Split lifeSplit;
    /**
     * Proxy name
     */
    protected final String name;
    /**
     * Parent factory of proxy factories
     */
    protected final JdbcProxyFactoryFactory proxyFactoryFactory;

    /**
     * Main constructor
     * @param delegate Wrapped JDBC object
     * @param delegateType JDBC object interface
     * @param name Proxy name
     * @param proxyFactoryFactory Parent factory
     * @param lifeSplit Proxy life split
     */
    protected JdbcProxyFactory(T delegate, Class<T> delegateType, String name,JdbcProxyFactoryFactory proxyFactoryFactory, Split lifeSplit) {
        super(delegate);
        this.delegateType = delegateType;
        this.proxyFactoryFactory=proxyFactoryFactory;
        this.name=name;
        this.lifeSplit=lifeSplit;
    }


    private boolean isDelegateType(Class<?> iface) {
        return this.delegateType.equals(iface);
    }
    private Class getClassArg(DelegatingMethodInvocation methodInvocation) {
        return (Class) methodInvocation.getArgAt(0, Class.class);
    }
    protected Object isWrapperFor(DelegatingMethodInvocation methodInvocation) throws Throwable {
        final Class iface = getClassArg(methodInvocation);
        return isDelegateType(iface) ? true : methodInvocation.proceed();
    }
    protected Object close(DelegatingMethodInvocation methodInvocation) throws Throwable {
        stop(lifeSplit);
        return methodInvocation.proceed();
    }
    protected final void stop(Split split) {
        StopwatchTemplate.stop(split);
    }
    protected Object unwrap(DelegatingMethodInvocation methodInvocation) throws SQLException {
        final Class iface= getClassArg(methodInvocation);
        final Wrapper delegateWrapper=(Wrapper) delegate;
        Object result;
        if (isDelegateType(iface)) {
            result=delegateWrapper.isWrapperFor(iface) ? delegateWrapper.unwrap(iface) : iface.cast(delegateWrapper);
        } else {
            result=delegateWrapper.unwrap(iface);
        }
        return result;
    }
    public T newProxy() {
        return (T) newProxy(delegate.getClass().getClassLoader(), new Class[]{delegateType});
    }
}
