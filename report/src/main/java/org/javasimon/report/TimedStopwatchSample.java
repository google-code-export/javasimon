package org.javasimon.report;

import org.javasimon.StopwatchSample;

/**
 * Class for storing {@link org.javasimon.StopwatchSample} together
 * with a timestamp when it was received.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class TimedStopwatchSample {
	/** Timestamp when a sample was received */
	private final long timestamp;

	/** Counter's sample */
	private final StopwatchSample sample;

	/**
	 * Constructor.
	 *
	 * @param timestamp timestamp when the sample was received
	 * @param stopwatchSample Stopwatch's sample
	 */
	public TimedStopwatchSample(long timestamp, StopwatchSample stopwatchSample) {
		this.timestamp = timestamp;
		this.sample = stopwatchSample;
	}

	/**
	 * Gets timestamp when the sample was received.
	 *
	 * @return timestamp when the sample was received
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * Gets the Stopwatch's sample.
	 *
	 * @return Stopwatch's sample
	 */
	public StopwatchSample getSample() {
		return sample;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TimedStopwatchSample that = (TimedStopwatchSample) o;

		if (timestamp != that.timestamp) return false;
		if (sample != null ? !sample.equals(that.sample) : that.sample != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = (int) (timestamp ^ (timestamp >>> 32));
		result = 31 * result + (sample != null ? sample.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "TimedStopwatchSample{" +
				"timestamp=" + timestamp +
				", sample=" + sample +
				'}';
	}
}
