package org.javasimon.report;

/**
 * Interface for getting current time.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public interface TimeSource {

	/**
	 * Gets current time in ms.
	 *
	 * @return current time in ms
	 */
	long getTime();
}
