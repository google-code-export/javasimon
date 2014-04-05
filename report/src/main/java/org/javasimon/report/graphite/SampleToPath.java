package org.javasimon.report.graphite;

import org.javasimon.Sample;

/**
 * Interface for mapping a Sample to a path in a Graphite data tree.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public interface SampleToPath {
	/**
	 * Map sample to a path in Graphite data tree.
	 *
	 * @param sample sample from a Simon that should be mapped to a path in Graphite tree
	 * @return a path in a Graphite tree where components of a path separated by dots
	 */
	String getPath(Sample sample);
}
