package org.javasimon.cache;

import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

/**
 * Unit test for {@link MapCache}
 */
public class MapCacheTest {
	/**
	 * Test that thanks cache, values are not loaded twice
	 */
	@Test
	public void testCacheEffect() {
		CacheLoader<Integer, String> cacheLoaderMock = mock(CacheLoader.class);
		Cache<Integer, String> cache = new MapCache<Integer, String>(cacheLoaderMock, 10, null);
		when(cacheLoaderMock.load(eq(1))).thenReturn("One");
		assertEquals("One", cache.get(1));
		assertEquals("One", cache.get(1));
		verify(cacheLoaderMock, times(1)).load(eq(1));
	}

	/**
	 * Test that when cache size is fixed, as soon as cache is fixed some entries en removed
	 */
	@Test
	public void testCacheSize() throws InterruptedException {
		CacheLoader<Integer, String> cacheLoaderMock = mock(CacheLoader.class);
		Cache<Integer, String> cache = new MapCache<Integer, String>(cacheLoaderMock, 3, null);
		when(cacheLoaderMock.load(eq(1))).thenReturn("One");
		when(cacheLoaderMock.load(eq(2))).thenReturn("Two");
		when(cacheLoaderMock.load(eq(3))).thenReturn("Three");
		when(cacheLoaderMock.load(eq(4))).thenReturn("Four");
		assertEquals("One", cache.get(1));
		Thread.sleep(50L); // Just to ensure they have different use timestamps
		assertEquals("Two", cache.get(2));
		Thread.sleep(50L);
		assertEquals("Three", cache.get(3));
		Thread.sleep(50L);
		assertEquals("Four", cache.get(4));
		Thread.sleep(50L);
		assertEquals("One", cache.get(1));
		verify(cacheLoaderMock, times(2)).load(eq(1));
		verify(cacheLoaderMock, times(1)).load(eq(2));
		verify(cacheLoaderMock, times(1)).load(eq(3));
		verify(cacheLoaderMock, times(1)).load(eq(4));
	}

	/**
	 * Test that when cache TTL is fixed, as soon as cache entry expires, the value is reloaded
	 */
	@Test
	public void testCacheExpiration() throws InterruptedException {
		CacheLoader<Integer, String> cacheLoaderMock = mock(CacheLoader.class);
		Cache<Integer, String> cache = new MapCache<Integer, String>(cacheLoaderMock, 10, 100L);
		when(cacheLoaderMock.load(eq(1))).thenReturn("One");
		assertEquals("One", cache.get(1));
		Thread.sleep(200L);
		assertEquals("One", cache.get(1));
		verify(cacheLoaderMock, times(2)).load(eq(1));
	}

	/**
	 * Test that when cache size is fixed, as soon as cache is fixed some entries en removed
	 */
	@Test
	public void testCacheSizeAndExpiration() throws InterruptedException {
		CacheLoader<Integer, String> cacheLoaderMock = mock(CacheLoader.class);
		Cache<Integer, String> cache = new MapCache<Integer, String>(cacheLoaderMock, 3, 100L);
		when(cacheLoaderMock.load(eq(1))).thenReturn("One");
		when(cacheLoaderMock.load(eq(2))).thenReturn("Two");
		when(cacheLoaderMock.load(eq(3))).thenReturn("Three");
		assertEquals("One", cache.get(1));
		assertEquals("Two", cache.get(2));
		assertEquals("Three", cache.get(3));
		Thread.sleep(200L);
		assertEquals("One", cache.get(1));
		assertEquals("Two", cache.get(2));
		verify(cacheLoaderMock, times(2)).load(eq(1));
		verify(cacheLoaderMock, times(2)).load(eq(2));
		verify(cacheLoaderMock, times(1)).load(eq(3));
	}
}
