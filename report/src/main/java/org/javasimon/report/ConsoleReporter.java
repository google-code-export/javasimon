package org.javasimon.report;

import org.javasimon.CounterSample;
import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.StopwatchSample;
import org.javasimon.utils.SimonUtils;

import java.io.PrintStream;
import java.util.List;

/**
 * Reporter that outputs current Simons' values to console. Also supports reporting to other
 * source in the same format (by using method {@link org.javasimon.report.ConsoleReporter#to(java.io.PrintStream)}.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public final class ConsoleReporter extends ScheduledReporter<ConsoleReporter> {
	/** Print stream that is used to report current Simons state */
	private PrintStream printStream;

	private ConsoleReporter(Manager manager) {
		super(manager);
		to(System.out);
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
	public ConsoleReporter to(PrintStream out) {
		if (out == null) {
			throw new IllegalArgumentException("PrintStream should not be null");
		}

		this.printStream = out;
		return this;
	}

	/**
	 * Gets the instance of <code>PrintStream</code> used by this reporter.
	 *
	 * @return the instance of <code>PrintStream</code> used by this reporter
	 */
	public PrintStream getPrintStream() {
		return printStream;
	}

	@Override
	protected void report(List<StopwatchSample> stopwatchSamples, List<CounterSample> counterSamples) {
		printStopwatchesBanner();
		reportStopwatches(stopwatchSamples);
		printCountersBanner();
		reportCounters(counterSamples);
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
		printStream.println(sample.getName());
		printStream.printf(getLocale(), "                       note = %s%n", sample.getNote());
		printStream.printf(getLocale(), "                first usage = %s%n", timestamp(sample.getFirstUsage()));
		printStream.printf(getLocale(), "                 last usage = %s%n", timestamp(sample.getLastUsage()));
		printStream.printf(getLocale(), "                 last reset = %s%n", ns(sample.getLastReset()));
		printStream.printf(getLocale(), "                    counter = %d%n", sample.getCounter());
		printStream.printf(getLocale(), "                        min = %d%n", sample.getMin());
		printStream.printf(getLocale(), "                        max = %d%n", sample.getMax());
		printStream.printf(getLocale(), "              min timestamp = %s%n", timestamp(sample.getMinTimestamp()));
		printStream.printf(getLocale(), "              max timestamp = %s%n", timestamp(sample.getMaxTimestamp()));
		printStream.printf(getLocale(), "              increment sum = %d%n", sample.getIncrementSum());
		printStream.printf(getLocale(), "              decrement sum = %d%n", sample.getDecrementSum());
	}

	private void printCountersBanner() {
		printStream.println("== Counters ==");
	}

	private void reportStopwatches(List<StopwatchSample> stopwatchSamples) {
		for (StopwatchSample sample : stopwatchSamples) {
			reportSample(sample);
		}
	}

	private void reportSample(StopwatchSample sample) {
		printStream.println(sample.getName());
		printStream.printf(getLocale(), "                       note = %s%n",    sample.getNote());
		printStream.printf(getLocale(), "                first usage = %s%n",    timestamp(sample.getFirstUsage()));
		printStream.printf(getLocale(), "                 last usage = %s%n",    timestamp(sample.getLastUsage()));
		printStream.printf(getLocale(), "                 last reset = %s%n",    ns(sample.getLastReset()));
		printStream.printf(getLocale(), "                      total = %s%n",    ns(sample.getTotal()));
		printStream.printf(getLocale(), "                      count = %d%n",    sample.getCounter());
		printStream.printf(getLocale(), "                  min split = %s%n",    ns(sample.getMin()));
		printStream.printf(getLocale(), "                  max split = %s%n",    ns(sample.getMax()));
		printStream.printf(getLocale(), "        min split timestamp = %s%n",    timestamp(sample.getMinTimestamp()));
		printStream.printf(getLocale(), "        max split timestamp = %s%n",    timestamp(sample.getMaxTimestamp()));
		printStream.printf(getLocale(), "              active splits = %d%n",    sample.getActive());
		printStream.printf(getLocale(), "          max active splits = %d%n",    sample.getMaxActive());
		printStream.printf(getLocale(), "max active splits timestamp = %s%n",    timestamp(sample.getMaxActiveTimestamp()));
		printStream.printf(getLocale(), "                 last split = %s%n",    ns(sample.getLast()));
		printStream.printf(getLocale(), "                  mean time = %s%n",    ns(sample.getMean()));
		printStream.printf(getLocale(), "              std deviation = %2.2f%n", sample.getStandardDeviation());
		printStream.printf(getLocale(), "                   variance = %2.2f%n", sample.getVariance());
		printStream.printf(getLocale(), "                 n variance = %2.2f%n", sample.getVarianceN());
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
		printStream.println("== Stopwatches ==");
	}
}
