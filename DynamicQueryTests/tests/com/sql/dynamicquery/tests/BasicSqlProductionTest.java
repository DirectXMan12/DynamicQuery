/**
 * 
 */
package com.sql.dynamicquery.tests;



import static org.junit.Assert.assertEquals;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;

import com.sql.dynamicquery.DynamicQuery;
import com.sql.dynamicquery.ITable;
import com.sql.dynamicquery.TableProxy;
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
		
		// test to see if needs creating
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:derby:testdb");
			Statement stmt = conn.createStatement();
			conn.close();
		}
		catch (SQLException ex)
		{
			Connection conn = DriverManager.getConnection("jdbc:derby:testdb;create=true");
			Statement stmt = conn.createStatement();
			
			stmt.execute("create table users (id int not null generated always as identity, name varchar(40))");
			stmt.execute("insert into users (users.name) values ('testuser1'),('testuser2')");
			
			conn.close();
		}
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
		assertEquals( "select users.name, users.id from users where users.name = \"bob\" and users.id = 1;",u.where(u.name().eq("bob").and( u.id().eq(1) )).project().toSql()+";");
		
		// test actual sql
		
		Object res[] = u.project().toArray();
		
		System.out.println(((User)res[0]).getName());
		
		for (ITable it : u.project())
		{
			System.out.println(((User)it).getName());
		}
	}

}
