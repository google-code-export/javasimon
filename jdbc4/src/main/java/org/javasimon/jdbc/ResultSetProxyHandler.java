package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.DelegatingMethodInvocation;

import java.sql.ResultSet;

/**
 * JDBC proxy handler for {@link ResultSet} and its subclasses.
 */
public class ResultSetProxyHandler<T extends ResultSet> extends JdbcProxyHandler<T> {
	public ResultSetProxyHandler(T delegate, Class<T> delegateType, String name, JdbcProxyFactory proxyFactory, Split lifeSplit) {
		super(delegate, delegateType, name, proxyFactory, lifeSplit);
	}

	private static final InvocationFilter THIS_INVOCATION_FILTER= new MethodNamesInvocationFilter("isWrapperFor", "unwrap", "close");

	@Override
	protected Object invoke(DelegatingMethodInvocation<T> delegatingMethodInvocation) throws Throwable {
		final String methodName = delegatingMethodInvocation.getMethodName();
		Object result;
		if (methodName.equals("isWrapperFor")) {
			result = isWrapperFor(delegatingMethodInvocation);
		} else if (methodName.equals("unwrap")) {
			result = unwrap(delegatingMethodInvocation);
		} else if (methodName.equals("close")) {
			result = close(delegatingMethodInvocation);
		} else {
			result = delegatingMethodInvocation.proceed();
		}
		return result;
	}

	@Override
	public InvocationFilter getInvocationFilter() {
		return THIS_INVOCATION_FILTER;
	}
}
