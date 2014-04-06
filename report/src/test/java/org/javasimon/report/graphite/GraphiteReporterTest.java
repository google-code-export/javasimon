package org.javasimon.report.graphite;

import org.javasimon.CounterSample;
import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.StopwatchSample;
import org.javasimon.report.TimeSource;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class GraphiteReporterTest {

	private GraphiteReporter graphiteReporter;
	private Manager manager;
	private GraphiteClient graphiteClient;
	private TimeSource timeSource;

	@BeforeMethod
	public void beforeMethod() {
		manager = mock(Manager.class);
		graphiteClient = mock(GraphiteClient.class);
		timeSource = mock(TimeSource.class);

		graphiteReporter = GraphiteReporter
				.forManager(manager)
				.graphiteClient(graphiteClient)
				.timeSource(timeSource);
	}

	@Test
	public void testGetManager() {
		Assert.assertEquals(graphiteReporter.getManager(), manager);
	}

	@Test
	public void testForDefaultManager() {
		Assert.assertEquals(GraphiteReporter.forDefaultManager().getManager(), SimonManager.manager());
	}

	@Test
	public void testGetGraphiteClient() {
		Assert.assertEquals(graphiteReporter.getGraphiteClient(), graphiteClient);
	}

	@Test
	public void testConnectOnStart() {
		graphiteReporter.onStart();
		verify(graphiteClient).connect();
	}

	@Test
	public void testCloseClientOnStop() {
		graphiteReporter.onStop();
		verify(graphiteClient).close();
	}

	@Test
	public void testSendDataOnReport() {
		long timestamp = 1234;
		when(timeSource.getTime()).thenReturn(timestamp);

		List<StopwatchSample> stopwatchSamples = Arrays.asList(new StopwatchSample());
		List<CounterSample> counterSamples = Arrays.asList(new CounterSample());

		graphiteReporter.report(stopwatchSamples, counterSamples);

		verify(graphiteClient).send(timestamp, stopwatchSamples, counterSamples);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testGraphiteClientNotSet() {
		GraphiteReporter.forDefaultManager().onStart();
	}
}

