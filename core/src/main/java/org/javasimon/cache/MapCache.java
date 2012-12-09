package org.javasimon.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache implementation base on Concurrent hash map.
 */
public class MapCache<K, V> implements Cache<K, V> {
	/**
	 * Storage map
	 */
	private final Map<K, MapCacheEntry<K, V>> map = new ConcurrentHashMap<K, MapCacheEntry<K, V>>();
	/**
	 * Cache loader
	 */
	private final CacheLoader<K, V> loader;
	/**
	 * Max number of entries in the cache
	 */
	private final Integer size;
	/**
	 * Time for an entry to live in cache before being expired and purged
	 */
	private final Long timeToLive;

	/**
	 * Constructor
	 *
	 * @param loader     Cache loader
	 * @param size       Max number of entries in the cache
	 * @param timeToLive Time for an entry to live in cache before being expired and purged
	 */
	public MapCache(CacheLoader<K, V> loader, Integer size, Long timeToLive) {
		this.loader = loader;
		this.size = size;
		this.timeToLive = timeToLive;
	}

	/**
	 * Get a value from key.
	 * In case of cache miss and cache max size is reached, all expired entries
	 * are remove and if needed the oldest one as well. Hence cache looks like a
	 * LRU
	 *
	 * @param key Key
	 * @return Value corresponding to key
	 */
	@Override
	public V get(K key) {
		MapCacheEntry<K, V> entry;
		V value = null;
		final long timestamp = System.currentTimeMillis();
		entry = map.get(key);
		if (entry == null) {
			// I expect to have cache hit most of the time, and prefer to load many times the same value
			// in case multiple threads face cache hit on the same key
			synchronized (map) {
				// Cache miss
				if (!(size == null || map.size() < size)) {
					removeAllExpiredOrOldest();
				}
				value = loader.load(key);
				entry = new MapCacheEntry<K, V>(key, value, timestamp);
				map.put(key, entry);
			}
		}

		if (value == null) {
			// Cache hit
			synchronized (entry) {
				if (isExpired(entry, timestamp)) {
					// Refresh
					value = loader.load(key);
					entry.setValue(value, timestamp);
				} else {
					value = entry.getValue(timestamp);
				}
			}
		}
		return value;
	}

	/**
	 * Test whether un entry is expired at given timestamp
	 *
	 * @param entry     Entry
	 * @param timestamp Timestamp
	 * @return true if expired
	 */
	private boolean isExpired(MapCacheEntry<K, V> entry, long timestamp) {
		return timeToLive != null && timestamp > entry.getUseTimestamp() + timeToLive;
	}

	@Override
	public void remove(K key) {
		map.remove(key);
	}

	@Override
	public void removeAll() {
		map.clear();
	}

	/**
	 * Called when theres is not enough room in the cache for a new entry.
	 * <ol>
	 * <li>First, all expired entries are removed</li>
	 * <li>If no entry was expired, the oldest is removed.</li>
	 * </ol>
	 */
	private void removeAllExpiredOrOldest() {
		// Remove expired entries and find oldest entry
		final long nowTimestamp = System.currentTimeMillis();
		final Long expireTimestamp = timeToLive == null ? null : nowTimestamp - timeToLive;
		int removeCount = 0;
		MapCacheEntry<K,V> oldestEntry=null;
		final Iterator<MapCacheEntry<K,V>> entryIterator=map.values().iterator();
		while (entryIterator.hasNext()) {
			MapCacheEntry<K, V> entry=entryIterator.next();
			if (expireTimestamp != null && entry.getUseTimestamp() < expireTimestamp) {
				// Remove expired entry
				entryIterator.remove();
				removeCount++;
			} else if (oldestEntry==null || entry.getUseTimestamp()<oldestEntry.getUseTimestamp()) {
				// Find oldest entry
				oldestEntry=entry;
			}
		}
		// Remove oldest entry if needed
		if (removeCount==0 && oldestEntry!=null) {
			map.remove(oldestEntry.getKey());
		}
	}
}