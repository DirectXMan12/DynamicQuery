/**
 * 
 */
package com.sql.dynamicquery.tests;

import static org.junit.Assert.*;

import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sql.dynamicquery.DynamicQueryDatabaseConfigurator;
import com.sql.dynamicquery.TableProxy;

/**
 * @author DirectXMan12
 *
 */
public class AdvancedDynamicQueryFunctionalityTests
{

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
	
	public User getUser()
	{
		return (User) Proxy.newProxyInstance(TableProxy.class.getClassLoader(), new Class[] {User.class}, new TableProxy(User.class));
	}
	
	@Test
	public void testNonSQLRelatedMethodsBeforeResults()
	{
		User u = getUser();
		assertEquals("cheese and crackers!", u.getSomeCheese());
	}
	
	@Test(expected=UnsupportedOperationException.class)
	public void testAfterResultsMethodBeforeResultsThrowsException()
	{
		User u = getUser();
		u.nameAndId();
	}
	
	@Test
	public void testAfterResultsMethod()
	{
		User u = getUser();
		
		Object res[] = u.project().toArray();
		
		assertEquals("testuser1-1",((User)res[0]).nameAndId());
		
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
