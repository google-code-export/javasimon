package org.javasimon.proxy;

import java.util.Arrays;

/**
 * Proxy factory is used to produce proxies of any class
 */
public abstract class ProxyFactory {
	/**
	 * Proxy class identifier
	 */
	public static final class ProxyClass {
		private final ClassLoader classLoader;
		private final Class<?>[] interfaces;
		private final int hashCode;
		public ProxyClass(ClassLoader classLoader, Class<?> ... interfaces) {
			this.classLoader = classLoader;
			this.interfaces = interfaces;
			int lHashCode = classLoader.hashCode() * 31 + interfaces.length;
			for(Class interf:interfaces) {
				lHashCode = 31 * lHashCode + interf.hashCode();
			}
			this.hashCode=lHashCode;
		}

		public ClassLoader getClassLoader() {
			return classLoader;
		}

		public Class<?>[] getInterfaces() {
			return interfaces;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			final ProxyClass that = (ProxyClass) o;
			if (this.hashCode!=that.hashCode) {
				return false;
			}
			if (!this.classLoader.equals(that.classLoader)) {
				return false;
			}
			if (!Arrays.equals(this.interfaces, that.interfaces)) {
				return false;
			}
			return true;
		}
		@Override
		public int hashCode() {
			return hashCode;
		}
	}
	/**
	 * Create a proxy using given classloader and interfaces
	 *
	 * @param proxyHandler Proxy invocation handler
	 * @param proxyClass  Class loader + Interfaces to implement
	 * @return Proxy
	 */
	public abstract Object newProxy(DelegatingProxyHandler<?> proxyHandler, ProxyClass proxyClass);
	/**
	 * Create a proxy using given classloader and interfaces
	 *
	 * @param proxyHandler Proxy invocation handler
	 * @param classLoader  Class loader
	 * @param interfaces   Interfaces to implement
	 * @return Proxy
	 */
	public Object newProxy(DelegatingProxyHandler<?> proxyHandler, ClassLoader classLoader, Class<?>... interfaces) {
		return newProxy(proxyHandler, new ProxyClass(classLoader,  interfaces));
	}

	/**
	 * Create a proxy using given classloader and interfaces.
	 * Current thread class loaded is used as default classload.
	 *
	 * @param interfaces Interfaces to implement
	 * @return Proxy
	 */
	public Object newProxy(DelegatingProxyHandler<?> proxyHandler, Class<?>... interfaces) {
		return newProxy(proxyHandler, Thread.currentThread().getContextClassLoader(), interfaces);
	}


	/**
	 * Create a proxy using given classloader and interfaces
	 *
	 * @param interfaces Interface to implement
	 * @return Proxy
	 */
	public <X> X newProxy(DelegatingProxyHandler<?> proxyHandler, Class<X> interfaces) {
		return (X) newProxy(proxyHandler, new Class[]{interfaces});
	}
}
