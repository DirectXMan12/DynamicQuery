/**
 * 
 */
package com.sql.dynamicquery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * @author DirectXMan12
 *
 */
public class ResultRowProxy implements InvocationHandler
{

	private LinkedList<Class<? extends ITable>> _tblClasses;
	private HashMap<TableColumn, Object> _rowResults;
	private Class<? extends ITable> _mainClass;
	
	public ResultRowProxy(LinkedList<TableColumn> cols, HashMap<String, Object> data, Class<? extends ITable> mainClass)
	{
		_tblClasses = new LinkedList<Class<? extends ITable>>();
		_rowResults = new HashMap<TableColumn, Object>();
		for (TableColumn t : cols)
		{
			_rowResults.put(t, null);
			_tblClasses.add(t.getTable().getActualClass());
		}
		
		for (String k : data.keySet())
		{
			setColumn(k, data.get(k));
		}
		
		_mainClass = mainClass;
	}
	
	private boolean inAnyTableMethod(Method meth)
	{
		for (Class<? extends ITable> cl : _tblClasses)
		{
			if (Arrays.asList(cl.getMethods()).contains(meth)) return true;
		}
		
		return false;
	}
	
	private TableColumn getColumnFromString(String colName)
	{
		for (TableColumn c : _rowResults.keySet())
		{
			if (c.getName().equals(colName)) return c;
		}
		
		return null;
	}
	
	private TableColumn getColumnFromFullName(String colName)
	{
		String colParts[] = colName.split("\\.");
		for (TableColumn c : _rowResults.keySet())
		{
			if (c.getTable().toSql().equals(colParts[0]) && c.getName().equals(colParts[1])) return c;
		}
		
		return null;
	}
	
	protected void setColumn(String colName, Object val)
	{
		_rowResults.put(getColumnFromFullName(colName), val);
	}
	
	public Object getColumnValue(String column)
	{
		String colParts[] = column.split("\\.");
		for (TableColumn c : _rowResults.keySet())
		{
			if (c.getTable().toSql().equals(colParts[0]) && c.getName().equals(colParts[1])) return _rowResults.get(c);
		}
		
		return null;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
	{
		if (m.getName().equals("getColumns"))
		{
			return new ArrayList<TableColumn>(_rowResults.keySet());
		}
		else if (inAnyTableMethod(m) && m.isAnnotationPresent(Column.class) && !m.getName().startsWith("get"))
		{
			// this is a column method
			return new TableColumn((ITable)proxy, m);
			//return new DynamicQuery().project(new TableColumn[] {new TableColumn(((ITable) proxy), m)});
		}
		else if (m.getName().startsWith("get") && inAnyTableMethod(m) && m.isAnnotationPresent(Column.class))
		{
			return _rowResults.get(getColumnFromString(Character.toLowerCase(m.getName().substring(3).charAt(0))+m.getName().substring(4)));
		}
		else if (m.getName().equals("toString"))
		{
			StringBuilder sb = new StringBuilder();
			sb.append("ResultRow(ITable Proxy): [");
			for(TableColumn t : _rowResults.keySet())
			{
				sb.append(t.getTable().toSql());
				sb.append(".");
				sb.append(t.getName());
				sb.append(" = ");
				sb.append(_rowResults.get(t).toString());
				sb.append(", ");
			}
			sb.replace(sb.length()-2, sb.length(), "");
			sb.append("]");
			
			return sb.toString();
		}
		else if (m.getName().equals("toString"))
		{
			return toPlural();
		}
		else
		{
			throw new UnsupportedOperationException("method "+m.getName()+" is not implemented for query results");
		}
		
		
	}
	
	
	protected String toPlural()
	{
		String t = TableProxy.getActualLocalName(_mainClass);
		return Inflector.pluralize(t);
	}

}
