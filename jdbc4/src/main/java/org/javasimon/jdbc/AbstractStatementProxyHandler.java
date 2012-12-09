package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.DelegatingMethodInvocation;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Base JDBC proxy handler for Statements
 */
public abstract class AbstractStatementProxyHandler<T extends Statement> extends JdbcProxyHandler<T> {
	public AbstractStatementProxyHandler(T delegate, Class<T> delegateType, String name, JdbcProxyFactory proxyFactory, Split lifeSplit) {
		super(delegate, delegateType, name, proxyFactory, lifeSplit);
	}


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
		} else if (methodName.equals("execute") || methodName.equals("executeQuery") || methodName.equals("executeUpdate")) {
			result = execute(delegatingMethodInvocation);
		} else {
			result = delegatingMethodInvocation.proceed();
		}
		return result;
	}

	protected abstract Object execute(DelegatingMethodInvocation<T> delegatingMethodInvocation) throws Throwable;

	protected final Object execute(DelegatingMethodInvocation<T> methodInvocation, String sql, String sqlId) throws Throwable {
		Object result;
		// Execute and measure
		final Split executeSplit = proxyFactory.startStatementExecuteStopwatch(name, sql, sqlId);
		result = methodInvocation.proceed();
		stop(executeSplit);
		// Wrap result set
		if (result instanceof ResultSet) {
			result = proxyFactory.wrapResultSet(name, (ResultSet) result, sql, sqlId);
		}
		return result;
	}
	private static final InvocationFilter THIS_INVOCATION_FILTER=new MethodNamesInvocationFilter("isWrapperFor", "unwrap", "close", "execute", "executeQuery", "executeUpdate");
	@Override
	public InvocationFilter getInvocationFilter() {
		return THIS_INVOCATION_FILTER;
	}
}
