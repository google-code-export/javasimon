package org.javasimon.report.graphite;

import org.javasimon.SimonException;
import org.javasimon.StopwatchSample;
import org.javasimon.CounterSample;

import javax.net.SocketFactory;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * Implementation of Graphite client that uses plaintext protocol.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class GraphitePlaintextClient implements GraphiteClient {
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
	 * Constructor.
	 *
	 * @param serverAddress address of a Graphite server
	 * @param socketFactory socket factory that will be used to get a socket to communicate with a Graphite server
	 * @param sampleToPath converter of samples to pathes in Graphite tree
	 */
	public GraphitePlaintextClient(InetSocketAddress serverAddress, SocketFactory socketFactory, SampleToPath sampleToPath) {
		this.serverAddress = serverAddress;
		this.socketFactory = socketFactory;
		this.sampleToPath = sampleToPath;
	}

	@Override
	public void connect() {
		if (socket != null) {
			throw new IllegalStateException("Connection has already been established");
		}

		try {
			socket = socketFactory.createSocket(serverAddress.getHostName(), serverAddress.getPort());
			writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			throw new SimonException(e);
		}
	}

	@Override
	public void send(long timestamp, List<StopwatchSample> stopwatchSamples, List<CounterSample> counterSamples) {
		try {
			for (StopwatchSample stopwatchSample : stopwatchSamples) {
				sendSample(timestamp, stopwatchSample);
			}

			for (CounterSample counterSample : counterSamples) {
				sendSample(timestamp, counterSample);
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
		sendSample(path(simonPath, "incrementSum"),counterSample.getIncrementSum(), timestamp);
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

	private void sendSample(String path, Object val, long timestamp) throws IOException {
		writer.write(path);
		writer.write(' ');
		writer.write(val.toString());
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
