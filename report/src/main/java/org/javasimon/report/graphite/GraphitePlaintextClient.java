package org.javasimon.report.graphite;

import org.javasimon.CounterSample;
import org.javasimon.SimonException;
import org.javasimon.StopwatchSample;
import org.javasimon.clock.ClockUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

import javax.net.SocketFactory;

/**
 * Implementation of Graphite client that uses plaintext protocol.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class GraphitePlaintextClient implements GraphiteClient {

	private static final Logger logger = LoggerFactory.getLogger(GraphitePlaintextClient.class);

	/** Used to build path for each Simon in Graphite tree */
	private final SampleToPath sampleToPath;

	/** Address of Graphite server */
	private final InetSocketAddress serverAddress;

	/** SocketFactory that is used to get a Socket instance */
	private final SocketFactory socketFactory;

	/** Socket that is used to send data to a Graphite server */
	private Socket socket;

	/** Writer that uses Socket to send data to a Graphite server */
	private Writer writer;

	/**
	 * Constructor. Creates an instance with default SocketFactory and <code>SampleToPath</code> that
	 * uses name of a sample as a path in a Graphite tree.
	 *
	 * @param serverAddress address of a Graphite server
	 */
	public GraphitePlaintextClient(InetSocketAddress serverAddress) {
		this(serverAddress, SocketFactory.getDefault());
	}

	/**
	 * Constructor. Creates an instance with <code>SampleToPath</code> that
	 * uses name of a sample as a path in a Graphite tree.
	 *
	 * @param serverAddress address of a Graphite server
	 * @param socketFactory socket factory that will be used to get a socket to communicate with a Graphite server
	 */
	public GraphitePlaintextClient(InetSocketAddress serverAddress, SocketFactory socketFactory) {
		this(serverAddress, socketFactory, new SampleToPathImpl(""));
	}

	/**
	 * Constructor. Creates an instance with default SocketFactory.
	 *
	 * @param serverAddress address of a Graphite server
	 * @param sampleToPath converter of samples to paths in Graphite tree
	 */
	public GraphitePlaintextClient(InetSocketAddress serverAddress, SampleToPath sampleToPath) {
		this(serverAddress, SocketFactory.getDefault(), sampleToPath);
	}

	/**
	 * Constructor.
	 *
	 * @param serverAddress address of a Graphite server
	 * @param socketFactory socket factory that will be used to get a socket to communicate with a Graphite server
	 * @param sampleToPath converter of samples to paths in Graphite tree
	 */
	public GraphitePlaintextClient(InetSocketAddress serverAddress, SocketFactory socketFactory, SampleToPath sampleToPath) {
		this.serverAddress = serverAddress;
		this.socketFactory = socketFactory;
		this.sampleToPath = sampleToPath;
	}

	@Override
	public void connect() {
		logger.info("Connecting to a Graphite server {}", serverAddress);

		if (socket != null) {
			throw new IllegalStateException("Connection has already been established");
		}

		try {
			socket = socketFactory.createSocket(serverAddress.getHostName(), serverAddress.getPort());
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8")));
		} catch (IOException e) {
			throw new SimonException(e);
		}
	}

	@Override
	public void send(long timestamp, List<StopwatchSample> stopwatchSamples, List<CounterSample> counterSamples) {
		logger.debug("Sending data to a Graphite server");

		long msTimestamp = timestamp / ClockUtils.MILLIS_IN_SECOND;

		try {
			for (StopwatchSample stopwatchSample : stopwatchSamples) {
				sendSample(msTimestamp, stopwatchSample);
			}

			for (CounterSample counterSample : counterSamples) {
				sendSample(msTimestamp, counterSample);
			}
		} catch (IOException e) {
			throw new SimonException(e);
		}
	}

	private void sendSample(long timestamp, CounterSample counterSample) throws IOException {
		String simonPath = sampleToPath.getPath(counterSample);

		sendSample(path(simonPath, "count"), counterSample.getCounter(), timestamp);
		sendSample(path(simonPath, "min"), counterSample.getMin(), timestamp);
		sendSample(path(simonPath, "max"), counterSample.getMax(), timestamp);
		sendSample(path(simonPath, "incrementSum"), counterSample.getIncrementSum(), timestamp);
		sendSample(path(simonPath, "decrementSum"), counterSample.getDecrementSum(), timestamp);
	}

	private void sendSample(long timestamp, StopwatchSample stopwatchSample) throws IOException {
		String simonPath = sampleToPath.getPath(stopwatchSample);

		sendSample(path(simonPath, "total"), stopwatchSample.getTotal(), timestamp);
		sendSample(path(simonPath, "min"), stopwatchSample.getMin(), timestamp);
		sendSample(path(simonPath, "max"), stopwatchSample.getMax(), timestamp);
		sendSample(path(simonPath, "active"), stopwatchSample.getActive(), timestamp);
		sendSample(path(simonPath, "maxActive"), stopwatchSample.getMaxActive(), timestamp);
		sendSample(path(simonPath, "mean"), stopwatchSample.getMean(), timestamp);
		sendSample(path(simonPath, "stdDev"), stopwatchSample.getStandardDeviation(), timestamp);
		sendSample(path(simonPath, "variance"), stopwatchSample.getVariance(), timestamp);
		sendSample(path(simonPath, "varianceN"), stopwatchSample.getVarianceN(), timestamp);
	}

	private void sendSample(String path, long val, long timestamp) throws IOException {
		sendSample(path, Long.toString(val), timestamp);
	}

	private void sendSample(String path, double val, long timestamp) throws IOException {
		String formattedDouble = formatDouble(val);
		sendSample(path, formattedDouble, timestamp);
	}

	private String formatDouble(double val) {
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		otherSymbols.setDecimalSeparator('.');

		DecimalFormat df = new DecimalFormat("#.##", otherSymbols);
		return df.format(val);
	}

	private void sendSample(String path, String val, long timestamp) throws IOException {
		writer.write(path);
		writer.write(' ');
		writer.write(val);
		writer.write(' ');
		writer.write(Long.toString(timestamp));
		writer.write('\n');
		writer.flush();
	}

	private String path(String simonPath, String metricName) {
		return simonPath + '.' + metricName;
	}

	@Override
	public void close() {
		logger.info("Disconnecting from a Graphite server {}", serverAddress);

		if (socket == null) {
			throw new IllegalStateException("close() should be called after connect()");
		}

		try {
			writer.flush();
			writer.close();
			socket.close();
		} catch (IOException e) {
			throw new SimonException(e);
		}

		socket = null;
		writer = null;
	}

	InetSocketAddress getServerAddress() {
		return serverAddress;
	}

	Socket getSocket() {
		return socket;
	}


	SampleToPath getSampleToPath() {
		return sampleToPath;
	}

	SocketFactory getSocketFactory() {
		return socketFactory;
	}
}
