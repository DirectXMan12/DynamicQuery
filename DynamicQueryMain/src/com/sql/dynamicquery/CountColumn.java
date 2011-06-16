/**
 * 
 */
package com.sql.dynamicquery;

import java.lang.reflect.Method;

/**
 * @author DirectXMan12
 *
 */
public class CountColumn extends TableColumn implements IAggregateColumn
{
	private TableColumn _countCol;
	
	public CountColumn()
	{
		super(null, "count(*)");
		_countCol = null;
		setAlias("CountAll");
	}

	public CountColumn(TableColumn c)
	{
		super(null, "count("+c.toSql()+")");
		_countCol = c;
		setAlias("Count"+DynamicQueryAbstractProxy.ucFirstLetter(c.getTable().toSql())+DynamicQueryAbstractProxy.ucFirstLetter(c.getName()));
	}
	
	@Override
	public String toDefinitionSql()
	{
		return "count("+_countCol.toSql()+") as "+getAlias();
	}
}
