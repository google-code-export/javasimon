package org.javasimon.report;

import org.javasimon.*;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class ScheduledReporterTest {
	private ScheduledReporter scheduledReporter;
	private ScheduledReporter.ReporterRunnable reporterRunnable;

	private ScheduledExecutorService executorService;
	private Manager manager;
	private ScheduledFuture scheduledFuture;

	@BeforeMethod
	public void beforeMethod() {
		manager = mock(Manager.class);
		executorService = mock(ScheduledExecutorService.class);
		reporterRunnable = mock(ScheduledReporter.ReporterRunnable.class);
		scheduledReporter = createScheduledReporter(manager);
		scheduledReporter.setExecutorService(executorService);

		scheduledFuture = mock(ScheduledFuture.class);
		when(executorService
				.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any(TimeUnit.class)))
				.thenReturn(scheduledFuture);
	}

	private ScheduledReporter createScheduledReporter(Manager manager) {
		ScheduledReporter reporter = spy(new ScheduledReporter(manager) {
			@Override
			protected void report(List list, List list2) {
				throw new AssertionError("This method should be mocked");
			}
		});

		return reporter;
	}

	@Test
	public void testDefaultExecutorServiceIsNotNull() {
		Assert.assertNotNull(createScheduledReporter(manager).getExecutorService());
	}

	@Test
	public void testGetManager() {
		Assert.assertSame(scheduledReporter.getManager(), manager);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testSetNullManager() {
	   scheduledReporter.setManager(null);
	}

	@Test
	public void testSetExecutorService() {
		Assert.assertSame(scheduledReporter.getExecutorService(), executorService);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testSetNullExecutorService() {
		scheduledReporter.setExecutorService(null);
	}

	@Test
	public void testSetPeriod() {
		long duration = 5;
		TimeUnit timeUnit = TimeUnit.SECONDS;
		scheduledReporter.every(duration, timeUnit);

		Assert.assertEquals(scheduledReporter.getDuration(), duration);
		Assert.assertEquals(scheduledReporter.getTimeUnit(), timeUnit);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testSetZeroDuration() {
		scheduledReporter.every(0, TimeUnit.SECONDS);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testSetNegativeDuration() {
		scheduledReporter.every(-1, TimeUnit.SECONDS);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullTimeUnit() {
		scheduledReporter.every(5, null);
	}

	@Test
	public void testGetDefaultDuration() {
		Assert.assertEquals(scheduledReporter.getDuration(), ScheduledReporter.DEFAULT_DURATION);
	}

	@Test
	public void testGetDefaultTimeUnit() {
		Assert.assertEquals(scheduledReporter.getTimeUnit(), ScheduledReporter.DEFAULT_TIME_UNIT);
	}

	@Test
	public void testGetDefaultLocale() {
		Assert.assertEquals(scheduledReporter.getLocale(), Locale.getDefault());
	}

	@Test
	public void testSetLocale() {
		Locale locale = Locale.CANADA;
		scheduledReporter.locale(locale);
		Assert.assertEquals(scheduledReporter.getLocale(), locale);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testSetNullLocale() {
		scheduledReporter.locale(null);
	}

	@Test
	public void testGetDefaultSimonsFilter() {
		Assert.assertEquals(scheduledReporter.getFilter(), SimonPattern.create("*"));
	}

	@Test
	public void testSetSimonsFilter() {
		SimonFilter filter = SimonPattern.create("*abc*");
		scheduledReporter.filter(filter);
		Assert.assertEquals(scheduledReporter.getFilter(), filter);
	}

	@Test
	public void testSetName() {
		scheduledReporter.name("name");
		Assert.assertEquals(scheduledReporter.getName(), "name");
	}

	@Test
	public void testReporterRunnerScheduled() {
		long timeDuration = 5;
		TimeUnit timeUnit = TimeUnit.SECONDS;

		ScheduledReporter reporter = scheduledReporter.setExecutorService(executorService).every(timeDuration, timeUnit);
		reporter.start();

		verify(executorService)
				.scheduleWithFixedDelay(any(ScheduledReporter.ReporterRunnable.class), eq(0L), eq(timeDuration), eq(timeUnit));

	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testReporterShedulerStartTwice() {
		long timeDuration = 5;
		TimeUnit timeUnit = TimeUnit.SECONDS;

		ScheduledReporter reporter = scheduledReporter.setExecutorService(executorService).every(timeDuration, timeUnit);
		reporter.start();
		reporter.start();
	}

	@Test
	public void testReporterStop() {
		long timeDuration = 5;
		TimeUnit timeUnit = TimeUnit.SECONDS;

		ScheduledReporter reporter = scheduledReporter.setExecutorService(executorService).every(timeDuration, timeUnit);
		reporter.start();
		reporter.stop();

		verify(scheduledFuture).cancel(false);
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testReporterStopBeforeStart() {
		long timeDuration = 5;
		TimeUnit timeUnit = TimeUnit.SECONDS;

		ScheduledReporter reporter = scheduledReporter.setExecutorService(executorService).every(timeDuration, timeUnit);
		reporter.stop();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testReporterDoubleStop() {
		long timeDuration = 5;
		TimeUnit timeUnit = TimeUnit.SECONDS;

		ScheduledReporter reporter = scheduledReporter.setExecutorService(executorService).every(timeDuration, timeUnit);
		reporter.start();
		reporter.stop();

		when(scheduledFuture.isCancelled()).thenReturn(true);

		reporter.stop();
	}

	@Test
	public void testReportMethodIsCalled() {
		Stopwatch s1 = mock(Stopwatch.class, "stopwatch1");
		Stopwatch s2 = mock(Stopwatch.class, "stopwatch2");
		Counter c1 = mock(Counter.class, "counter1");

		StopwatchSample ss1 = stopwatchIncrementSample(1);
		StopwatchSample ss2 = stopwatchIncrementSample(2);
		CounterSample cs1 = counterIncrementSample(1);

		String key = ScheduledReporter.DEFAULT_INCREMENT_KEY;
		when(s1.sampleIncrement(key)).thenReturn(ss1);
		when(s2.sampleIncrement(key)).thenReturn(ss2);
		when(c1.sampleIncrement(key)).thenReturn(cs1);

		when(manager.getSimons(SimonPattern.create("*"))).thenReturn(Arrays.<Simon>asList(s1, s2, c1));
		doAnswer(RETURNS_DEFAULTS).when(scheduledReporter).report(any(List.class), any(List.class));

		ScheduledReporter.ReporterRunnable runnable = scheduledReporter.createReporterRunner();
		runnable.run();

		verify(scheduledReporter).report(Arrays.asList(ss1, ss2), Arrays.asList(cs1));
	}

	@Test
	public void testOnlyIncrementSamplesAreReported() {
		Stopwatch s1 = mock(Stopwatch.class, "stopwatch1");
		Stopwatch s2 = mock(Stopwatch.class, "stopwatch2");
		Counter c1 = mock(Counter.class, "counter1");

		StopwatchSample ss1 = stopwatchIncrementSample(1);
		StopwatchSample ss2 = stopwatchSample("stopwatch1", 2);
		CounterSample cs1 = counterSample("counter1", 1);

		String key = ScheduledReporter.DEFAULT_INCREMENT_KEY;
		when(s1.sampleIncrement(key)).thenReturn(ss1);
		when(s2.sampleIncrement(key)).thenReturn(ss2);
		when(c1.sampleIncrement(key)).thenReturn(cs1);

		when(manager.getSimons(SimonPattern.create("*"))).thenReturn(Arrays.<Simon>asList(s1, s2, c1));
		doAnswer(RETURNS_DEFAULTS).when(scheduledReporter).report(any(List.class), any(List.class));

		ScheduledReporter.ReporterRunnable runnable = scheduledReporter.createReporterRunner();
		runnable.run();

		verify(scheduledReporter).report(Arrays.asList(ss1), Collections.emptyList());
	}

	private CounterSample counterIncrementSample(long count) {
		return counterSample(null, count);
	}

	private CounterSample counterSample(String name, long count) {
		CounterSample sample = new CounterSample();
		sample.setCounter(count);
		sample.setName(name);
		return sample;
	}

	private StopwatchSample stopwatchIncrementSample(long count) {
		return stopwatchSample(null, count);
	}

	private StopwatchSample stopwatchSample(String name, long count) {
		StopwatchSample sample = new StopwatchSample();
		sample.setCounter(count);
		sample.setName(name);
		return sample;
	}

	@Test
	public void testReportMethodIsCalledWithFilter() {
		Stopwatch s1 = mock(Stopwatch.class, "stopwatch1");
		Stopwatch s2 = mock(Stopwatch.class, "stopwatch2");
		Counter c1 = mock(Counter.class, "counter1");

		StopwatchSample ss1 = stopwatchIncrementSample(1);
		StopwatchSample ss2 = stopwatchIncrementSample(2);
		CounterSample cs1 = counterIncrementSample(1);

		String key = ScheduledReporter.DEFAULT_INCREMENT_KEY;
		when(s1.sampleIncrement(key)).thenReturn(ss1);
		when(s2.sampleIncrement(key)).thenReturn(ss2);
		when(c1.sampleIncrement(key)).thenReturn(cs1);

		SimonPattern filter = SimonPattern.create("*1");
		scheduledReporter.filter(filter);
		when(manager.getSimons(filter)).thenReturn(Arrays.asList(s1, c1));

		doAnswer(RETURNS_DEFAULTS).when(scheduledReporter).report(any(List.class), any(List.class));

		ScheduledReporter.ReporterRunnable runnable = scheduledReporter.createReporterRunner();
		runnable.run();

		verify(scheduledReporter).report(Arrays.asList(ss1), Arrays.asList(cs1));
	}

	@Test
	public void testSpecifiedKeyIsUsed() {
		Stopwatch s1 = mock(Stopwatch.class, "stopwatch1");
		Stopwatch s2 = mock(Stopwatch.class, "stopwatch2");
		Counter c1 = mock(Counter.class, "counter1");

		StopwatchSample ss1 = stopwatchIncrementSample(1);
		StopwatchSample ss2 = stopwatchIncrementSample(2);
		CounterSample cs1 = counterIncrementSample(1);

		String key = "key";
		when(s1.sampleIncrement(key)).thenReturn(ss1);
		when(s2.sampleIncrement(key)).thenReturn(ss2);
		when(c1.sampleIncrement(key)).thenReturn(cs1);

		SimonPattern filter = SimonPattern.create("*1");
		scheduledReporter.filter(filter);
		when(manager.getSimons(filter)).thenReturn(Arrays.asList(s1, c1));

		doAnswer(RETURNS_DEFAULTS).when(scheduledReporter).report(any(List.class), any(List.class));

		scheduledReporter.name(key);
		ScheduledReporter.ReporterRunnable runnable = scheduledReporter.createReporterRunner();
		runnable.run();

		verify(scheduledReporter).report(Arrays.asList(ss1), Arrays.asList(cs1));
	}

	@Test
	public void testUnknownSimonsIsIgnored() {
		// Unknown Simon is in another package, so we cannot use it in this test
		// We mock it with mock Simon
		Simon unknownSimon = mock(Simon.class);

		when(manager.getSimons(SimonPattern.create("*"))).thenReturn(Arrays.asList(unknownSimon));

		doAnswer(RETURNS_DEFAULTS).when(scheduledReporter).report(any(List.class), any(List.class));
		ScheduledReporter.ReporterRunnable runnable = scheduledReporter.createReporterRunner();
		runnable.run();

		verify(scheduledReporter).report(Arrays.asList(), Arrays.asList());
	}
}
