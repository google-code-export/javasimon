package org.javasimon.examples.report;

import org.javasimon.EnabledManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.javasimon.report.graphite.GraphiteClient;
import org.javasimon.report.graphite.GraphitePlaintextClient;
import org.javasimon.report.graphite.GraphiteReporter;
import org.javasimon.report.graphite.SampleToPathImpl;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Example that shows how to report Javasimon data to a Graphite server.
 * It shows a way in which data from two separate managers/servers can be separated in Graphite data tree.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class GraphiteReporterExample {
	// Default plaintext protocol port
	private static final int PORT = 2003;
	// Graphite server address
	private static final String ADDRESS = "localhost";
	private static final InetSocketAddress SERVER = new InetSocketAddress(ADDRESS, PORT);
	// Name of a stopwatch
	private static final String STOPWATCH_NAME = "example.stopwatch";

	private static final int REPORT_PERIOD = 2;
	public static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;

	public static void main(String... args) throws Exception {
		// Create two managers to emulate two separates servers with the same Simons
		EnabledManager manager1 = new EnabledManager();
		EnabledManager manager2 = new EnabledManager();

		// Create two clients to access Graphite servers.
		// Each one will add data to different subtrees (manager1, manager2)
		GraphiteClient client1 = new GraphitePlaintextClient(SERVER, new SampleToPathImpl("manager1"));
		GraphiteClient client2 = new GraphitePlaintextClient(SERVER, new SampleToPathImpl("manager2"));

		// Create a reporter for each manager
		GraphiteReporter graphiteReporter1 = GraphiteReporter.forManager(manager1).graphiteClient(client1).every(REPORT_PERIOD, TIME_UNIT);
		GraphiteReporter graphiteReporter2 = GraphiteReporter.forManager(manager2).graphiteClient(client2).every(REPORT_PERIOD, TIME_UNIT);

		// Start reporters
		graphiteReporter1.start();
		graphiteReporter2.start();

		// Two stopwatches with same names, but from different managers
		Stopwatch stopwatch1 = manager1.getStopwatch(STOPWATCH_NAME);
		Stopwatch stopwatch2 = manager2.getStopwatch(STOPWATCH_NAME);

		// Generate some random metrics
		Random random = new Random();
		while (true) {
			Split split = stopwatch1.start();
			Thread.sleep(random.nextInt(80));
			split.stop();

			split = stopwatch2.start();
			Thread.sleep(random.nextInt(20));
			split.stop();
		}
	}
}
