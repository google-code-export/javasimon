package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.DelegatingMethodInvocation;
import org.javasimon.proxy.DelegatingProxyHandler;
import org.javasimon.proxy.ProxyFactory;
import org.javasimon.source.StopwatchTemplate;

import java.sql.SQLException;
import java.sql.Wrapper;

/**
 * Base class for all JDBC Proxy factories.
 */
public abstract class JdbcProxyHandler<T> extends DelegatingProxyHandler<T> {
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
    protected final JdbcProxyFactory proxyFactory;

    /**
     * Main constructor
     * @param delegate Wrapped JDBC object
     * @param delegateType JDBC object interface
     * @param name Proxy name
     * @param proxyFactory Parent factory
     * @param lifeSplit Proxy life split
     */
    protected JdbcProxyHandler(T delegate, Class<T> delegateType, String name, JdbcProxyFactory proxyFactory, Split lifeSplit) {
        super(delegate);
        this.delegateType = delegateType;
        this.proxyFactory = proxyFactory;
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
    protected Object unwrap(DelegatingMethodInvocation<T> methodInvocation) throws SQLException {
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
	public ProxyFactory.ProxyClass getProxyClass() {
		return new ProxyFactory.ProxyClass(delegate.getClass().getClassLoader(), delegateType);
	}
}
