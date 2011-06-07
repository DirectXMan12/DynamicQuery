/**
 * 
 */
package com.sql.dynamicquery;

import java.lang.reflect.Method;

/**
 * @author DirectXMan12
 *
 */
public class CountColumn extends TableColumn {

	public CountColumn()
	{
		super(null, "count(*)");
	}

	public CountColumn(TableColumn c)
	{
		super(null, "count("+c.toSql()+")");
	}
}
