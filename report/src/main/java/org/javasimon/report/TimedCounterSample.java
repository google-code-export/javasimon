package org.javasimon.report;


import org.javasimon.CounterSample;

/**
 * Class for storing {@link org.javasimon.CounterSample} together
 * with a timestamp when it was received.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class TimedCounterSample {
	/** Timestamp when a sample was received */
	private final long timestamp;

	/** Counter's sample */
	private final CounterSample sample;

	/**
	 * Constructor.
	 *
	 * @param timestamp timestamp when the sample was received
	 * @param counterSample Counter's sample
	 */
	public TimedCounterSample(long timestamp, CounterSample counterSample) {
		this.timestamp = timestamp;
		this.sample = counterSample;
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
	 * Gets the Counter's sample.
	 *
	 * @return Counter's sample
	 */
	public CounterSample getSample() {
		return sample;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TimedCounterSample that = (TimedCounterSample) o;

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
		return "TimedCounterSample{" +
				"timestamp=" + timestamp +
				", sample=" + sample +
				'}';
	}
}
