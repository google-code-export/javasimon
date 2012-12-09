package org.javasimon.proxy;

import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Proxy factory based on CGLib
 */
public class CGLibProxyFactory extends ProxyFactory {
	private Map<ProxyClass, Class> proxyClasses=new ConcurrentHashMap<ProxyClass, Class>();
	public static final Class[] ADAPTER_CALLBACK_TYPES = new Class[]{
			AdapterMethodInterceptor.class,
			AdapterLazyLoader.class
	};
	private static class AdapterCallbackFilter implements CallbackFilter {
		private final DelegatingProxyHandler.InvocationFilter invocationFilter;
		private AdapterCallbackFilter(DelegatingProxyHandler.InvocationFilter invocationFilter) {
			this.invocationFilter = invocationFilter;
		}

		@Override
		public int accept(Method method) {
			// 0 is AdapterMethodInterceptor
			// 1 is AdapterLazyLoader
			return invocationFilter.isIntercepted(method)?0:1;
		}
	}
	private static class AdapterMethodInterceptor implements MethodInterceptor {
		private final DelegatingProxyHandler proxyHandler;
		private AdapterMethodInterceptor(DelegatingProxyHandler proxyHandler) {
			this.proxyHandler = proxyHandler;
		}
		@Override
		public Object intercept(Object proxy, Method method, Object[] arguments, MethodProxy methodProxy) throws Throwable {
			return proxyHandler.invoke(proxy, method, arguments);
		}
	}
	private static class AdapterLazyLoader<T> implements LazyLoader {
		private final T delegate;
		private AdapterLazyLoader(T delegate) {
			this.delegate = delegate;
		}
		@Override
		public T loadObject() {
			return delegate;
		}
	}
	private Class getProxyClass(DelegatingProxyHandler<?> proxyHandler, ProxyClass proxyClass) {
		Class clazz=proxyClasses.get(proxyClass);
		if (clazz==null) {
			Enhancer enhancer = new Enhancer();
			enhancer.setCallbackFilter(new AdapterCallbackFilter(proxyHandler.getInvocationFilter()));
			enhancer.setCallbackTypes(ADAPTER_CALLBACK_TYPES);
			enhancer.setClassLoader(proxyClass.getClassLoader());
			enhancer.setInterfaces(proxyClass.getInterfaces());
			clazz=enhancer.createClass();
			proxyClasses.put(proxyClass, clazz);
		}
		return clazz;
	}
	@Override
	public Object newProxy(DelegatingProxyHandler<?> proxyHandler, ProxyClass proxyClass) {
		try {
			Object proxy=getProxyClass(proxyHandler, proxyClass).newInstance();
			((Factory) proxy).setCallbacks(new Callback[]{
					new AdapterMethodInterceptor(proxyHandler),
					new AdapterLazyLoader<Object>(proxyHandler.getDelegate())
			});
			return proxy;
		} catch (InstantiationException e) {
			throw new ProxyException(e);
		} catch (IllegalAccessException e) {
			throw new ProxyException(e);
		}
	}
}
