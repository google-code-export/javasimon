package org.javasimon.report;

import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.PrintWriter;

import static org.mockito.Mockito.mock;

/**
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class ConsoleReporterTest {

	private ConsoleReporter consoleReporter;
	private Manager manager;

	@BeforeMethod
	public void beforeMethod() {
		manager = mock(Manager.class);
		consoleReporter = ConsoleReporter.forManager(manager);
	}

	@Test
	public void testCreateForManager() {
		Assert.assertSame(consoleReporter.getManager(), manager);
	}

	@Test
	public void testCreateForDefaultManager() {
		ConsoleReporter reporter = ConsoleReporter.forDefaultManager();
		Assert.assertSame(reporter.getManager(), SimonManager.manager());
	}

	@Test
	public void testSetPrintWriter() {
		PrintWriter writer = mock(PrintWriter.class);
		consoleReporter.to(writer);
		Assert.assertEquals(consoleReporter.getWriter(), writer);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testSetNullPrintStream() {
		consoleReporter.to(null);
	}
}
