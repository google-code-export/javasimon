package org.javasimon.cache;

import java.util.Comparator;

/**
 * Entry in the cache: value with its expiration details.
 */
final class MapCacheEntry<K,V> {
	/**
	 * Key
	 */
	private final K key;
	/**
	 * Value
	 */
	private V value;
	/**
	 * When this value was used
	 */
	private long useTimestamp;

	public MapCacheEntry(K key, V value, long useTimestamp) {
		this.key = key;
		setValue(value, useTimestamp);
	}

	/**
	 * Get value
	 * @param useTimestamp Use timestamp
	 * @return Value
	 */
	public V getValue(long useTimestamp) {
		this.useTimestamp = useTimestamp;
		return value;
	}

	/**
	 * Set value and update load/expire timestamps
	 * @param value New value
	 * @param useTimestamp Use timestamp
	 */
	public void setValue(V value, long useTimestamp) {
		this.value = value;
		this.useTimestamp = useTimestamp;
	}

	/**
	 * Get timestamp when the value was used
	 * @return Use timestamp
	 */
	public long getUseTimestamp() {
		return useTimestamp;
	}

	/**
	 * Get key
	 * @return Key
	 */
	public K getKey() {
		return key;
	}

	public static final Comparator<MapCacheEntry> USE_TIMESTAMP_COMPARATOR=new Comparator<MapCacheEntry>() {
		@Override
		public int compare(MapCacheEntry o1, MapCacheEntry o2) {
			return Long.valueOf(o1.useTimestamp).compareTo(o2.useTimestamp);
		}
	};
}
