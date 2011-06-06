package com.sql.dynamicquery.annotationprocessor.tests;

import java.lang.reflect.Proxy;

import com.sql.dynamicquery.TableProxy;
import testcol.*;

public class TestClass
{
	public static TestCol getTestCol()
	{
		return (TestCol) Proxy.newProxyInstance(TableProxy.class.getClassLoader(), new Class[] {TestCol.class}, new TableProxy(TestCol.class));
	}
	
	public static void main(String[] args)
	{
		TestCol c = getTestCol();
		System.out.print(c.getId());
		c.id();
	}
}
