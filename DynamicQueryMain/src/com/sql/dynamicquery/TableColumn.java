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
public class TableColumn
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
	
	public EqualsPredicate eq(int val)
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
}
