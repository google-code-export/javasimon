package org.javasimon.report;

import org.javasimon.CounterSample;
import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.StopwatchSample;

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

	private void reportCounters(List<CounterSample> counterSamples) {
		for (CounterSample sample : counterSamples) {
			reportSample(sample);
		}
	}

	private void reportSample(CounterSample sample) {
		printStream.println(sample);
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
		printStream.println(sample);
	}

	private void printStopwatchesBanner() {
		printStream.println("== Stopwatches ==");
	}
}
