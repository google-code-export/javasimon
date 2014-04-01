package org.javasimon.report;

/**
 * Time source that returns system time.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class SystemTimeSource implements TimeSource {


	@Override
	public long getTime() {
		return System.currentTimeMillis();
	}
}
