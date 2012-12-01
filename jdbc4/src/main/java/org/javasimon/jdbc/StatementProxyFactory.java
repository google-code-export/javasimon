package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.DelegatingMethodInvocation;
import org.javasimon.source.StopwatchTemplate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created with IntelliJ IDEA.
 * User: gquintana
 * Date: 30/11/12
 * Time: 21:04
 * To change this template use File | Settings | File Templates.
 */
public class StatementProxyFactory extends AbstractStatementProxyFactory<Statement> {
    public StatementProxyFactory(Statement delegate, String name, JdbcProxyFactoryFactory proxyFactoryFactory, Split lifeSplit) {
        super(delegate, Statement.class, name, proxyFactoryFactory, lifeSplit);
    }

    protected Object execute(DelegatingMethodInvocation<Statement> methodInvocation) throws Throwable {
        Object result;
        if (methodInvocation.getArgCount()>0) {
            final String sql=methodInvocation.getArgAt(0, String.class);
            final String sqlId=proxyFactoryFactory.buildSqlId(sql);
            result = execute(methodInvocation, sql, sqlId);

        } else {
            result=methodInvocation.proceed();
        }
        return result;
    }

}
