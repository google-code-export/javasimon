package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.DelegatingMethodInvocation;

import java.sql.Statement;

/**
 * JDBC Proxy handler for {@link Statement}
 */
public class StatementProxyHandler extends AbstractStatementProxyHandler<Statement> {
    public StatementProxyHandler(Statement delegate, String name, JdbcProxyFactory proxyFactory, Split lifeSplit) {
        super(delegate, Statement.class, name, proxyFactory, lifeSplit);
    }

    protected Object execute(DelegatingMethodInvocation<Statement> methodInvocation) throws Throwable {
        Object result;
        if (methodInvocation.getArgCount()>0) {
            final String sql=methodInvocation.getArgAt(0, String.class);
            final String sqlId= proxyFactory.buildSqlId(sql);
            result = execute(methodInvocation, sql, sqlId);

        } else {
            result=methodInvocation.proceed();
        }
        return result;
    }

}
