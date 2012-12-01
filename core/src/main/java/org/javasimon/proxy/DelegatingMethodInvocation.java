package org.javasimon.proxy;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Proxy method invocation
 * @author gquintana
 */
public final class DelegatingMethodInvocation<T> implements Delegating<T>, Runnable, Callable<Object> {
	/**
	 * Target (real) object
	 */
	private final T delegate;
	/**
	 * Proxy
	 */
	private final Object proxy;
	/**
	 * Method
	 */
	private final Method method;
	/**
	 * Invocation arguments
	 */
	private final Object[] args;

	public DelegatingMethodInvocation(T target,Object proxy, Method method, Object... args) {
		this.delegate = target;
		this.proxy = proxy;
		this.method = method;
		this.args = args;
	}

	public Object[] getArgs() {
		return args;
	}
    public int getArgCount() {
        return args==null?0:args.length;
    }
    public Object getArgAt(int argIndex) {
        return args[argIndex];
    }
    public <R> R getArgAt(int argIndex, Class<R> argType) {
        return argType.cast(getArgAt(argIndex));
    }
    public Method getMethod() {
		return method;
	}
    public String getMethodName() {
        return method.getName();
    }

    public Object getProxy() {
		return proxy;
	}
	
	public T getDelegate() {
		return delegate;
	}
	public Method getTargetMethod() throws NoSuchMethodException {
		return delegate.getClass().getMethod(method.getName(), method.getParameterTypes());
	}
	public Object proceed() throws Throwable {
		return method.invoke(delegate, args);
	}

	public void run() {
		try {
			proceed();
		} catch(Throwable throwable) {
			// Forget exception
		}
	}

	public Object call() throws Exception {
		try {
			return proceed();
		} catch (Exception exception) {
			throw exception;
		} catch (Throwable throwable) {
			throw new IllegalStateException(throwable);
		}
	}


}
