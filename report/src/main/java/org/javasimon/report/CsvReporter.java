package org.javasimon.report;

import org.javasimon.*;

import java.io.*;
import java.util.List;

/**
 * Reporter that writes collected samples to CSV files. Counters' samples and stopwatches' samples are written into two
 * different files. Both files have the following common fields:
 * <ul>
 *     <li>time - time in ms when the sample was received</li>
 *     <li>name - name of a simon</li>
 * </ul>
 *
 * Other fields differs for counters and stopwatches and correspond to fields in {@link org.javasimon.CounterSample} and
 * {@link org.javasimon.StopwatchSample} correspondingly.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public final class CsvReporter extends ScheduledReporter<CsvReporter> {

	/** Default name of a CSV file where counters' samples will be written */
	public static final String DEFAULT_COUNTERS_FILE = "counters.csv";

	/** Default name of a CSV file where stopwatches' samples will be written */
	public static final String DEFAULT_STOPWATCHES_FILE = "stopwatches.csv";

	/** Path to a file where counters' samples will be written */
	private String countersFile;

	/** Path to a file where stopwatches' samples will be written */
	private String stopwatchesFile;

	/** Writer that is used to save counters' samples */
	private PrintWriter countersWriter;

	/** Writer that is used to save stopwatches' samples */
	private PrintWriter stopwatchesWriter;

	/** Time source used to get current time */
	private TimeSource timeSource;

	/**
	 * Constructor. Creates CsvReporter for the specified manager.
	 *
	 * @param manager manager that will be used by the CsvReporter instance
	 */
	private CsvReporter(Manager manager) {
		super(manager);
	}

	/**
	 * Create an instance of <code>CsvReporter</code> for the specified manager.
	 *
	 * @param manager manager that will be used by the <code>CsvReporter</code> instance
	 * @return a new instance of <code>CsvReporter</code>
	 */
	public static CsvReporter forManager(Manager manager) {
		CsvReporter reporter = new CsvReporter(manager);
		reporter.countersFile(DEFAULT_COUNTERS_FILE);
		reporter.stopwatchesFile(DEFAULT_STOPWATCHES_FILE);
		reporter.timeSource(new SystemTimeSource());

		return reporter;
	}

	/**
	 * Create an instance of <code>CsvReporter</code>.
	 *
	 * @return a new instance of <code>CsvReporter</code>
	 */
	public static CsvReporter forDefaultManager() {
		return forManager(SimonManager.manager());
	}

	@Override
	protected void report(List<StopwatchSample> stopwatchSamples, List<CounterSample> counterSamples) {
		long currentTime = timeSource.getTime();
		for (CounterSample counterSample : counterSamples) {
			writeSample(currentTime, counterSample);
		}

		for (StopwatchSample stopwatchSample : stopwatchSamples) {
			writeSample(currentTime, stopwatchSample);
		}
	}

	private void writeSample(long currentTime, StopwatchSample stopwatchSample) {
		writeFields(stopwatchesWriter,
				currentTime,
				stopwatchSample.getName(),
				stopwatchSample.getTotal(),
				stopwatchSample.getMin(),
				stopwatchSample.getMax(),
				stopwatchSample.getMinTimestamp(),
				stopwatchSample.getMaxTimestamp(),
				stopwatchSample.getActive(),
				stopwatchSample.getMaxActive(),
				stopwatchSample.getMaxActiveTimestamp(),
				stopwatchSample.getLast(),
				stopwatchSample.getMean(),
				stopwatchSample.getStandardDeviation(),
				stopwatchSample.getVariance(),
				stopwatchSample.getVarianceN());
	}

	private void writeSample(long currentTime, CounterSample counterSample) {
		writeFields(countersWriter,
				currentTime,
				counterSample.getName(),
				counterSample.getCounter(),
				counterSample.getMin(),
				counterSample.getMax(),
				counterSample.getMinTimestamp(),
				counterSample.getMaxTimestamp(),
				counterSample.getIncrementSum(),
				counterSample.getDecrementSum());
	}

	private void writeFields(PrintWriter countersWriter, Object... fields) {
		for (int i = 0; i < fields.length; i++) {
			countersWriter.write(format(fields[i]));
			if (i != fields.length - 1) {
				countersWriter.write(',');
			}
		}

		countersWriter.println("");
	}

	private String format(Object value) {
		if (value instanceof String) {
			return "\"" + value + "\"";
		} else {
			return value.toString();
		}
	}

	@Override
	protected void onStart() {
		try {
			countersWriter = createPrintWriter(countersFile);
			stopwatchesWriter = createPrintWriter(stopwatchesFile);
			writeCountersHeader();
			writeStopwatchesHeader();
		} catch (IOException e) {
			throw new SimonException(e);
		}
	}

	private void writeStopwatchesHeader() {
		stopwatchesWriter.println("time,name,total,min,max,minTimestamp,maxTimestamp,active,maxActive,maxActiveTimestamp,last,mean,stdDev,variance,varianceN");
	}

	private PrintWriter createPrintWriter(String filePath) throws IOException {
		FileWriter writer = new FileWriter(filePath);
		return new PrintWriter(new BufferedWriter(writer));
	}

	private void writeCountersHeader() {
		countersWriter.println("time,name,total,min,max,minTimestamp,maxTimestamp,incrementSum,decrementSum");
	}

	@Override
	protected void onStop() {
		countersWriter.flush();
		countersWriter.close();
		stopwatchesWriter.flush();
		stopwatchesWriter.close();
	}

	/**
	 * Set a path to a file where counters' samples will be written.
	 *
	 * @param countersFile path to a CSV file for counters
	 * @return this instance of <code>CsvReporter</code>
	 */
	public CsvReporter countersFile(String countersFile) {
		if (countersFile == null || countersFile.isEmpty()) {
			throw new IllegalArgumentException("countersFile should not be null or empty");
		}

		this.countersFile = countersFile;
		return this;
	}

	/**
	 * Gets path to a CSV file where counters' samples will be written.
	 *
	 * @return path to a CSV file where counters' samples will be written
	 */
	public String getCountersFile() {
		return countersFile;
	}

	/**
	 * Set path to a CSV file where counters' samples will be written.
	 *
	 * @param stopwatchesFile path to a CSV file where counters' samples will be written
	 * @return this instance of <code>CsvReporter</code>
	 */
	public CsvReporter stopwatchesFile(String stopwatchesFile) {
		if (stopwatchesFile == null || stopwatchesFile.isEmpty()) {
			throw new IllegalArgumentException("stopwatchesFile should not be null or empty");
		}

		this.stopwatchesFile = stopwatchesFile;
		return this;
	}

	/**
	 * Gets path to a CSV file where stopwatches' samples will be written.
	 *
	 * @return path to a CSV file where stopwatches' samples will be written
	 */
	public String getStopwatchesFile() {
		return stopwatchesFile;
	}

	/**
	 * Set time source that will be used to acquiring current time.
	 *
	 * @param timeSource time source instance
	 * @return this <code>CsvReporter</code> instance
	 */
	CsvReporter timeSource(TimeSource timeSource) {
		this.timeSource = timeSource;
		return this;
	}

	/**
	 * Gets <code>TimeSource</code> instance used by this <code>CsvReporter</code> instance.
	 * @return <code>TimeSource</code> instance used by this <code>CsvReporter</code> instance
	 */
	TimeSource getTimeSource() {
		return timeSource;
	}
}
