package org.javasimon.proxy;

import java.lang.reflect.Proxy;

/**
 * Factory of proxies producing proxies based on Java reflection
 */
public class ReflectProxyFactory extends ProxyFactory {
	/**
	 * {@inheritDoc}
	 */
	public Object newProxy(DelegatingProxyHandler<?> proxyHandler, ProxyClass proxyClass) {
		return Proxy.newProxyInstance(proxyClass.getClassLoader(), proxyClass.getInterfaces(), proxyHandler);
	}
}
