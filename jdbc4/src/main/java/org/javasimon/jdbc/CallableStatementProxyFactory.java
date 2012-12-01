package org.javasimon.jdbc;

import org.javasimon.Split;
import org.javasimon.proxy.DelegatingMethodInvocation;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;

/**
 * Created with IntelliJ IDEA.
 * User: gquintana
 * Date: 30/11/12
 * Time: 21:37
 * To change this template use File | Settings | File Templates.
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
        if (methodInvocation.getArgs().length>0) {
            result=execute(methodInvocation, sql, sqlId);
        } else {
            result=methodInvocation.proceed();
        }
        return result;
    }

}
