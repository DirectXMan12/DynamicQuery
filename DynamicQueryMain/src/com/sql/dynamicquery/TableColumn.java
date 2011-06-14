/**
 * 
 */
package com.sql.dynamicquery;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * @author DirectXMan12
 *
 */
public class TableColumn implements SQLConvertable
{
	private ITable _parentTable;
	private String _columnName;
	
	public TableColumn(ITable pt, String name)
	{
		_parentTable = pt;
		_columnName = name;
	}
	
	public TableColumn(ITable t, Method m)
	{
		_parentTable = t;
		_columnName = m.getName();
	}
	
	public ITable getTable() { return _parentTable; }
	public String getName() { return _columnName; }
	
	public EqualsPredicate eq(Number val)
	{
		return new EqualsPredicate(this, val);
	}
	
	public EqualsPredicate eq(SQLConvertable val)
	{
		return new EqualsPredicate(this, val);
	}
	
	public EqualsPredicate eq(Object val)
	{
		return new EqualsPredicate(this, val.toString());
	}
	
	public InPredicate in(Collection<?> list)
	{
		return new InPredicate(this, list);
	}

	public String toSql()
	{
		return _parentTable.toSql()+"."+_columnName; // TODO: fix this
	}
}
