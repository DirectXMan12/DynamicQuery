/**
 * 
 */
package com.sql.dynamicquery.tests;



import java.lang.reflect.Proxy;

import org.junit.Before;
import org.junit.Test;

import com.sql.dynamicquery.DynamicQuery;
import com.sql.dynamicquery.TableProxy;
import com.sun.tools.javac.tree.JCTree.Visitor;;
/**
 * @author DirectXMan12
 *
 */
public class BasicSqlProductionTest
{

	User u;
	DynamicQuery dq;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		dq = new DynamicQuery();
	}
	
	// TODO: figure out a better way to do this
	public User getUser()
	{
		return (User) Proxy.newProxyInstance(TableProxy.class.getClassLoader(), new Class[] {User.class}, new TableProxy(User.class));
	}
	
	@Test
	public void TestBasicWhereQuerySQLString()
	{
		User u = getUser();
		//System.out.println(u.id().getClass().getName());
		//System.out.println(dq.where(u.getColumn("name").eq("bob")).project().toSql());
		//System.out.println(u.where(u.getColumn("name").eq("bob").and(u.getColumn("id").eq(33))).project().toSql());
		System.out.println(u.where(u.name().eq("bob").and( u.id().eq(1) )).project().toSql());
	}

}
