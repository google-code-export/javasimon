package org.javasimon.proxy;
import org.testng.annotations.Test;

import java.sql.Statement;

import static org.testng.Assert.*;
/**
 * Unit test for {@link ProxyFactory} and {@link ProxyFactory.ProxyClass}
 */
public class ProxyFactoryTest {
	@Test
	public void testProxyClass() {
		ProxyFactory.ProxyClass proxyClass1=new ProxyFactory.ProxyClass(Thing.class.getClassLoader(),Thing.class);

		ProxyFactory.ProxyClass proxyClass2=new ProxyFactory.ProxyClass(Thing.class.getClassLoader(),Thing.class);
		assertEquals(proxyClass1, proxyClass2);

		ProxyFactory.ProxyClass proxyClass3=new ProxyFactory.ProxyClass(Thing.class.getClassLoader(),Statement.class);
		assertNotEquals(proxyClass1, proxyClass3);

		ProxyFactory.ProxyClass proxyClass4=new ProxyFactory.ProxyClass(Thing.class.getClassLoader(),Thing.class, Statement.class);
		assertNotEquals(proxyClass1, proxyClass4);
	}
}
