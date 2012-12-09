package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.DelegatingMethodInvocation;

import java.sql.PreparedStatement;

/**
 * JDBC proxy handler for {@link PreparedStatement}
 */
public class PreparedStatementProxyHandler extends AbstractStatementProxyHandler<PreparedStatement> {
	private final String sql;
	private final String sqlId;

	public PreparedStatementProxyHandler(PreparedStatement delegate, String name, JdbcProxyFactory proxyFactory, Split lifeSplit, String sql, String sqlId) {
		super(delegate, PreparedStatement.class, name, proxyFactory, lifeSplit);
		this.sql = sql;
		this.sqlId = sqlId;
	}

	protected final Object execute(DelegatingMethodInvocation<PreparedStatement> methodInvocation) throws Throwable {
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
