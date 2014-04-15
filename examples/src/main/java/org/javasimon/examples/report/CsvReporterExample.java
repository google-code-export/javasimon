package org.javasimon.examples.report;

import org.javasimon.Counter;
import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.javasimon.examples.ExampleUtils;
import org.javasimon.report.CsvReporter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.TimeUnit;

/**
 * This example demo shows how to use CsvReporter class to store collected metrics to
 * CSV files.
 * <p/>
 * It creates several stopwatches and counters emulates metrics and then outputs
 * the result metric file to console. *
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class CsvReporterExample {

	// How long metrics will be collected in ms
	private static final long WORK_TIME = 40000;
	// Name of the reporter
	private static final String REPORTER_NAME = "csvReporter";

	public static void main(String... args) throws Exception {
		// Create and configure the reporter
		CsvReporter reporter = CsvReporter
			.forDefaultManager()
			.append()
			.separator(';')
			.every(5, TimeUnit.SECONDS)
			.name(REPORTER_NAME);

		reporter.start();

		// Create Simons that will be reported
		Manager manager = SimonManager.manager();
		Stopwatch stopwatch1 = manager.getStopwatch("stopwatch1");
		Stopwatch stopwatch2 = manager.getStopwatch("stopwatch2");

		Counter counter1 = manager.getCounter("counter1");
		Counter counter2 = manager.getCounter("counter2");

		long timeToFinish = System.currentTimeMillis() + WORK_TIME;
		long timeForNextCountdownReport = System.currentTimeMillis();

		while (System.currentTimeMillis() < timeToFinish) {
			ExampleUtils.randomWork(stopwatch1, stopwatch2);
			ExampleUtils.randomWork(counter1, counter2);

			if (System.currentTimeMillis() > timeForNextCountdownReport) {
				System.out.println("Time to finish (ms): " + (timeToFinish - System.currentTimeMillis()));
				timeForNextCountdownReport = System.currentTimeMillis() + 5000;
			}
		}

		// Stop the reporter
		reporter.stop();

		// Display result CSV files
		System.out.println("Counters file:");
		System.out.println(fileToString(CsvReporter.DEFAULT_COUNTERS_FILE));

		System.out.println("Stopwatches file:");
		System.out.println(fileToString(CsvReporter.DEFAULT_STOPWATCHES_FILE));
	}

	private static String fileToString(String filePath) throws IOException {
		StringBuilder content = new StringBuilder();

		Reader reader = null;

		try {
			reader = new BufferedReader(new FileReader(filePath));

			char[] buffer = new char[1000];
			int read;
			while ((read = reader.read(buffer)) > 0) {
				content.append(buffer, 0, read);
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}

		return content.toString();
	}
}
