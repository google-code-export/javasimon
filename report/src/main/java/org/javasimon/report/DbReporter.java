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
 * <code>DbReporter</code> can create tables for data by itself or leave this task for the administrator.
 * The desired behaviour can be changed using methods {@link DbReporter#createTables()}
 * or {@link DbReporter#setCreateTables(boolean)}. By default SqlReport does not
 * create tables in the database
 *
 * <p>
 * <code>DbReporter</code> can remove all existing data from the tables or simply append
 * new data. This behaviour can be changed using methods {@link DbReporter#append()} or
 * {@link DbReporter#setAppendData(boolean)}
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class DbReporter extends ScheduledReporter<DbReporter> {

	/** Sql storage used to store samples to a database */
	private DbStorage dbStorage;

	/** Whether to append or replace existing data */
	private boolean appendData;

	/** Whether to create tables or use existing */
	private boolean createTables;

	private DbReporter(Manager manager) {
		super(manager);
	}

	/**
	 * Create an instance of <code>DbReporter</code> for the specified manager.
	 *
	 * @param manager manager that will be used by the new instance of <code>DbReporter</code>
	 * @return a new instance of <code>DbReporter</code>
	 */
	public static DbReporter forManager(Manager manager) {
		return new DbReporter(manager);
	}

	/**
	 * Create an instance of <code>DbReporter</code> for the specified manager.
	 *
	 * @return a new instance <code>DbReporter</code>
	 */
	public static DbReporter forDefaultManager() {
		return forManager(SimonManager.manager());
	}

	@Override
	protected void report(List<StopwatchSample> stopwatchSamples, List<CounterSample> counterSamples) {
		long timestamp = getTimeSource().getTime();
		dbStorage.storeCounters(timestamp, counterSamples);
		dbStorage.storeStopwatches(timestamp, stopwatchSamples);
	}

	@Override
	protected void onStart() {
		if (createTables) {
			dbStorage.createTables();
		}

		if (!appendData) {
			dbStorage.removeAll();
		}
	}

	@Override
	protected void onStop() {

	}

	DbStorage getDbStorage() {
		return dbStorage;
	}

	/**
	 * Create tables on reporter start.
	 *
	 * @return this instance of <code>DbReporter</code>
	 */
	public DbReporter createTables() {
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
	 * Set <code>DbStorage</code> instance that will be used by this reporter.
	 *
	 * @param storage sql storage instance that will be used by this reporter
	 * @return this instance of <code>DbReporter</code>
	 */
	public DbReporter storage(DbStorage storage) {
		this.dbStorage = storage;
		return this;
	}

	/**
	 * Set data source that will be used to access the database.
	 *
	 * @param dataSource data source that will be sued to access the database
	 * @return this instance of <code>DbReporter</code>
	 */
	DbReporter dataSource(DataSource dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException("dataSource should be not null");
		}

		dbStorage = new SqlStorage(dataSource);
		return this;
	}

	/**
	 * Append new data to data already existing in the database.
	 *
	 * @return this instance of <code>DbReporter</code>
	 */
	public DbReporter append() {
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
