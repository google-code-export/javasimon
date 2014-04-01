package org.javasimon.report;

import org.javasimon.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;

/**
 * Base class for reporters that periodically report current Simons state to external source.
 * An instance of {@link org.javasimon.report.ScheduledReporter} is created using a fluent API. Before the
 * reporter is started it should be configured using methods like
 * {@link org.javasimon.report.ScheduledReporter#filter(org.javasimon.SimonFilter)},
 * {@link org.javasimon.report.ScheduledReporter#name(String)}, etc.
 *
 * Since the reporter uses incremental sampling, to avoid interference between different reporters
 * a user should select different names for different reporters.
 *
 * This class is thread safe.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public abstract class ScheduledReporter<R extends ScheduledReporter> {

	private static final Logger logger = LoggerFactory.getLogger(ScheduledReporter.class);

	static final long DEFAULT_DURATION = 1;
	static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MINUTES;
	public static final String DEFAULT_INCREMENT_KEY = "scheduledReporter";

	/** Manager for which reporting is done */
	private Manager manager;

	/** Executor service that is periodically executes reporting task */
	private ScheduledExecutorService executorService;

	/** Duration of a time period between consecutive reports */
	private long duration;

	/** Time unit of period */
	private TimeUnit timeUnit;

	/** Locale used to output the report data */
	private Locale locale;

	/** Filter to report only subset of existing Simons */
	private SimonFilter filter;

	/** Future object for the reporting Runnable */
	private ScheduledFuture<?> scheduledFuture;

	/** Name of current reporter */
	private String name;

	/**
	 * Create ScheduledReporter for a specified manager. Also sets the following default values:
	 * <ul>
	 *     <li>Set <code>duration</code> to default value</li>
	 *     <li>Set <code>timeUnit</code> to default value</li>
	 *     <li>Set <code>locale</code> to system wide default locale</li>
	 *     <li>Set <code>filter</code> that accepts all Simons</li>
	 * </ul>
	 *
	 * @param manager
	 */
	protected ScheduledReporter(Manager manager) {
		setManager(manager);
		every(DEFAULT_DURATION, DEFAULT_TIME_UNIT);
		name(DEFAULT_INCREMENT_KEY);
		locale(Locale.getDefault());
		filter(SimonPattern.create("*"));
		setExecutorService(createExecutorService());
	}

	private ScheduledExecutorService createExecutorService() {
		ThreadFactory threadFactory = new ThreadFactory() {

			private long threadNum = 0;

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, name + "-scheduledReporter-" + threadNum);
				thread.setDaemon(true);
				return thread;
			}
		};

		return Executors.newSingleThreadScheduledExecutor(threadFactory);
	}

	/**
	 * Set period of reporting.
	 *
	 * @param duration duration of a period
	 * @param timeUnit timeunit of a period
	 * @return scheduled reporter instance
	 */
	public R every(long duration, TimeUnit timeUnit) {
		setDuration(duration);
		setTimeUnit(timeUnit);
		return (R) this;
	}

	private void setDuration(long duration) {
		if (duration <= 0) {
			throw new IllegalArgumentException("Duration should be positive");
		}

		this.duration = duration;
	}

	private void setTimeUnit(TimeUnit timeUnit) {
		if (timeUnit == null) {
			throw new IllegalArgumentException("Time unit should not be null");
		}

		this.timeUnit = timeUnit;
	}

	/**
	 * Get duration of the reporting period.
	 *
	 * @return duration of the reporting period
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Get time unit of the reporting period.
	 *
	 * @return time unit of the reporting period
	 */
	public TimeUnit getTimeUnit() {
		return timeUnit;
	}

	/**
	 * Get locale.
	 * @return locale used by the reporter
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Set locale used by the reporter.
	 *
	 * @param locale locale used by the reporter
	 * @return this scheduled reporter
	 */
	public R locale(Locale locale) {
		if (locale == null) {
			throw new IllegalArgumentException("Locale should not be null");
		}

		this.locale = locale;
		return (R) this;
	}

	/**
	 * Get filter that is used to select a subset of Simons to report about.
	 * @return filter that is used to select a subset of Simons to report about
	 */
	public SimonFilter getFilter() {
		return filter;
	}

	/**
	 * Set filter that will be used to select subset of Simons to report about.
	 * Only Simons that will be passed by the specified filter will be used during reporting.
	 *
	 * @param filter that
	 * @return
	 */
	public R filter(SimonFilter filter) {
		this.filter = filter;
		return (R) this;
	}

	/**
	 * Set manager instance.
	 * @param manager manager instance
	 */
	void setManager(Manager manager) {
		if (manager == null) {
			throw new IllegalArgumentException("Manager should not be null");
		}

		this.manager = manager;
	}

	protected Manager getManager() {
		return manager;
	}

	ScheduledExecutorService getExecutorService() {
		return executorService;
	}

	/**
	 * Set an instance of executor service that will be used by the reporter.
	 * @param executorService instance of executor service that will be used by the reporter.
	 * @return this scheduled reporter
	 */
	R setExecutorService(ScheduledExecutorService executorService) {
		if (executorService == null) {
			throw new IllegalArgumentException("Executor service should be not null");
		}

		this.executorService = executorService;
		return (R) this;
	}

	/**
	 * Set name of the reporter.
	 *
	 * @param name name of the reporter
	 * @return this scheduled reporter
	 */
	public R name(String name) {
		this.name = name;
		return (R) this;
	}

	/**
	 * Get name of this reporter.
	 *
	 * @return name of this reporter
	 */
	public String getName() {
		return name;
	}

	/**
	 * Runnable that is submitted to ScheduledExecutorService.
	 */
	class ReporterRunnable implements Runnable {
		public void run() {
			try {
				Collection<Simon> simons = manager.getSimons(filter);
				List<CounterSample> counterSamples = new ArrayList<CounterSample>();
				List<StopwatchSample> stopwatchSamples = new ArrayList<StopwatchSample>();

				for (Simon simon : simons) {
					if (simon instanceof Counter) {
						CounterSample counterSample = ((Counter) simon).sampleIncrement(name);
						if (incrementSample(counterSample)) {
							counterSample.setName(simon.getName());
							counterSamples.add(counterSample);
						}
					} else if (simon instanceof Stopwatch) {
						StopwatchSample stopwatchSample = ((Stopwatch) simon).sampleIncrement(name);
						if (incrementSample(stopwatchSample)) {
							stopwatchSample.setName(simon.getName());
							stopwatchSamples.add(stopwatchSample);
						}
					}
					// Ignore Simons of other types (e.g. UnknownSimon)
				}

				report(stopwatchSamples, counterSamples);
			} catch (RuntimeException e) {
				logger.error("Exception is scheduled reporter Runner. ", e);
				throw e;
			}
		}

		private boolean incrementSample(StopwatchSample stopwatchSample) {
			return stopwatchSample.getName() == null;
		}

		private boolean incrementSample(CounterSample counterSample) {
			return counterSample.getName() == null;
		}
	}

	ReporterRunnable createReporterRunner() {
		return new ReporterRunnable();
	}

	/**
	 * Method that is called when reporting should be done. Only Simons that pass filter specified during
	 * the reporter construction.
	 *
	 * @param stopwatchSamples stopwatches to report
	 * @param counterSamples counters to report
	 */
	protected abstract void report(List<StopwatchSample> stopwatchSamples, List<CounterSample> counterSamples);

	/**
	 * Start the reporter.
	 * If reporter has already been started it will cause <code>IllegalStateException</code>.
	 *
	 * @throws java.lang.IllegalStateException if the reporter has already been started
	 */
	public synchronized void start() {
		if (scheduledFuture == null || scheduledFuture.isCancelled()) {
			ReporterRunnable reporterRunner = createReporterRunner();

			onStart();
			// we execute Runnable immediately to create incremental Simons
			scheduledFuture = getExecutorService().scheduleWithFixedDelay(reporterRunner, 0, duration, timeUnit);
		} else {
			throw new IllegalStateException("Reporter has already been started");
		}
	}

	/**
	 * This method is called during {@link ScheduledReporter#start()}
	 * method execution before reporting task is scheduled.
	 * It can be used to allocated required resources such as connections, files, etc.
	 */
	protected abstract void onStart();

	/**
	 * Stop the reporter.
	 * If reporter has already been stopped  it will cause <code>IllegalStateException</code>.
	 *
	 * @throws java.lang.IllegalStateException if the reporter has already been started
	 */
	public synchronized void stop() {
		if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
			boolean interruptIfRunning = false;
			scheduledFuture.cancel(interruptIfRunning);
			onStop();
		} else {
			throw new IllegalStateException("Reporter has not been started");
		}
	}

	/**
	 * This method is called during {@link ScheduledReporter#stop()} method execution after the
	 * reporting task was cancelled.
	 * It can be used to free allocated resources such as connections, files, etc.
	 */
	protected abstract void onStop();
}
