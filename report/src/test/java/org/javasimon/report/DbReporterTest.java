package org.javasimon.report;

import org.javasimon.CounterSample;
import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.StopwatchSample;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class DbReporterTest {

	private DbReporter dbReporter;
	private DbStorage dbStorage;
	private Manager manager;
	private TimeSource timeSource;

	@BeforeMethod
	public void beforeMethod() {
		dbStorage = mock(DbStorage.class);
		manager = mock(Manager.class);
		timeSource = mock(TimeSource.class);

		dbReporter = DbReporter.forManager(manager)
				.timeSource(timeSource)
				.storage(dbStorage)
				.append()
				.createTables();
	}

	@Test
	public void testGetManager() {
		Assert.assertSame(dbReporter.getManager(), manager);
	}

	@Test
	public void testForDefaultManager() {
		Assert.assertSame(DbReporter.forDefaultManager().getManager(), SimonManager.manager());
	}

	@Test
	public void testGetStorage() {
		Assert.assertSame(dbReporter.getDbStorage(), dbStorage);
	}

	@Test
	public void testIsCreateTables() {
		Assert.assertTrue(dbReporter.isCreateTables());
	}

	@Test
	public void testSetCreateTables() {
		dbReporter.setCreateTables(false);
		Assert.assertFalse(dbReporter.isCreateTables());
	}

	@Test
	public void testSetDataSource() {
		DataSource dataSource = mock(DataSource.class);
		DbReporter reporter = DbReporter.forDefaultManager().dataSource(dataSource);
		Assert.assertNotNull(reporter.getDbStorage());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testSetNullDataSource() {
		dbReporter.dataSource(null);
	}

	@Test
	public void testGetIsAppendData() {
		Assert.assertTrue(dbReporter.isAppendData());
	}

	@Test
	public void testSetAppendData() {
		dbReporter.setAppendData(false);
		Assert.assertFalse(dbReporter.isAppendData());
	}

	@Test
	public void testReport() {
		List<StopwatchSample> stopwatchesSamples = Arrays.asList(new StopwatchSample());
		List<CounterSample> countersSamples = Arrays.asList(new CounterSample());

		long timestamp = 1234;

		when(timeSource.getTime()).thenReturn(timestamp);

		dbReporter.report(stopwatchesSamples, countersSamples);

		verify(dbStorage).storeStopwatches(timestamp, stopwatchesSamples);
		verify(dbStorage).storeCounters(timestamp, countersSamples);
	}

	@Test
	public void testAppendData() {
		dbReporter.onStart();
		verify(dbStorage, times(0)).removeAll();
	}

	@Test
	public void testNotAppendData() {
		dbReporter.setAppendData(false);
		dbReporter.onStart();
		verify(dbStorage).removeAll();
	}

	@Test
	public void testCreateTables() {
		dbReporter.onStart();
		verify(dbStorage).createTables();
	}

	@Test
	public void testNotCreateTables() {
		dbReporter.setCreateTables(false);
		dbReporter.onStart();
		verify(dbStorage, times(0)).createTables();
	}
}
