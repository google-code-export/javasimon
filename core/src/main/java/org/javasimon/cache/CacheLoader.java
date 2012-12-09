package org.javasimon.cache;

/**
 * Cache loader is in charge of providing value to fill the case in case of cache miss.
 */
public interface CacheLoader<K,V> {
	/**
	 * Load value corresponding to key
	 * @param key Key
	 * @return Loaded value
	 */
	V load(K key);
}
