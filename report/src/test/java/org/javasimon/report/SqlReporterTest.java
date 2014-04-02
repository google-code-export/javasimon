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
public class SqlReporterTest {

	private SqlReporter sqlReporter;
	private SqlStorage sqlStorage;
	private Manager manager;
	private TimeSource timeSource;

	@BeforeMethod
	public void beforeMethod() {
		sqlStorage = mock(SqlStorage.class);
		manager = mock(Manager.class);
		timeSource = mock(TimeSource.class);

		sqlReporter = SqlReporter.forManager(manager)
				.timeSource(timeSource)
				.storage(sqlStorage)
				.append()
				.createTables();
	}

	@Test
	public void testGetManager() {
		Assert.assertSame(sqlReporter.getManager(), manager);
	}

	@Test
	public void testForDefaultManager() {
		Assert.assertSame(SqlReporter.forDefaultManager().getManager(), SimonManager.manager());
	}

	@Test
	public void testGetStorage() {
		Assert.assertSame(sqlReporter.getSqlStorage(), sqlStorage);
	}

	@Test
	public void testIsCreateTables() {
		Assert.assertTrue(sqlReporter.isCreateTables());
	}

	@Test
	public void testSetCreateTables() {
		sqlReporter.setCreateTables(false);
		Assert.assertFalse(sqlReporter.isCreateTables());
	}

	@Test
	public void testSetDataSource() {
		DataSource dataSource = mock(DataSource.class);
		SqlReporter reporter = SqlReporter.forDefaultManager().dataSource(dataSource);
		Assert.assertNotNull(reporter.getSqlStorage());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testSetNullDataSource() {
		sqlReporter.dataSource(null);
	}

	@Test
	public void testGetIsAppendData() {
		Assert.assertTrue(sqlReporter.isAppendData());
	}

	@Test
	public void testSetAppendData() {
		sqlReporter.setAppendData(false);
		Assert.assertFalse(sqlReporter.isAppendData());
	}

	@Test
	public void testReport() {
		List<StopwatchSample> stopwatchesSamples = Arrays.asList(new StopwatchSample());
		List<CounterSample> countersSamples = Arrays.asList(new CounterSample());

		long timestamp = 1234;

		when(timeSource.getTime()).thenReturn(timestamp);

		sqlReporter.report(stopwatchesSamples, countersSamples);

		verify(sqlStorage).storeStopwatch(timestamp, stopwatchesSamples);
		verify(sqlStorage).storeCounter(timestamp, countersSamples);
	}

	@Test
	public void testAppendData() {
		sqlReporter.onStart();
		verify(sqlStorage, times(0)).removeAll();
	}

	@Test
	public void testNotAppendData() {
		sqlReporter.setAppendData(false);
		sqlReporter.onStart();
		verify(sqlStorage).removeAll();
	}

	@Test
	public void testCreateTables() {
		sqlReporter.onStart();
		verify(sqlStorage).createTables();
	}

	@Test
	public void testNotCreateTables() {
		sqlReporter.setCreateTables(false);
		sqlReporter.onStart();
		verify(sqlStorage, times(0)).createTables();
	}
}
