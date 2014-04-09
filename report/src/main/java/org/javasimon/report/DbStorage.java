package org.javasimon.report;

import org.javasimon.StopwatchSample;
import org.javasimon.CounterSample;

import java.util.List;

/**
 * Interface for storing Simons' samples to a database.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public interface DbStorage {

	/**
	 * Create tables required for storing samples.
	 */
	void createTables();

	/**
	 * Store stopwatch samples.
	 *
	 * @param timestamp timestamp when samples were received
	 * @param stopwatchSamples samples to be stored
	 */
	void storeStopwatches(long timestamp, List<StopwatchSample> stopwatchSamples);

	/**
	 * Store counter samples.
	 *
	 * @param timestamp timestamp when samples were received
	 * @param counterSamples samples to be stored
	 */
	void storeCounters(long timestamp, List<CounterSample> counterSamples);

	/**
	 * Remove all stored samples.
	 */
	void removeAll();

	/**
	 * Close storage and free all used resources.
	 */
	void close();

	/**
	 * Delete tables used by the storage.
	 *
	 * @return <code>true</code> if databases were removed successfully, <code>false</code> otherwise
	 */
	boolean deleteTables();
}
