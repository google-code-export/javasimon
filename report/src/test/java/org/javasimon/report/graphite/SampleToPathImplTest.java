package org.javasimon.report.graphite;

import org.javasimon.StopwatchSample;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:ivan.mushketyk@gmail.com">Ivan Mushketyk</a>
 */
public class SampleToPathImplTest {

	@DataProvider(name = "prefixPath")
	public Object[][] prefixPath() {
		return new Object[][] {
				{"", "a.b.c", "a.b.c"},
				{"z", "a.b.c", "z.a.b.c"},
				{"z.", "a.b.c", "z.a.b.c"},
				{null, "a.b.c", "a.b.c"}
		};
	}

	@Test(dataProvider = "prefixPath")
	public void testGetPath(String prefix, String simonName, String expectedPath) {
		SampleToPathImpl simonToPath = new SampleToPathImpl(prefix);
		StopwatchSample sample = new StopwatchSample();
		sample.setName(simonName);

		Assert.assertEquals(simonToPath.getPath(sample), expectedPath);
	}




}
