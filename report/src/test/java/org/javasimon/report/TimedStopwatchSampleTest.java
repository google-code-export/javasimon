package org.javasimon.report;

import org.javasimon.StopwatchSample;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class TimedStopwatchSampleTest {

	private TimedStopwatchSample timedCounterSample;

	private static final long TIMESTAMP = 123L;
	private static final StopwatchSample SAMPLE = sample(1);

	@BeforeMethod
	public void beforeMethod() {
		timedCounterSample = new TimedStopwatchSample(TIMESTAMP, SAMPLE);
	}

	private static StopwatchSample sample(long total) {
		StopwatchSample sample = new StopwatchSample();
		sample.setTotal(total);
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
		Assert.assertEquals(timedCounterSample, new TimedStopwatchSample(TIMESTAMP, SAMPLE));
	}
}
