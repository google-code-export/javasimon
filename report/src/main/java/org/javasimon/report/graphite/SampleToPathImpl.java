package org.javasimon.report.graphite;

import org.javasimon.Sample;

/**
 * Implementation of <code>SampleToPath</code> that forms a path in Graphite tree
 * by prepending prefix to a Simon's name. A prefix can be used to separate samples from Simons
 * with a same name, belonging for example to different managers or servers. If prefix is null or empty
 * {@link SampleToPathImpl#getPath(org.javasimon.Sample)} returns a name of a Simon.
 *
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class SampleToPathImpl implements SampleToPath {

	/** Prefix that will be prepended to the name of a Sample */
	private String prefix;

	/**
	 * Constructor.
	 *
	 * @param prefix prefix that will be prepended
	 */
	public SampleToPathImpl(String prefix) {
		this.prefix = prefix;

		if (this.prefix == null) {
			this.prefix = "";
		}

		if (this.prefix.endsWith(".")) {
			this.prefix = this.prefix.substring(0, this.prefix.length() - 1);
		}
	}

	@Override
	public String getPath(Sample sample) {
		if (prefix.isEmpty()) {
			return sample.getName();
		}

		return prefix + '.' + sample.getName();
	}

	/**
	 * Gets prefix that is prepended to a Simon's name
	 *
	 * @return prefix that is prepended to a Simon's name
	 */
	public String getPrefix() {
		return prefix;
	}
}
