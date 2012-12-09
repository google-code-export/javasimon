package org.javasimon.cache;

/**
 * Cache interface
 * @param <K> Key
 * @param <V> Value
 */
public interface Cache<K, V> {
	/**
	 * Read an entry in cache, triggering loading if
	 * required.
	 * @param key Key
	 * @return value
	 */
	V get(K key);

	/**
	 * Remove an entry from cache
	 * @param key Key
	 */
	void remove(K key);

	/**
	 * Remove all entries
	 */
	void removeAll();
}
