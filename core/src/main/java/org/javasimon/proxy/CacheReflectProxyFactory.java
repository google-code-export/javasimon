package org.javasimon.proxy;


import org.javasimon.cache.Cache;
import org.javasimon.cache.CacheLoader;
import org.javasimon.cache.MapCache;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/**
 * Factory of proxies producing proxies based on Java reflection
 * using cache on proxified classes.
 */
public class CacheReflectProxyFactory extends ProxyFactory {
	private static final CacheLoader<ProxyClass,Constructor> CONSTRUCTOR_CACHE_LOADER=new CacheLoader<ProxyClass, Constructor>() {
		@Override
		public Constructor load(ProxyClass key) {
			try {
				final Class clazz=Proxy.getProxyClass(key .getClassLoader(), key.getInterfaces());
				return clazz.getConstructor(InvocationHandler.class);
			} catch (NoSuchMethodException e) {
				throw new ProxyException(e);
			}
		}
	};
	/**
	 * Constructor cache is map proxy type &rarr;> proxy constructor
	 */
	private final Cache<ProxyClass,Constructor> constructorCache =new MapCache<ProxyClass, Constructor>(CONSTRUCTOR_CACHE_LOADER, null, null);
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object newProxy(DelegatingProxyHandler<?> proxyHandler, ProxyClass proxyClass)  {
		try {
			return constructorCache.get(proxyClass).newInstance(proxyHandler);
		} catch (InstantiationException e) {
			throw new ProxyException(e);
		} catch (IllegalAccessException e) {
			throw new ProxyException(e);
		} catch (InvocationTargetException e) {
			throw new ProxyException(e);
		}
	}
}
