package org.javasimon.jdbc;

import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.javasimon.cache.Cache;
import org.javasimon.cache.CacheLoader;
import org.javasimon.cache.MapCache;
import org.javasimon.cache.NoCache;
import org.javasimon.jdbc4.SqlNormalizer;
import org.javasimon.jdbcx.DataSourceProxyHandler;
import org.javasimon.jdbcx.PooledConnectionProxyHandler;
import org.javasimon.jdbcx.XADataSourceProxyHandler;
import org.javasimon.proxy.ProxyFactory;
import org.javasimon.proxy.ReflectProxyFactory;

import javax.sql.*;
import javax.sql.rowset.*;
import java.security.MessageDigest;
import java.sql.*;

/**
 * Factory of {@code JdbcProxyHandler} sub classes, central class of the JDBC module,
 * it can be used to wrap any JDBC component (connection, statement, result set...).
 * It may cache SQL to SQL ID transformation which is an expensive operation, but caching can not be enabled when
 * SQL contains arguments (very bad practice because of possible SQL injection):
 * for example {@code select * from person where name='John'}
 * instead of {@code select * from person where name=?}.
 */
public class JdbcProxyFactory {
	/**
	 * Simon manager
	 */
	private final Manager manager;
	/**
	 * Proxy factory
	 */
	private final ProxyFactory proxyFactory;
	/**
	 * Cache for SQL Ids
	 */
	private final Cache<String, String> sqlCache;
	/**
	 * Strategy to generate an SQL Id from an SQL Query
	 */
	public static final CacheLoader<String, String> SQL_CACHE_LOADER = new CacheLoader<String, String>() {
		@Override
		public String load(String key) {
			final SqlNormalizer sqlNormalizer = new SqlNormalizer(key);
			StringBuilder stringBuilder = new StringBuilder(sqlNormalizer.getType()).append('_');
			toSha1(sqlNormalizer.getNormalizedSql(), stringBuilder);
			return stringBuilder.toString();
		}
	};

	/**
	 * Constructor with no cache , default {@link ReflectProxyFactory} and default Simon manager
	 */
	public JdbcProxyFactory() {
		this(SimonManager.manager());
	}

	/**
	 * Constructor with no cache and {@link ReflectProxyFactory}
	 *
	 * @param manager Simon manager to use for monitoring
	 */
	public JdbcProxyFactory(Manager manager) {
		this(manager, new ReflectProxyFactory());
	}

	/**
	 * Constructor with no cache
	 *
	 * @param manager      Simon manager to use for monitoring
	 * @param proxyFactory ProxyFactory to use for proxy creation
	 */
	public JdbcProxyFactory(Manager manager, ProxyFactory proxyFactory) {
		this(manager, proxyFactory, null, null);
	}

	/**
	 * Constructor
	 *
	 * @param manager            Simon manager to use for monitoring
	 * @param proxyFactory       ProxyFactory to use for proxy creation
	 * @param sqlCacheSize       Number of SQL to keep in cache, to avoid SQL &rarr; SQL ID conversion. Value <=0 means to cache at all.
	 * @param sqlCacheTimeToLive TTL of SQL in cache
	 */
	public JdbcProxyFactory(Manager manager, ProxyFactory proxyFactory, Integer sqlCacheSize, Long sqlCacheTimeToLive) {
		this.manager = manager;
		this.proxyFactory = proxyFactory;
		if (sqlCacheSize == null || sqlCacheSize <= 0) {
			sqlCache = new NoCache<String, String>(SQL_CACHE_LOADER);
		} else {
			sqlCache = new MapCache<String, String>(SQL_CACHE_LOADER, sqlCacheSize, sqlCacheTimeToLive);
		}
	}

	private <T> T newProxy(JdbcProxyHandler<T> proxyHandler) {
		return (T) proxyFactory.newProxy(proxyHandler, proxyHandler.getProxyClass());
	}

	private Split startStopwatch(String name) {
		return manager.getStopwatch(name).start();
	}

	/**
	 * Start Stopwatch when pooled connection is created
	 *
	 * @param connectionFactoryName DataSource/Driver name
	 * @return Started split or null
	 */
	protected Split startPooledConnectionStopwatch(String connectionFactoryName) {
		return startStopwatch(connectionFactoryName + ".pooledconn");
	}

	/**
	 * Wrap a data source to monitor it.
	 *
	 * @param connectionFactoryName Data source name
	 * @param wrappedDataSource     Data source to wrap
	 * @return Wrapped data source
	 */
	public DataSource wrapDataSource(String connectionFactoryName, DataSource wrappedDataSource) {
		return newProxy(new DataSourceProxyHandler(wrappedDataSource, connectionFactoryName, this));
	}

	/**
	 * Wrap an XA data source to monitor it.
	 *
	 * @param connectionFactoryName Data source name
	 * @param wrappedDataSource     XA Data source to wrap
	 * @return Wrapped XA data source
	 */
	public XADataSource wrapXADataSource(String connectionFactoryName, XADataSource wrappedDataSource) {
		return newProxy(new XADataSourceProxyHandler(wrappedDataSource, connectionFactoryName, this));
	}

	/**
	 * Wrap a pooled connection to monitor it.
	 *
	 * @param connectionFactoryName Data source/Driver name
	 * @param wrappedConnection     Pooled connection to wrap
	 * @return Wrapped pooled connection
	 */
	public PooledConnection wrapPooledConnection(String connectionFactoryName, PooledConnection wrappedConnection) {
		return newProxy(new PooledConnectionProxyHandler<PooledConnection>(wrappedConnection, PooledConnection.class, connectionFactoryName, this, startPooledConnectionStopwatch(connectionFactoryName)));
	}

	/**
	 * Wrap an XA connection to monitor it.
	 *
	 * @param connectionFactoryName Data source/Driver name
	 * @param wrappedConnection     XA connection to wrap
	 * @return XA pooled connection
	 */
	public XAConnection wrapXAConnection(String connectionFactoryName, XAConnection wrappedConnection) {
		return newProxy(new PooledConnectionProxyHandler<XAConnection>(wrappedConnection, XAConnection.class, connectionFactoryName, this, startPooledConnectionStopwatch(connectionFactoryName)));
	}

	/**
	 * Start Stopwatch for connection
	 *
	 * @param connectionFactoryName DataSource/Driver name
	 * @param suffix                Stopwatch name suffix
	 * @return Started split or null
	 */
	protected final Split startConnectionStopwatch(String connectionFactoryName, String suffix) {
		return startStopwatch(connectionFactoryName + suffix);
	}

	/**
	 * Start Stopwatch when pooled connection is created
	 *
	 * @param connectionFactoryName DataSource/Driver name
	 * @return Started split or null
	 */
	protected Split startConnectionStopwatch(String connectionFactoryName) {
		return startConnectionStopwatch(connectionFactoryName, ".conn");
	}

	/**
	 * Wrap a connection to monitor it.
	 *
	 * @param connectionFactoryName Data source/Driver name
	 * @param wrappedConnection     Connection to wrap
	 * @return Wrapped connection
	 */
	public Connection wrapConnection(String connectionFactoryName, Connection wrappedConnection) {
		return newProxy(new ConnectionProxyHandler(wrappedConnection, connectionFactoryName, this, startConnectionStopwatch(connectionFactoryName)));
	}

	/**
	 * Start Stopwatch when statement is created
	 *
	 * @param connectionFactoryName DataSource/Driver name
	 * @return Started split or null
	 */
	protected Split startStatementStopwatch(String connectionFactoryName) {
		return startConnectionStopwatch(connectionFactoryName, ".stmt");
	}

	/**
	 * Convert byte array to en Hexa String
	 *
	 * @param input         Input byte array
	 * @param outputBuilder Output Hexa String
	 */
	private static void toHexa(byte[] input, StringBuilder outputBuilder) {
		for (byte b : input) {
			String h = Integer.toString((b & 0xff) + 0x100, 16).substring(1);
			outputBuilder.append(h);
		}
	}

	/**
	 * Encode a String to SHA-1
	 *
	 * @param input         Input String
	 * @param outputBuilder Output SHA-1
	 */
	private static void toSha1(String input, StringBuilder outputBuilder) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			toHexa(md.digest(input.getBytes("US-ASCII")), outputBuilder);
		} catch (Exception e) {
			outputBuilder.append(Integer.toHexString(input.hashCode()));
		}
	}

	/**
	 * Normalize SQL and generate a unique ID identify the SQL.
	 * A SQL cache may/should be use to speed up SQL to SQL Id transformations, provided SQL uses prepared statements
	 * and bound parameter.
	 *
	 * @param sql SQL
	 * @return SQL ID
	 */
	public String buildSqlId(String sql) {
		return sqlCache.get(sql);
	}

	/**
	 * Start Stopwatch when prepared/callable statement is created
	 *
	 * @param connectionFactoryName DataSource/Driver name
	 * @return Started split or null
	 */
	protected final Split startStatementStopwatch(String connectionFactoryName, String sql, String sqlId, String suffix) {
		Stopwatch stopwatch = manager.getStopwatch(connectionFactoryName + "." + sqlId + suffix);
		if (stopwatch.getNote() == null) {
			stopwatch.setNote(sql);
		}
		return stopwatch.start();
	}

	/**
	 * Start Stopwatch when prepared/callable statement is created
	 *
	 * @param connectionFactoryName DataSource/Driver name
	 * @return Started split or null
	 */
	protected Split startStatementStopwatch(String connectionFactoryName, String sql, String sqlId) {
		return startStatementStopwatch(connectionFactoryName, sql, sqlId, ".stmt");
	}

	/**
	 * Start Stopwatch when statement is executed
	 *
	 * @param connectionFactoryName DataSource/Driver name
	 * @return Started split or null
	 */
	public Split startStatementExecuteStopwatch(String connectionFactoryName, String sql, String sqlId) {
		return startStatementStopwatch(connectionFactoryName, sql, sqlId, ".exec");
	}

	/**
	 * Start Stopwatch when result set is created
	 *
	 * @param connectionFactoryName DataSource/Driver name
	 * @return Started split or null
	 */
	protected Split startResultSetStopwatch(String connectionFactoryName, String sql, String sqlId) {
		return startStatementStopwatch(connectionFactoryName, sql, sqlId, ".rset");
	}

	/**
	 * Wrap a simple statement to monitor it.
	 *
	 * @param connectionFactoryName Data source/Driver name
	 * @param statement             Statement to wrap
	 * @return Wrapped statement
	 */
	public Statement wrapStatement(String connectionFactoryName, Statement statement) {
		return newProxy(new StatementProxyHandler(statement, connectionFactoryName, this,
				startStatementStopwatch(connectionFactoryName)
		));
	}

	/**
	 * Wrap a prepared statement to monitor it.
	 *
	 * @param connectionFactoryName Data source/Driver name
	 * @param preparedStatement     Prepared statement to wrap
	 * @param sql                   SQL used for creation
	 * @return Wrapped prepared statement
	 */
	public PreparedStatement wrapPreparedStatement(String connectionFactoryName, PreparedStatement preparedStatement, String sql) {
		final String sqlId = buildSqlId(sql);
		return newProxy(new PreparedStatementProxyHandler(preparedStatement, connectionFactoryName, this,
				startStatementStopwatch(connectionFactoryName, sql, sqlId),
				sql, sqlId
		));
	}

	/**
	 * Wrap a callable statement to monitor it.
	 *
	 * @param connectionFactoryName Data source/Driver name
	 * @param callableStatement     Prepared statement to wrap
	 * @param sql                   SQL used for creation
	 * @return Wrapped prepared statement
	 */
	public CallableStatement wrapCallableStatement(String connectionFactoryName, CallableStatement callableStatement, String sql) {
		final String sqlId = buildSqlId(sql);
		return newProxy(new CallableStatementProxyHandler(callableStatement, connectionFactoryName, this,
				startStatementStopwatch(connectionFactoryName, sql, sqlId),
				sql, sqlId
		));
	}

	/**
	 * Wrap a result set to monitor it.
	 *
	 * @param connectionFactoryName Data source/Driver name
	 * @param resultSet             set to wrap
	 * @param sql                   SQL
	 * @return Wrapped prepared statement
	 */
	public ResultSet wrapResultSet(String connectionFactoryName, ResultSet resultSet, String sql, String sqlId) {
		return (ResultSet) newProxy(new ResultSetProxyHandler(resultSet, getResultSetType(resultSet), connectionFactoryName, this,
				startResultSetStopwatch(connectionFactoryName, sql, sqlId)
		));
	}

	/**
	 * Determine the interface implemented by this result set
	 *
	 * @param resultSet Result set
	 */
	private Class<? extends ResultSet> getResultSetType(ResultSet resultSet) {
		Class<? extends ResultSet> resultSetType;
		if (resultSet instanceof RowSet) {
			if (resultSet instanceof CachedRowSet) {
				if (resultSet instanceof WebRowSet) {
					if (resultSet instanceof FilteredRowSet) {
						resultSetType = FilteredRowSet.class;
					} else if (resultSet instanceof JoinRowSet) {
						resultSetType = JoinRowSet.class;
					} else {
						resultSetType = WebRowSet.class;
					}
				} else {
					resultSetType = CachedRowSet.class;
				}
			} else if (resultSet instanceof JdbcRowSet) {
				resultSetType = JdbcRowSet.class;
			} else {
				resultSetType = RowSet.class;
			}
		} else {
			resultSetType = ResultSet.class;
		}
		return resultSetType;
	}
}