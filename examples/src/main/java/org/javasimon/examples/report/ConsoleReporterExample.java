package org.javasimon.examples.report;

import org.javasimon.*;
import org.javasimon.report.ConsoleReporter;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Example of using ConsoleReporter for reporting current values of Simons.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class ConsoleReporterExample {

	public static void main(String... args) throws Exception {
		// Create instance of ConsoleReader
		ConsoleReporter reporter = ConsoleReporter.forDefaultManager()
				.to(System.out) // can be avoided if printing should be done to console
				.name("testReporter")
				.every(5, TimeUnit.SECONDS);

		// Start reporter
		reporter.start();

		Manager manager = SimonManager.manager();

		// Simons to collect measures
		Stopwatch s1 = manager.getStopwatch("example.stopwatch1");
		Stopwatch s2 = manager.getStopwatch("example.stopwatch2");
		Counter counter = manager.getCounter("example.counter");

		Random random = new Random();
		while (true) {
			Split split = s1.start();
			Thread.sleep(random.nextInt(500));
			split.stop();

			split = s2.start();
			Thread.sleep(random.nextInt(500));
			split.stop();

			counter.increase();
		}
	}
}
