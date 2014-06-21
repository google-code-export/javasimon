package org.javasimon.report.graphite;

import org.javasimon.CounterSample;
import org.javasimon.SimonException;
import org.javasimon.StopwatchSample;
import org.mockito.InOrder;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.net.SocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class GraphitePlaintextClientTest {

	private static final String TEST_HOST = "test.host";
	private static final int TEST_PORT = 1234;
	private static final InetSocketAddress TEST_ADDRESS = new InetSocketAddress(TEST_HOST, TEST_PORT);

	private SocketFactory socketFactory;
	private Socket socket;
	private SampleToPath sampleToPath;
	private ByteArrayOutputStream outputStream;

	private GraphitePlaintextClient graphiteClient;


	@BeforeMethod
	public void beforeMethod() throws IOException {
		socketFactory = mock(SocketFactory.class);
		socket = mock(Socket.class);
		sampleToPath = mock(SampleToPath.class);
		outputStream = new ByteArrayOutputStream();

		when(socketFactory.createSocket(TEST_HOST, TEST_PORT)).thenReturn(socket);
		when(socket.getOutputStream()).thenReturn(outputStream);

		graphiteClient = new GraphitePlaintextClient(TEST_ADDRESS, socketFactory, sampleToPath);
	}

	@Test
	public void testGetAddress() {
		Assert.assertEquals(graphiteClient.getServerAddress(), TEST_ADDRESS);
	}

	@Test
	public void testCreateSocketOnConnect() throws IOException {
		graphiteClient.connect();

		verify(socketFactory).createSocket(TEST_HOST, TEST_PORT);
		Assert.assertEquals(graphiteClient.getSocket(), socket) ;
	}

	@Test(expectedExceptions = SimonException.class)
	public void testSimonExceptionThrownOnIOException() throws IOException {
		//noinspection unchecked
		when(socketFactory.createSocket(TEST_HOST, TEST_PORT)).thenThrow(IOException.class);
		graphiteClient.connect();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testDoubleConnect() {
		graphiteClient.connect();
		graphiteClient.connect();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testCloseWithoutConnect() {
		graphiteClient.close();
	}

	@Test
	public void testClose() throws IOException {
		OutputStream outputStreamMock = mock(OutputStream.class);
		when(socket.getOutputStream()).thenReturn(outputStreamMock);

		graphiteClient.connect();
		graphiteClient.close();

		InOrder inOrder = inOrder(socket, outputStreamMock);

		inOrder.verify(outputStreamMock).flush();
		inOrder.verify(outputStreamMock).close();
		inOrder.verify(socket).close();

		Assert.assertNull(graphiteClient.getSocket());
	}

	@Test(expectedExceptions = SimonException.class)
	public void testExceptionOnWriterFlush() throws IOException {
		OutputStream outputStreamMock = mock(OutputStream.class);
		when(socket.getOutputStream()).thenReturn(outputStreamMock);
		doThrow(IOException.class).when(outputStreamMock).flush();

		graphiteClient.connect();
		graphiteClient.close();

	}

	@Test(expectedExceptions = SimonException.class)
	public void testExceptionOnWriterClose() throws IOException {
		OutputStream outputStreamMock = mock(OutputStream.class);
		when(socket.getOutputStream()).thenReturn(outputStreamMock);
		doThrow(IOException.class).when(outputStreamMock).close();

		graphiteClient.connect();
		graphiteClient.close();

	}

	@Test(expectedExceptions = SimonException.class)
	public void testExceptionOnSocketClose() throws IOException {
		OutputStream outputStreamMock = mock(OutputStream.class);
		when(socket.getOutputStream()).thenReturn(outputStreamMock);
		doThrow(IOException.class).when(socket).close();

		graphiteClient.connect();
		graphiteClient.close();
	}

	@Test
	public void testReportStopwatchSamples() {
		graphiteClient.connect();

		StopwatchSample sample = new StopwatchSample();
		sample.setName("stopwatch.name");
		sample.setNote("note");
		sample.setFirstUsage(50);
		sample.setLastUsage(500);
		sample.setTotal(100);
		sample.setMin(2);
		sample.setMax(10);
		sample.setMinTimestamp(200);
		sample.setMaxTimestamp(300);
		sample.setActive(3);
		sample.setMaxActive(10);
		sample.setMaxActiveTimestamp(400);
		sample.setLast(5);
		sample.setMean(8.2);
		sample.setStandardDeviation(4.2);
		sample.setVariance(2.2);
		sample.setVarianceN(2.2);

		long timestamp = 12345;

		when(sampleToPath.getPath(sample)).thenReturn("simon.path");

		graphiteClient.send(timestamp, Arrays.asList(sample), Collections.<CounterSample>emptyList());

		String sentString = new String(outputStream.toByteArray());
		Assert.assertEquals(sentString, lines(
				"simon.path.total 100 12",
				"simon.path.min 2 12",
				"simon.path.max 10 12",
				"simon.path.active 3 12",
				"simon.path.maxActive 10 12",
				"simon.path.mean 8.2 12",
				"simon.path.stdDev 4.2 12",
				"simon.path.variance 2.2 12",
				"simon.path.varianceN 2.2 12"
		));
	}

	private String lines(String... strings) {
		StringBuilder result = new StringBuilder();
		for (String line : strings) {
			result.append(line);
			result.append('\n');
		}

		return result.toString();
	}

	@Test
	public void testReportCounterSamples() {
		graphiteClient.connect();

		CounterSample sample = new CounterSample();
		sample.setName("counter.name");
		sample.setNote("note");
		sample.setFirstUsage(50);
		sample.setLastUsage(500);
		sample.setCounter(1);
		sample.setMin(2);
		sample.setMax(10);
		sample.setMinTimestamp(200);
		sample.setMaxTimestamp(300);
		sample.setIncrementSum(3);
		sample.setDecrementSum(2);

		long timestamp = 12345;

		when(sampleToPath.getPath(sample)).thenReturn("simon.path");

		graphiteClient.send(timestamp, Collections.<StopwatchSample>emptyList(), Arrays.asList(sample));

		String sentString = new String(outputStream.toByteArray());
		Assert.assertEquals(sentString, lines(
				"simon.path.count 1 12",
				"simon.path.min 2 12",
				"simon.path.max 10 12",
				"simon.path.incrementSum 3 12",
				"simon.path.decrementSum 2 12"
		));
	}

	@Test(expectedExceptions = SimonException.class)
	public void testIOExceptionOnWrite() throws IOException {
		OutputStream outputStreamMock = mock(OutputStream.class);
		when(socket.getOutputStream()).thenReturn(outputStreamMock);
		doThrow(IOException.class).when(outputStreamMock).write(anyInt());
		doThrow(IOException.class).when(outputStreamMock).write(any(byte[].class));
		doThrow(IOException.class).when(outputStreamMock).write(any(byte[].class), anyInt(), anyInt());

		graphiteClient.connect();

		CounterSample sample = new CounterSample();
		long timestamp = 12345;
		when(sampleToPath.getPath(sample)).thenReturn("simon.path");
		graphiteClient.send(timestamp, Collections.<StopwatchSample>emptyList(), Arrays.asList(sample));
	}

	@Test
	public void testDefaultSimonToPathParamsCtor() {
		GraphitePlaintextClient client = new GraphitePlaintextClient(TEST_ADDRESS, socketFactory);
		SampleToPathImpl simonToPath = (SampleToPathImpl) client.getSampleToPath();

		Assert.assertEquals(simonToPath.getPrefix(), "");
	}

	@Test
	public void testOneParamsCtor() {
		GraphitePlaintextClient client = new GraphitePlaintextClient(TEST_ADDRESS);
		Assert.assertEquals(
				client.getSocketFactory(), SocketFactory.getDefault());

		SampleToPathImpl simonToPath = (SampleToPathImpl) client.getSampleToPath();
		Assert.assertEquals(simonToPath.getPrefix(), "");
	}

	@Test
	public void testDefaultSocketFactoryParamsCtor() {
		GraphitePlaintextClient client = new GraphitePlaintextClient(TEST_ADDRESS, sampleToPath);

		Assert.assertEquals(
				client.getSocketFactory(), SocketFactory.getDefault());

		Assert.assertEquals(client.getSampleToPath(), sampleToPath);
	}
}
