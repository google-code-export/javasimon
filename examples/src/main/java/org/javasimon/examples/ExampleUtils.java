package org.javasimon.examples;

import org.javasimon.Counter;
import org.javasimon.Manager;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;

import java.util.Random;

/**
 * Contains some supportive utils common for more examples.
 *
 * @author <a href="mailto:virgo47@gmail.com">Richard "Virgo" Richter</a>
 * @since 3.1
 */
public final class ExampleUtils {

	private static final Random RANDOM = new Random();

	private ExampleUtils() {
	}

	/**
	 * Method that lasts randomly from ~0 to the square of the specified amount of maxMsRoot.
	 * This is just to avoid linear randomness.
	 *
	 * @param maxMsRoot square root of the maximal waiting time
	 */
	public static void waitRandomlySquared(int maxMsRoot) {
		int random = RANDOM.nextInt(maxMsRoot);
		try {
			Thread.sleep(random * random + RANDOM.nextInt(maxMsRoot));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static final String[] RANDOM_NAMES = ("Deadra Breakell Rod Herrero Genesis Boutilier Cliff Daus Carey Chevas" +
		" Loralee Rizvi Virgen Pahler Un Muscat Elwood Poeppel Jeffry Carlise Kuramoto Bibi Whatcott Lianne Tellefson" +
		" Ruthanne Stipes Elwood Kisselburg Raphael Maxam Pura Abrecht Rod Jernberg Bok Mehrtens Brittanie Palamino" +
		" Jeffry Wansing Delsie Palms Rob Doub Moises Minney Armand Khaleel").split(" ");

	/**
	 * Fills the {@link org.javasimon.SimonManager} with specified number of Simons (or slightly more).
	 *
	 * @param roughCount how many Stopwatches to create (or a bit more)
	 */
	public static void fillManagerWithSimons(int roughCount) {
		System.out.print("Filling manager with ~" + roughCount + " Simons...");
		while (SimonManager.getSimonNames().size() < roughCount) {
			SimonManager.getStopwatch(generateRandomName(RANDOM.nextInt(10) + 1));
		}
		System.out.println(" " + SimonManager.getSimonNames().size() + " created.");
	}

	private static String generateRandomName(int depth) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < depth; i++) {
			if (sb.length() > 0) {
				sb.append(Manager.HIERARCHY_DELIMITER);
			}
			sb.append(RANDOM_NAMES[RANDOM.nextInt(RANDOM_NAMES.length)]);
		}
		return sb.toString();
	}

	private static final int RANDOM_WORK_INIT = 20;
	private static final int RANDOM_WORK_MAX = 50;
	private static final int RANDOM_WORK_STEP = 5;

	/**
	 * Generates random "work" on stopwatches with typical values smaller for first stopwatch and bigger for later.
	 *
	 * @param stopwatches array of stopwatches
	 */
	public static void randomWork(Stopwatch... stopwatches) {
		int waitRoot = RANDOM_WORK_INIT;
		for (Stopwatch stopwatch : stopwatches) {
			Split split = stopwatch.start();
			waitRandomlySquared(waitRoot);
			split.stop();
			waitRoot = nextRandomWork(waitRoot);
		}
	}

	/**
	 * Generates random "work" on counters with typical values smaller for first stopwatch and bigger for later.
	 * Increase or decrease is random.
	 *
	 * @param counters array of counters
	 */
	public static void randomWork(Counter... counters) {
		int countBase = RANDOM_WORK_INIT;
		for (Counter counter : counters) {
			int count = RANDOM.nextInt(countBase);
			if (RANDOM.nextBoolean()) {
				counter.increase(count);
			} else {
				counter.decrease(count);
			}
			countBase = nextRandomWork(countBase);
		}
	}

	private static int nextRandomWork(int randomWork) {
		randomWork += RANDOM_WORK_STEP;
		if (randomWork > RANDOM_WORK_MAX) {
			randomWork = RANDOM_WORK_INIT;
		}
		return randomWork;
	}
}
