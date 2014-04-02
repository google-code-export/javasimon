package org.javasimon.report;

import org.h2.jdbcx.JdbcConnectionPool;
import org.javasimon.CounterSample;
import org.javasimon.StopwatchSample;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.IDBI;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class SqlStorageImplTest {

	private SqlStorageImpl sqlStorage;
	private DBI dbi;

	/**
	 * List of all Handles that were created during running a test method.
	 * It is used to check if all acquired Handle objects were correctly
	 * closed.
	 */
	private List<Handle> handlesToClose;

	@BeforeMethod
	public void beforeMethod() {
		handlesToClose = new ArrayList<Handle>();

		DataSource ds = JdbcConnectionPool.create("jdbc:h2:mem:test",
				"username",
				"password");

		// DBI instance to spy on
		final DBI realDbi = new DBI(ds);
		dbi = spy(realDbi);

		// Every time a class-under-test acquires a Handle object
		// it is added to a list. When test finishes
		// we can check if all of them were closed
		when(dbi.open()).then(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				IDBI dbi = (IDBI) invocationOnMock.getMock();
				// Spy on handle
				Handle handle = spy(realDbi.open());
				// Save handle to check it later
				handlesToClose.add(handle);
				// Return object that is being spied on
				return handle;
			}
		});

		sqlStorage = new SqlStorageImpl(dbi);
		sqlStorage.deleteTables();
		sqlStorage.createTables();

	}

	/**
	 * Checks if all Handle objects were closed.
	 */
	private void verifyAllHandlesClosed() {
		for (Handle handle : handlesToClose) {
			verify(handle).close();
		}
	}

	@Test
	public void testGetDbi() {
		Assert.assertSame(sqlStorage.getDbi(), dbi);
	}

	@Test
	public void testAddStopwatchSample() {
		long timestamp = 1000;
		StopwatchSample sample = new StopwatchSample();
		sample.setName("stopwatch.name");
		sample.setNote("note");
		sample.setFirstUsage(50);
		sample.setLastUsage(500);
		sample.setLastReset(20);
		sample.setTotal(100);
		sample.setMin(2);
		sample.setMax(10);
		sample.setMinTimestamp(200);
		sample.setMaxTimestamp(300);
		sample.setActive(3);
		sample.setMaxActive(10);
		sample.setMaxActiveTimestamp(400);
		sample.setLast(5);
		sample.setMean(8.0);
		sample.setStandardDeviation(4.0);
		sample.setVariance(2.0);
		sample.setVarianceN(2.0);

		sqlStorage.storeStopwatch(timestamp, Arrays.asList(sample));

		List<TimedStopwatchSample> stopwatchSamples = sqlStorage.getStopwatchSamples();
		Assert.assertEquals(stopwatchSamples.size(), 1);
		// This looks weird, but since we do not have equals in StopwatchSamples it should work
		Assert.assertEquals(stopwatchSamples.get(0).toString(), new TimedStopwatchSample(timestamp, sample).toString());

		verifyAllHandlesClosed();
	}

	@Test
	public void testAddCounterSample() {
		long timestamp = 1000;
		CounterSample sample = new CounterSample();
		sample.setName("stopwatch.name");
		sample.setNote("note");
		sample.setFirstUsage(50);
		sample.setLastUsage(500);
		sample.setLastReset(20);
		sample.setCounter(1);
		sample.setMin(2);
		sample.setMax(10);
		sample.setMinTimestamp(200);
		sample.setMaxTimestamp(300);
		sample.setIncrementSum(3);
		sample.setDecrementSum(2);

		sqlStorage.storeCounter(timestamp, Arrays.asList(sample));

		List<TimedCounterSample> stopwatchSamples = sqlStorage.getCounterSamples();
		Assert.assertEquals(stopwatchSamples.size(), 1);
		// This looks weird, but since we do not have equals in CounterSample it should work
		Assert.assertEquals(stopwatchSamples.get(0).toString(), new TimedCounterSample(timestamp, sample).toString());

		verifyAllHandlesClosed();
	}

	@Test
	public void testRemoveAllData() {
		CounterSample counterSample = new CounterSample();
		StopwatchSample stopwatchSample = new StopwatchSample();

		sqlStorage.storeCounter(100, Arrays.asList(counterSample));
		sqlStorage.storeStopwatch(100, Arrays.asList(stopwatchSample));

		sqlStorage.removeAll();

		Assert.assertEquals(sqlStorage.getCounterSamples().size(), 0);
		Assert.assertEquals(sqlStorage.getStopwatchSamples().size(), 0);

		verifyAllHandlesClosed();
	}

	private StopwatchSample sample(int total) {
		StopwatchSample sample = new StopwatchSample();
		sample.setTotal(total);
		return sample;
	}
}
