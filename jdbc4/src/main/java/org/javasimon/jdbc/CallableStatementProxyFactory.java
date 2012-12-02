package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.DelegatingMethodInvocation;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;

/**
 * JDBC Proxy for {@link CallableStatement}
 */
public class CallableStatementProxyFactory extends AbstractStatementProxyFactory<CallableStatement>{
    private final String sql;
    private final String sqlId;
    public CallableStatementProxyFactory(CallableStatement delegate, String name, JdbcProxyFactoryFactory proxyFactoryFactory, Split lifeSplit, String sql, String sqlId) {
        super(delegate, CallableStatement.class, name, proxyFactoryFactory, lifeSplit);
        this.sql = sql;
        this.sqlId = sqlId;
    }
    protected final Object execute(DelegatingMethodInvocation<CallableStatement> methodInvocation) throws Throwable {
        Object result;
        if (methodInvocation.getArgCount()>0) {
            final String sql=methodInvocation.getArgAt(0, String.class);
            final String sqlId=proxyFactoryFactory.buildSqlId(sql);
            result = execute(methodInvocation, sql, sqlId);
        } else {
            result = execute(methodInvocation, sql, sqlId);
        }
        return result;
    }

}
