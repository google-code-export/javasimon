package org.javasimon.cache;

/**
 * No-op cache cache implementation
 */
public class NoCache<K,V> implements Cache<K,V> {
	private final CacheLoader<K,V> loader;

	public NoCache(CacheLoader<K, V> loader) {
		this.loader = loader;
	}

	@Override
	public V get(K key) {
		return loader.load(key);
	}

	@Override
	public void remove(K key) {
	}

	@Override
	public void removeAll() {
	}
}
