package org.javasimon.report.graphite;

import org.javasimon.StopwatchSample;
import org.javasimon.CounterSample;

import java.util.List;

/**
 * Interface for sending collected metrics to a Graphite server. Different implementations
 * of this interface may implement different protocols for reporting collected data.
 *
 * <p>
 * An implementation of this interface can be in one of two states: connected, disconnected.
 * Only transitions from connected to disconnected and from disconnected to connected states are allowed.
 * To change current state to connected one need to use {@link GraphiteClient#connect()} method. Method
 * {@link GraphiteClient#close()} should be used to change current state to disconnected. Data can
 * only be sent in the connected state.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public interface GraphiteClient {

	/**
	 * Connect to a Graphite server.
	 */
	void connect();

	/**
	 * Send collected metrics to a Graphite server.
	 *
	 * @param timestamp timestamp in milliseconds when samples were acquired
	 * @param stopwatchSamples stopwatch samples to send
	 * @param counterSamples conters samples to send
	 */
	void send(long timestamp, List<StopwatchSample> stopwatchSamples, List<CounterSample> counterSamples);

	/**
	 * Close connection to a Graphite server.
	 */
	void close();
}
