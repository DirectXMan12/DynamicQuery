/**
 * 
 */
package com.sql.dynamicquery.tests;

import com.sql.dynamicquery.AfterResultsOnly;
import com.sql.dynamicquery.ITable;

/**
 * @author DirectXMan12
 *
 */
public class UserMethodDefinitions
{
	public static String getSomeCheese(ITable it)
	{
		return "cheese and crackers!";
	}
	
	@AfterResultsOnly
	public static String nameAndId(ITable it)
	{
		User u = (User)it;
		
		return u.getName()+"-"+u.getId();
	}
}
