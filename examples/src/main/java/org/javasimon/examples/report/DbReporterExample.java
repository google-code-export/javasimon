package org.javasimon.examples.report;

import org.h2.jdbcx.JdbcDataSource;
import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.javasimon.report.DbReporter;
import org.javasimon.report.DbStorage;
import org.javasimon.report.SqlStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Example that shows how DbStorage can be used to store collected metrics
 * to a relation database and how SQL queries can be then made on the collected
 * data.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class DbReporterExample {

	// For how long data will be collected
	public static final long WORK_TIME = 15000;
	// Url to connect to a DB
	public static final String DB_URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
	// Username for a DB
	public static final String DB_USERNAME = "username";
	// Password for a DB
	public static final String DB_PASSWORD = "password";

	public static void main(String... args) throws Exception {
		// Create DataSource for DbStorage
		JdbcDataSource ds = createDataSource();

		// Create DB storage that will be used by a reporter
		DbStorage storage = new SqlStorage(ds);

		// Create a reporter that will store collected data
		// to a database every two seconds
		DbReporter reporter = DbReporter.forDefaultManager()
				.storage(storage) // Set DB storage
				.createTables()   // Tables should be created on start
				.every(2, TimeUnit.SECONDS); // Reporting should be done every two seconds

		// Start the reporter
		reporter.start();

		// Acquire default manager
		Manager manager = SimonManager.manager();

		Stopwatch stopwatch1 = manager.getStopwatch("stopwatch1");
		Stopwatch stopwatch2 = manager.getStopwatch("stopwatch2");

		long startTime = System.currentTimeMillis();

		System.out.println("Starting data generation");
		Random random = new Random();

		// Collect data for some time
		while (System.currentTimeMillis() < startTime + WORK_TIME) {
			Split split = stopwatch1.start();
			Thread.sleep(random.nextInt(100));
			split.stop();

			split = stopwatch2.start();
			Thread.sleep(random.nextInt(46));
			split.stop();
		}
		System.out.println("Data collecting finished");

		reporter.stop();

		System.out.println("Stopwatches with max > 45ms:");

		Connection connection = ds.getConnection();
		// Collect all incremental samples that have max value greater than 45ms (data stored in nanoseconds)
		PreparedStatement statement = connection.prepareStatement("SELECT * FROM stopwatch WHERE max > 45000000;");
		ResultSet resultSet = statement.executeQuery();

		// Output name of each Simon and timestamp when an increment sample was acquired
		while (resultSet.next()) {
			String sampleName = resultSet.getString("name");
			Long timestamp = resultSet.getLong("timestamp");

			System.out.println(String.format("%s at %d", sampleName, timestamp));
		}

		resultSet.close();
		statement.close();
		connection.close();
	}

	private static JdbcDataSource createDataSource() {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL(DB_URL);
		ds.setUser(DB_USERNAME);
		ds.setPassword(DB_PASSWORD);

		return ds;
	}
}
