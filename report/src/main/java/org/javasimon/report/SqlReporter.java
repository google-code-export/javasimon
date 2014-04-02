package org.javasimon.report;

import org.javasimon.CounterSample;
import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.StopwatchSample;

import javax.sql.DataSource;
import java.util.List;

/**
 * Reporter that stores collected samples to a relation database.
 * To create an instance of this reporter one need to provide a DataSource
 * instance to connect to database.
 *
 * <p>
 * <code>SqlReporter</code> can create tables for data by itself or leave this task for the administrator.
 * The desired behaviour can be changed using methods {@link SqlReporter#createTables()}
 * or {@link SqlReporter#setCreateTables(boolean)}. By default SqlReport does not
 * create tables in the database
 *
 * <p>
 * <code>SqlReporter</code> can remove all existing data from the tables or simply append
 * new data. This behaviour can be changed using methods {@link SqlReporter#append()} or
 * {@link SqlReporter#setAppendData(boolean)}
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class SqlReporter extends ScheduledReporter<SqlReporter> {

	/** Sql storage used to store samples to a database */
	private SqlStorage sqlStorage;

	/** Whether to append or replace existing data */
	private boolean appendData;

	/** Whether to create tables or use existing */
	private boolean createTables;

	private SqlReporter(Manager manager) {
		super(manager);
	}

	/**
	 * Create an instance of <code>SqlReporter</code> for the specified manager.
	 *
	 * @param manager manager that will be used by the new instance of <code>SqlReporter</code>
	 * @return a new instance of <code>SqlReporter</code>
	 */
	public static SqlReporter forManager(Manager manager) {
		return new SqlReporter(manager);
	}

	/**
	 * Create an instance of <code>SqlReporter</code> for the specified manager.
	 *
	 * @return a new instance <code>SqlReporter</code>
	 */
	public static SqlReporter forDefaultManager() {
		return forManager(SimonManager.manager());
	}

	@Override
	protected void report(List<StopwatchSample> stopwatchSamples, List<CounterSample> counterSamples) {
		long timestamp = getTimeSource().getTime();
		sqlStorage.storeCounter(timestamp, counterSamples);
		sqlStorage.storeStopwatch(timestamp, stopwatchSamples);
	}

	@Override
	protected void onStart() {
		if (createTables) {
			sqlStorage.createTables();
		}

		if (!appendData) {
			sqlStorage.removeAll();
		}
	}

	@Override
	protected void onStop() {

	}

	SqlStorage getSqlStorage() {
		return sqlStorage;
	}

	/**
	 * Create tables on reporter start.
	 *
	 * @return this instance of <code>SqlReporter</code>
	 */
	public SqlReporter createTables() {
		createTables = true;
		return this;
	}

	/**
	 * Set whether to create tables.
	 *
	 * @param createTables create tables if <code>true</code>, use existing otherwise
	 */
	public void setCreateTables(boolean createTables) {
		this.createTables = createTables;
	}

	/**
	 * Whether tables for storing samples will be created.
	 *
	 * @return <code>true</code> if tables will be created, <code>false</code> otherwise
	 */
	public boolean isCreateTables() {
		return createTables;
	}

	/**
	 * Set <code>SqlStorage</code> instance that will be used by this reporter.
	 *
	 * @param storage sql storage instance that will be used by this reporter
	 * @return this instance of <code>SqlReporter</code>
	 */
	SqlReporter storage(SqlStorage storage) {
		this.sqlStorage = storage;
		return this;
	}

	/**
	 * Set data source that will be used to access the database.
	 *
	 * @param dataSource data source that will be sued to access the database
	 * @return this instance of <code>SqlReporter</code>
	 */
	public SqlReporter dataSource(DataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException("dataSource should be not null");
		}

		sqlStorage = new SqlStorageImpl(dataSource);
		return this;
	}

	/**
	 * Append new data to data already existing in the database.
	 *
	 * @return this instance of <code>SqlReporter</code>
	 */
	public SqlReporter append() {
		this.appendData = true;
		return this;
	}

	/**
	 * Change append data behaviour.
	 *
	 * @param appendData whether to append new data
	 */
	public void setAppendData(boolean appendData) {
		this.appendData = appendData;
	}

	/**
	 * Whether reporter append data or remove existing.
	 *
	 * @return <code>true</code> if data will be appended, <code>false</code> otherwise
	 */
	public boolean isAppendData() {
		return appendData;
	}
}
