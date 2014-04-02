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
 * <p>
 * Other fields differs for counters and stopwatches and correspond to fields in {@link org.javasimon.CounterSample} and
 * {@link org.javasimon.StopwatchSample} correspondingly.
 *
 * <p>
 * The reporter can either append existing files or replace old content. The appropriate mode can be selected using
 * either {@link CsvReporter#append()} or {@link CsvReporter#setAppendFile(boolean)}. By default old content is rewritten.
 * If append mode set to true and target file does exists, fields' names are not written on the reporter start.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public final class CsvReporter extends ScheduledReporter<CsvReporter> {

	/** Default name of a CSV file where counters' samples will be written */
	public static final String DEFAULT_COUNTERS_FILE = "counters.csv";

	/** Default name of a CSV file where stopwatches' samples will be written */
	public static final String DEFAULT_STOPWATCHES_FILE = "stopwatches.csv";
	public static final char DEFAULT_SEPARATOR = ',';

	/** Path to a file where counters' samples will be written */
	private String countersFile;

	/** Path to a file where stopwatches' samples will be written */
	private String stopwatchesFile;

	/** Writer that is used to save counters' samples */
	private PrintWriter countersWriter;

	/** Writer that is used to save stopwatches' samples */
	private PrintWriter stopwatchesWriter;

	/** Separator in CSV file */
	private char separator;

	/** Whether to append or replace existing files */
	private boolean appendFile;

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
		reporter.separator(DEFAULT_SEPARATOR);

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
		long currentTime = getTimeSource().getTime();
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

	private void writeFields(PrintWriter writer, Object... fields) {
		for (int i = 0; i < fields.length; i++) {
			writer.write(format(fields[i]));
			if (i != fields.length - 1) {
				writer.write(separator);
			}
		}

		writer.println();
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
			boolean countersFileExists = fileExists(countersFile);
			boolean stopwatchesFileExists = fileExists(stopwatchesFile);

			countersWriter = createPrintWriter(countersFile);
			stopwatchesWriter = createPrintWriter(stopwatchesFile);

			if (!appendFile || !countersFileExists) {
				writeCountersHeader();
			}

			if (!appendFile || !stopwatchesFileExists) {
				writeStopwatchesHeader();
			}
		} catch (IOException e) {
			throw new SimonException(e);
		}
	}

	private boolean fileExists(String fileName) {
		File file = new File(fileName);
		return file.exists();
	}

	private void writeStopwatchesHeader() {
		writeHeaders(stopwatchesWriter,
				"time",
				"name",
				"total",
				"min",
				"max",
				"minTimestamp",
				"maxTimestamp",
				"active",
				"maxActive",
				"maxActiveTimestamp",
				"last",
				"mean",
				"stdDev",
				"variance",
				"varianceN");
	}

	private PrintWriter createPrintWriter(String filePath) throws IOException {
		FileWriter writer = new FileWriter(filePath, appendFile);
		return new PrintWriter(new BufferedWriter(writer));
	}

	private void writeCountersHeader() {
		writeHeaders(countersWriter,
				"time",
				"name",
				"total",
				"min",
				"max",
				"minTimestamp",
				"maxTimestamp",
				"incrementSum",
				"decrementSum");
	}

	private void writeHeaders(PrintWriter writer, String... headers) {
		for (int i = 0; i < headers.length; i++) {
			writer.write(headers[i]);
			if (i != headers.length - 1) {
				writer.write(separator);
			}
		}

		writer.println();
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
	 * Set seprator in CSV files.
	 *
	 * @param separator separator in CSV files
	 * @return this instance of <code>CsvReporter</code>
	 */
	public CsvReporter separator(char separator) {
		this.separator = separator;
		return this;
	}

	/**
	 * Gets currently used separator in CSV files.
	 *
	 * @return currently used separator in CSV files
	 */
	public char getSeparator() {
		return separator;
	}

	/**
	 * Enable appending to CSV files.
	 *
	 * @return this instance of <code>CsvReporter</code>
	 */
	public CsvReporter append() {
		appendFile = true;
		return this;
	}

	/**
	 * Set appending mode for writing CSV files.     *
	 *
	 * @param appendFile appending mode for writing CSV files
	 */
	public void setAppendFile(boolean appendFile) {
		this.appendFile = appendFile;
	}

	/**
	 * Gets appending mode.
	 *
	 * @return appending mode
	 */
	public boolean isAppendFile() {
		return appendFile;
	}
}
