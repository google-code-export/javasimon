package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.DelegatingMethodInvocation;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created with IntelliJ IDEA.
 * User: gquintana
 * Date: 30/11/12
 * Time: 21:26
 * To change this template use File | Settings | File Templates.
 */
public class PreparedStatementProxyFactory extends AbstractStatementProxyFactory<PreparedStatement> {
    private final String sql;
    private final String sqlId;
    public PreparedStatementProxyFactory(PreparedStatement delegate, String name, JdbcProxyFactoryFactory proxyFactoryFactory, Split lifeSplit, String sql, String sqlId) {
        super(delegate, PreparedStatement.class, name, proxyFactoryFactory, lifeSplit);
        this.sql = sql;
        this.sqlId = sqlId;
    }
    protected final Object execute(DelegatingMethodInvocation<PreparedStatement> methodInvocation) throws Throwable {
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
