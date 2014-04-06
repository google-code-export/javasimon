package org.javasimon.report.graphite;

import org.javasimon.CounterSample;
import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.StopwatchSample;
import org.javasimon.report.ScheduledReporter;

import java.util.List;

/**
 * Reporter that sends data to a Graphite server. Can be configured with one of
 * {@link org.javasimon.report.graphite.GraphiteClient} implementations.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class GraphiteReporter extends ScheduledReporter<GraphiteReporter> {
	/** Graphite client that will be used to send data to Graphite server */
	private GraphiteClient graphiteClient;

	private GraphiteReporter(Manager manager) {
		super(manager);
	}

	/**
	 * Create an instance of <code>GraphiteReporter</code> for the specified manager.
	 *
	 * @param manager manager that will be used by the <code>GraphiteReporter</code> instance
	 * @return a new instance of <code>GraphiteReporter</code>
	 */
	public static GraphiteReporter forManager(Manager manager) {
		return new GraphiteReporter(manager);
	}

	/**
	 * Create an instance of <code>GraphiteReporter</code>.
	 *
	 * @return a new instance of <code>GraphiteReporter</code>
	 */
	public static GraphiteReporter forDefaultManager() {
		return forManager(SimonManager.manager());
	}

	/**
	 * Set an instance of <code>GraphiteClient</code> that will be used to send data
	 * to a Graphite server.
	 *
	 * @param graphiteClient instance of <code>GraphiteClient</code> that will be used to send data to a Graphite server.
	 * @return this instance of <code>GraphiteReporter</code>
	 */
	public GraphiteReporter graphiteClient(GraphiteClient graphiteClient) {
		this.graphiteClient = graphiteClient;
		return this;
	}

	@Override
	protected void report(List<StopwatchSample> stopwatchSamples, List<CounterSample> counterSamples) {
		long timestamp = getTimeSource().getTime();
		graphiteClient.send(timestamp, stopwatchSamples, counterSamples);
	}

	@Override
	protected void onStart() {
		if (graphiteClient == null) {
			throw new IllegalStateException("Instance of GraphiteClient should be specified");
		}

		graphiteClient.connect();
	}

	@Override
	protected void onStop() {
		graphiteClient.close();
	}

	/**
	 * Gets instance of <code>GraphiteClient</code>.
	 *
	 * @return instance of <code>GraphiteClient</code>
	 */
	public GraphiteClient getGraphiteClient() {
		return graphiteClient;
	}
}
