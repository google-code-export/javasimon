package org.javasimon.report;

import org.javasimon.SimonException;
import org.javasimon.StopwatchSample;
import org.javasimon.CounterSample;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.exceptions.UnableToExecuteStatementException;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link DbStorage} interface that stores data to a relational database.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class SqlStorage implements DbStorage {

	/** SQL statement to create table for storing Stopwatches' samples */
	private static final String CREATE_STOPWATCH_TABLE =
				"CREATE TABLE stopwatch (" +
						"timestamp BIGINT," +
						"name varchar(255)," +
						"note varchar(255)," +
						"firstUsage BIGINT," +
						"lastUsage BIGINT," +
						"lastReset BIGINT," +
						"total BIGINT," +
						"min BIGINT," +
						"max BIGINT," +
						"minTimestamp BIGINT," +
						"maxTimestamp BIGINT," +
						"active BIGINT," +
						"maxActive BIGINT," +
						"maxActiveTimestamp BIGINT," +
						"last BIGINT," +
						"mean DOUBLE PRECISION," +
						"stdDev DOUBLE PRECISION," +
						"variance DOUBLE PRECISION," +
						"varianceN DOUBLE PRECISION" +
				");";

	/** SQL statement to remove table for storing Stopwatches' samples */
	private static final String DROP_STOPWATCH_TABLE =
			"DROP TABLE stopwatch";

	/** SQL statement to insert a Stopwatch sample to the database */
	private static final String INSERT_STOPWATCH =
			"INSERT INTO stopwatch " +
			"(timestamp, name, note, firstUsage, lastUsage, lastReset, total, min, max, minTimestamp, maxTimestamp, active, maxActive, maxActiveTimestamp, last, mean, stdDev, variance, varianceN) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	/** SQL statement to select all Stopwatch samples */
	private static final String SELECT_STOPWATCHES =
			"SELECT * FROM stopwatch";

	/** SQL statement to remove all Stopwatches' samples */
	private static final String DELETE_STOPWATCHES =
			"DELETE FROM stopwatch";

	/** SQL statement to create for storing Counters' samples */
	private static final String CREATE_COUNTER_TABLE =
			"CREATE TABLE counter (" +
				"timestamp BIGINT," +
				"name varchar(255)," +
				"note varchar(255)," +
				"firstUsage BIGINT," +
				"lastUsage BIGINT," +
				"lastReset BIGINT," +
				"counter BIGINT," +
				"min BIGINT," +
				"max BIGINT," +
				"minTimestamp BIGINT," +
				"maxTimestamp BIGINT," +
				"incrementSum BIGINT," +
				"decrementSum BIGINT" +
			");";

	/** SQL statement to remove table for storing Counters' samples */
	private static final String DROP_COUNTER_TABLE =
			"DROP TABLE counter";

	/** SQL statement to insert a Counter's sample */
	private static final String INSERT_COUNTER =
			"INSERT INTO counter " +
					"(timestamp, name, note, firstUsage, lastUsage, lastReset, counter, min, max, minTimestamp, maxTimestamp, incrementSum, decrementSum) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

	/** SQL statement to select all Counters' samples */
	private static final String SELECT_COUNTERS =
			"SELECT * FROM counter";

	/** SQL statement to delete all Counters' samples */
	private static final String DELETE_COUNTERS =
			"DELETE FROM counter";

	/** Instance of DBI object from JDBI library to manipulate data in the database */
	private DBI dbi;

	/** Create <code>SqlStorate</code> for the specified <code>dbi</code> instance */
	SqlStorage(DBI dbi) {
		this.dbi = dbi;
	}

	/** Create <code>SqlStorage</code> for the specified data source */
	public SqlStorage(DataSource dataSource) {
		this.dbi = new DBI(dataSource);
	}

	@Override
	public void createTables() {
		Handle h = dbi.open();
		try {
			h.execute(CREATE_STOPWATCH_TABLE);
			h.execute(CREATE_COUNTER_TABLE);
		} catch (Exception e) {
			throw new SimonException(e);
		} finally {
			h.close();
		}
	}

	@Override
	public void storeStopwatches(long timestamp, List<StopwatchSample> stopwatchSamples) {
		Handle h = dbi.open();
		try {
			for (StopwatchSample stopwatchSample : stopwatchSamples) {
				h.execute(INSERT_STOPWATCH,
						timestamp,
						stopwatchSample.getName(),
						stopwatchSample.getNote(),
						stopwatchSample.getFirstUsage(),
						stopwatchSample.getLastUsage(),
						stopwatchSample.getLastReset(),
						stopwatchSample.getTotal(),
						stopwatchSample.getMin(),
						stopwatchSample.getMax(),
						stopwatchSample.getMinTimestamp(),
						stopwatchSample.getMaxTimestamp(),
						stopwatchSample.getActive(),
						stopwatchSample.getMaxActive(),
						stopwatchSample.getMaxActiveTimestamp(),
						stopwatchSample.getLast(),
						stopwatchSample.getMean(),
						stopwatchSample.getStandardDeviation(),
						stopwatchSample.getVariance(),
						stopwatchSample.getVarianceN());
			}
		} finally {
			h.close();
		}
	}

	@Override
	public void storeCounters(long timestamp, List<CounterSample> counterSamples) {
		Handle h = dbi.open();
		try {
			for (CounterSample sample : counterSamples) {
				h.execute(INSERT_COUNTER,
						timestamp,
						sample.getName(),
						sample.getNote(),
						sample.getFirstUsage(),
						sample.getLastUsage(),
						sample.getLastReset(),
						sample.getCounter(),
						sample.getMin(),
						sample.getMax(),
						sample.getMinTimestamp(),
						sample.getMaxTimestamp(),
						sample.getIncrementSum(),
						sample.getDecrementSum());
			}
		} finally {
			h.close();
		}
	}

	@Override
	public void removeAll() {
		Handle h = dbi.open();
		try {
			h.execute(DELETE_COUNTERS);
			h.execute(DELETE_STOPWATCHES);
		} finally {
			h.close();
		}
	}

	@Override
	public void close() {

	}

	@Override
	public boolean deleteTables() {
		Handle h = dbi.open();
		try {
			h.execute(DROP_STOPWATCH_TABLE);
			h.execute(DROP_COUNTER_TABLE);
			return true;
		} catch (UnableToExecuteStatementException e) {
			return false;
		} finally {
			h.close();
		}
	}

	DBI getDbi() {
		return dbi;
	}

	/**
	 * Gets all stored Counters' samples.
	 *
	 * @return all stored Counters' samples
	 */
	List<TimedCounterSample> getCounterSamples() {
		Handle h = dbi.open();
		try {
			List<Map<String, Object>> query = h.select(SELECT_COUNTERS);
			List<TimedCounterSample> samples = new ArrayList<TimedCounterSample>(query.size());

			for (Map<String, Object> map : query) {
				samples.add(mapCounter(map));
			}

			return samples;
		} finally {
			h.close();
		}
	}

	private TimedCounterSample mapCounter(Map<String, Object> map) {
		long timestamp = getLong(map, "timestamp");

		CounterSample sample = new CounterSample();
		sample.setName(getString(map, "name"));
		sample.setNote(getString(map, "note"));
		sample.setFirstUsage(getLong(map, "firstUsage"));
		sample.setLastUsage(getLong(map, "lastUsage"));
		sample.setLastReset(getLong(map, "lastReset"));
		sample.setCounter(getLong(map, "counter"));
		sample.setMax(getLong(map, "max"));
		sample.setMin(getLong(map, "min"));
		sample.setMinTimestamp(getLong(map, "minTimestamp"));
		sample.setMaxTimestamp(getLong(map, "maxTimestamp"));
		sample.setIncrementSum(getLong(map, "incrementSum"));
		sample.setDecrementSum(getLong(map, "decrementSum"));

		return new TimedCounterSample(timestamp, sample);
	}

	/**
	 * Gets all stored Stopwatches's samples.
	 *
	 * @return all stored Stopwatches's samples
	 */
	List<TimedStopwatchSample> getStopwatchSamples() {
		Handle h = dbi.open();
		try {
			List<Map<String, Object>> query = h.select(SELECT_STOPWATCHES);
			List<TimedStopwatchSample> samples = new ArrayList<TimedStopwatchSample>(query.size());
			for (Map<String, Object> map : query) {
				samples.add(mapStopwatch(map));
			}

			return samples;
		} finally {
			h.close();
		}
	}

	private TimedStopwatchSample mapStopwatch(Map<String, Object> map) {
		long timestamp = getLong(map, "timestamp");

		StopwatchSample sample = new StopwatchSample();
		sample.setName(getString(map, "name"));
		sample.setNote(getString(map, "note"));
		sample.setFirstUsage(getLong(map, "firstUsage"));
		sample.setLastUsage(getLong(map, "lastUsage"));
		sample.setLastReset(getLong(map, "lastReset"));
		sample.setTotal(getLong(map, "total"));
		sample.setMax(getLong(map, "max"));
		sample.setMin(getLong(map, "min"));
		sample.setMinTimestamp(getLong(map, "minTimestamp"));
		sample.setMaxTimestamp(getLong(map, "maxTimestamp"));
		sample.setActive(getLong(map, "active"));
		sample.setMaxActive(getLong(map, "maxActive"));
		sample.setMaxActiveTimestamp(getLong(map, "maxActiveTimestamp"));
		sample.setLast(getLong(map, "last"));
		sample.setMean(getDouble(map, "mean"));
		sample.setStandardDeviation(getDouble(map, "stdDev"));
		sample.setVariance(getDouble(map, "variance"));
		sample.setVarianceN(getDouble(map, "varianceN"));

		return new TimedStopwatchSample(timestamp, sample);
	}

	private Double getDouble(Map<String, Object> map, String key) {
		return (Double) map.get(key);
	}

	private String getString(Map<String, Object> map, String key) {
		return (String) map.get(key);
	}

	private Long getLong(Map<String, Object> map, String key) {
		return (Long) map.get(key);
	}
}
