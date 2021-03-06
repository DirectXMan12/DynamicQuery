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
import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sql.dynamicquery.DynamicQuery;
import com.sql.dynamicquery.DynamicQueryDatabaseConfigurator;
import com.sql.dynamicquery.ITable;
import com.sql.dynamicquery.OrderByFilter.DIRECTION;
import com.sql.dynamicquery.TableProxy;
import com.sun.tools.javac.code.Attribute.Array;
/**
 * @author DirectXMan12
 *
 */
public class BasicSqlProductionTest
{

	User u;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpDB() throws Exception
	{
		// test to see if needs creating
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:derby:testdb");
			Statement stmt = conn.createStatement();
			stmt.execute("select * from users");
			conn.close();
		}
		catch (SQLException ex)
		{
			Connection conn = DriverManager.getConnection("jdbc:derby:testdb;create=true");
			Statement stmt = conn.createStatement();
			
			stmt.execute("create table users (id int not null generated always as identity, name varchar(40), primary key (id))");
			stmt.execute("insert into users (users.name) values ('testuser1'),('testuser2')");
			
			conn.close();
		}
		
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:derby:testdb");
			Statement stmt = conn.createStatement();
			stmt.execute("select * from apps");
			conn.close();
		}
		catch(SQLException ex)
		{	Connection conn = DriverManager.getConnection("jdbc:derby:testdb");
			Statement stmt = conn.createStatement();
			
			stmt.execute("create table apps (id int not null generated always as identity, name varchar(40), userId int not null , primary key(id), foreign key (userId) references users(id))");
			stmt.execute("insert into apps (apps.name, apps.userId) values ('testapp1', 1),('testapp2', 1)");
			
			conn.close();
		}
		
		DynamicQueryDatabaseConfigurator.setDatabaseString("jdbc:derby:testdb");
	}
	
	// TODO: figure out a better way to do this
	public User getUser()
	{
		return (User) Proxy.newProxyInstance(TableProxy.class.getClassLoader(), new Class[] {User.class}, new TableProxy(User.class));
	}
	
	public App getApp()
	{
		return (App) Proxy.newProxyInstance(TableProxy.class.getClassLoader(), new Class[] {App.class}, new TableProxy(App.class));
	}
	
	@Test
	public void TestBasicWhereQuerySQLString()
	{
		User u = getUser();
		//System.out.println(u.id().getClass().getName());
		//System.out.println(dq.where(u.getColumn("name").eq("bob")).project().toSql());
		//System.out.println(u.where(u.getColumn("name").eq("bob").and(u.getColumn("id").eq(33))).project().toSql());
		assertEquals( "select users.name, users.id from users where users.name = \"bob\" and users.id = 1;",u.where(u.name().eq("bob").and( u.id().eq(1) )).project().toSql()+";");
	}
	
	@Test
	public void TestActualFlatQuery()
	{
		User u = getUser();
		DynamicQuery q = u.project();
		System.out.println("TestActualFlatQuery: Query = " + q.toSql());
		Object res[] = q.toArray();
		
		System.out.println(((User)res[0]).getName());
		
		for (ITable it : u.project())
		{
			System.out.println(((User)it).getName());
			Object apps[] = ((User)it).apps().project().toArray();
			if (apps.length > 0) System.out.println(((App)apps[0]).getName());
		}
	}
	
	@Test
	public void TestActualSingleJoinQuery()
	{
		User u = getUser();
		App a = getApp();
		
		DynamicQuery q = u.join(a).on(a.id().eq(u.id())).project();
		System.out.println(q.toSql());
		
		Object res[] = q.toArray();
		System.out.println(res[0]+": "+((User)res[0]).getApps()[0].getName());
		assertEquals(((User)res[0]).getName(), "testuser1");
		assertEquals(((User)res[0]).getApps()[0].getName(), "testapp1");
	}
	
	@Test
	public void TestOrderBySQLString()
	{
		User u = getUser();
		
		DynamicQuery q = u.order(u.name(), DIRECTION.asc).project();
		
		assertEquals("select users.name, users.id from users order by users.name ASC", q.toSql());
	}
	
	@Test
	public void TestGroupByHavingSQLString()
	{
		App a = getApp();
		
		DynamicQuery q = a.group(a.userId()).having(a.userId().in(Arrays.asList(2,3))).project();
		
		assertEquals("select apps.userId from apps group by apps.userId having apps.userId in (2,3)", q.toSql());
	}
	
	@Test
	public void TestGroupByHavingQuery()
	{
		App a = getApp();
		
		DynamicQuery q = a.group(a.userId()).having(a.userId().in(Arrays.asList(1,2,3))).project(a.userId(), a.userId().count());
		
		for(ITable it : q)
		{
			System.out.println(it.toString());
		}
	}
	
	@Test
	public void TestTableAliasingSQLString()
	{
		User u = getUser();
		User ua = (User) u.as("awesomeUsers");
		
		DynamicQuery q = u.join(ua).on(u.name().eq(ua.name())).project();
		
		assertEquals("select users.name, users.id, awesomeUsers.name, awesomeUsers.id from users join users as awesomeUsers on users.name = awesomeUsers.name ", q.toSql());
	}
	
	@AfterClass
	public static void tearDownDB()
	{
		Connection conn;
		try
		{
			conn = DriverManager.getConnection("jdbc:derby:testdb");
			Statement stmt = conn.createStatement();
			stmt.execute("drop table apps");
			stmt.execute("drop table users");
			conn.close();
		}
		catch (SQLException e)
		{
			System.err.println("Couldn't tear down the database (stack trace below):");
			e.printStackTrace();
		}
		
		
	}

}
