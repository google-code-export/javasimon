package org.javasimon.report;

import org.javasimon.CounterSample;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class TimedCounterSampleTest {

	private TimedCounterSample timedCounterSample;

	private static final long TIMESTAMP = 123L;
	private static final CounterSample SAMPLE = sample(1);

	@BeforeMethod
	public void beforeMethod() {
		timedCounterSample = new TimedCounterSample(TIMESTAMP, SAMPLE);
	}

	private static CounterSample sample(long count) {
		CounterSample sample = new CounterSample();
		sample.setCounter(count);
		return sample;
	}

	@Test
	public void testGetTimestamp() {
		Assert.assertEquals(timedCounterSample.getTimestamp(), TIMESTAMP);
	}

	@Test
	public void testGetCounterSample() {
		Assert.assertEquals(timedCounterSample.getSample(), SAMPLE);
	}


	@Test
	public void testEquality() {
		Assert.assertEquals(timedCounterSample, new TimedCounterSample(TIMESTAMP, SAMPLE));
	}
}
