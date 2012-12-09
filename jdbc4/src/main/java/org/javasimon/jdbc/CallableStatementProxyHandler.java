package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.DelegatingMethodInvocation;

import java.sql.CallableStatement;

/**
 * JDBC Proxy handler for {@link CallableStatement}
 */
public class CallableStatementProxyHandler extends AbstractStatementProxyHandler<CallableStatement> {
	private final String sql;
	private final String sqlId;

	public CallableStatementProxyHandler(CallableStatement delegate, String name, JdbcProxyFactory proxyFactory, Split lifeSplit, String sql, String sqlId) {
		super(delegate, CallableStatement.class, name, proxyFactory, lifeSplit);
		this.sql = sql;
		this.sqlId = sqlId;
	}

	protected final Object execute(DelegatingMethodInvocation<CallableStatement> methodInvocation) throws Throwable {
		Object result;
		if (methodInvocation.getArgCount() > 0) {
			final String sql = methodInvocation.getArgAt(0, String.class);
			final String sqlId = proxyFactory.buildSqlId(sql);
			result = execute(methodInvocation, sql, sqlId);
		} else {
			result = execute(methodInvocation, sql, sqlId);
		}
		return result;
	}

}
