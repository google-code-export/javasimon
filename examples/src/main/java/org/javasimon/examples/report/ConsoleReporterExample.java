package org.javasimon.examples.report;

import org.javasimon.Counter;
import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.Stopwatch;
import org.javasimon.examples.ExampleUtils;
import org.javasimon.report.ConsoleReporter;

import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * Example of using ConsoleReporter for reporting current values of Simons.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class ConsoleReporterExample {

	public static void main(String... args) throws Exception {
		// Create instance of ConsoleReporter
		ConsoleReporter reporter = ConsoleReporter.forDefaultManager()
			.to(new PrintWriter(System.out)) // can be avoided if printing should be done to console
			.name("testReporter")
			.every(5, TimeUnit.SECONDS);

		// Start reporter
		reporter.start();

		Manager manager = SimonManager.manager();

		// Simons to collect measures
		Stopwatch s1 = manager.getStopwatch("example.stopwatch1");
		Stopwatch s2 = manager.getStopwatch("example.stopwatch2");
		Counter counter = manager.getCounter("example.counter");

		while (true) {
			ExampleUtils.randomWork(s1, s2);
			counter.increase();
		}
	}
}
