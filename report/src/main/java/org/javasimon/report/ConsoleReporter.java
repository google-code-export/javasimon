package org.javasimon.report;

import org.javasimon.CounterSample;
import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.StopwatchSample;
import org.javasimon.utils.SimonUtils;

import java.io.PrintWriter;
import java.util.List;

/**
 * Reporter that outputs current Simons' values to console. Also supports reporting to other
 * source in the same format (by using method {@link org.javasimon.report.ConsoleReporter#to(java.io.PrintStream)}.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public final class ConsoleReporter extends ScheduledReporter<ConsoleReporter> {
	/** Print stream that is used to report current Simons state */
	private PrintWriter printWriter;

	private ConsoleReporter(Manager manager) {
		super(manager);
		to(new PrintWriter(System.out));
	}

	/**
	 * Create <code>ConsoleReporter</code> for the specified manager.
	 *
	 * @param manager manager whose Simons will be reported
	 * @return a new instance of ConsoleReporter
	 */
	public static ConsoleReporter forManager(Manager manager) {
	   return new ConsoleReporter(manager);
	}

	/**
	 * Create <code>ConsoleReporter</code> for the default manager.
	 *
	 * @return a new instance of <code>ConsoleReporter</code>
	 */
	public static ConsoleReporter forDefaultManager() {
		return new ConsoleReporter(SimonManager.manager());
	}

	/**
	 * Set <code>PrintStream</code> that will be used by this reporter.
	 *
	 * @param out <code>PrintStream</code> that will be used by this reporter
	 * @return this instance of ConsoleReporter
	 */
	public ConsoleReporter to(PrintWriter out) {
		if (out == null) {
			throw new IllegalArgumentException("PrintStream should not be null");
		}

		this.printWriter = out;
		return this;
	}

	/**
	 * Gets the instance of <code>PrintStream</code> used by this reporter.
	 *
	 * @return the instance of <code>PrintStream</code> used by this reporter
	 */
	PrintWriter getWriter() {
		return printWriter;
	}

	@Override
	protected void report(List<StopwatchSample> stopwatchSamples, List<CounterSample> counterSamples) {
		printStopwatchesBanner();
		reportStopwatches(stopwatchSamples);
		printCountersBanner();
		reportCounters(counterSamples);
		printWriter.flush();
	}

	@Override
	protected void onStart() {

	}

	@Override
	protected void onStop() {

	}

	private void reportCounters(List<CounterSample> counterSamples) {
		for (CounterSample sample : counterSamples) {
			reportSample(sample);
		}
	}

	private void reportSample(CounterSample sample) {
		printWriter.printf(getLocale(), "Counter(name=%s, note=%s, first-usage=%s, last-usage=%s, last-reset=%s, counter=%d, min=%d, " +
				"max=%d, min-timestamp=%s, max-timestamp=%s, increment-sum=%d, decrement-sum=%d%n",
				sample.getName(),
				sample.getNote(),
				timestamp(sample.getFirstUsage()),
				timestamp(sample.getLastUsage()),
				ns(sample.getLastReset()),
				sample.getCounter(),
				sample.getMin(),
				sample.getMax(),
				timestamp(sample.getMinTimestamp()),
				timestamp(sample.getMaxTimestamp()),
				sample.getIncrementSum(),
				sample.getDecrementSum());
	}

	private void printCountersBanner() {
		printWriter.println("== Counters ==");
	}

	private void reportStopwatches(List<StopwatchSample> stopwatchSamples) {
		for (StopwatchSample sample : stopwatchSamples) {
			reportSample(sample);
		}
	}

	private void reportSample(StopwatchSample sample) {
		printWriter.printf(getLocale(), "Stopwatch(name=%s, note=%s, first-usage=%s, last-usage=%s, last-reset=%s, total=%s, " +
				"count=%d, min-split=%s, max-split=%s, min-timestamp=%s, max-timestamp=%s, active=%d, max-active=%d, " +
				"max-active-timestamp=%s, last=%s, mean-time=%s, std-deviation=%2.2f, variance=%2.2f, n-variance=%2.2f%n",
				sample.getName(),
				sample.getNote(),
				timestamp(sample.getFirstUsage()),
				timestamp(sample.getLastUsage()),
				ns(sample.getLastReset()),
				ns(sample.getTotal()),
				sample.getCounter(),
				ns(sample.getMin()),
				ns(sample.getMax()),
				timestamp(sample.getMinTimestamp()),
				timestamp(sample.getMaxTimestamp()),
				sample.getActive(),
				sample.getMaxActive(),
				timestamp(sample.getMaxActiveTimestamp()),
				ns(sample.getLast()),
				ns(sample.getMean()),
				sample.getStandardDeviation(),
				sample.getVariance(),
				sample.getVarianceN());
	}

	private String timestamp(long timestamp) {
		return SimonUtils.presentTimestamp(timestamp);
	}

	private String ns(long nsTime) {
		return SimonUtils.presentNanoTime(nsTime);
	}

	private String ns(double nsTime) {
		return SimonUtils.presentNanoTime(nsTime);
	}

	private void printStopwatchesBanner() {
		printWriter.println("== Stopwatches ==");
	}
}
