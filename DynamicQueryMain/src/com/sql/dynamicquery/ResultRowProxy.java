/**
 * 
 */
package com.sql.dynamicquery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author DirectXMan12
 *
 */
public class ResultRowProxy implements InvocationHandler
{

	private Set<Class<? extends ITable>> _tblClasses;
	private HashMap<TableColumn, Object> _rowResults;
	private Class<? extends ITable> _mainClass;
	
	public ResultRowProxy(Set<TableColumn> cols, HashMap<String, Object> data, Class<? extends ITable> mainClass)
	{
		_tblClasses = new LinkedHashSet<Class<? extends ITable>>();
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
			if (c.getName().equals(colName) && c.getTable().getActualClass().equals(_mainClass)) return c;
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
	
	private <T extends ITable> T getProxiedInstanceOf(Class<T> c)
	{
		return c.cast(Proxy.newProxyInstance(TableProxy.class.getClassLoader(), new Class[] {c}, new TableProxy(c)));
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
		else if (m.isAnnotationPresent(Column.class) && !m.getName().startsWith("get"))
		{
			// this is a column method
			return new TableColumn((ITable)proxy, m);
			//return new DynamicQuery().project(new TableColumn[] {new TableColumn(((ITable) proxy), m)});
		}
		else if (m.isAnnotationPresent(HasMany.class) && !m.getName().startsWith("get"))
		{
			Class<?> resType = _mainClass.getMethod("get"+TableProxy.ucFirstLetter(m.getName())).getReturnType();
			Class<? extends ITable> colType;
			if (!resType.isArray()) colType = (Class<? extends ITable>) resType;
			else colType = (Class<? extends ITable>) Class.forName(resType.getName().replaceFirst("\\[.", "").replaceFirst(";", ""));
			TableColumn idCol = ((TableColumn)colType.getMethod(lcFirstLetter(Inflector.singularize(((ITable)proxy).toSql()))+"Id").invoke(getProxiedInstanceOf(colType)));

		
			EqualsPredicate w = idCol.eq((Number)_mainClass.getMethod("getId").invoke(proxy));
			return new DynamicQuery(colType).where(w);
		}
		else if (m.isAnnotationPresent(HasMany.class) && m.getName().startsWith("get"))
		{
			// TODO: implement -- need to have collected all entries for this unique master entry
			return null;
		}
		else if (m.getName().startsWith("get") && m.isAnnotationPresent(Column.class))
		{
			return _rowResults.get(getColumnFromString(lcFirstLetter(m.getName().substring(3))));
		}
		else if (m.isAnnotationPresent(BelongsTo.class) && !m.getName().startsWith("get"))
		{
			// TODO: implement
			return null;
		}
		else if (m.getName().equals("equals"))
		{
			if (!(args[0] instanceof ITable)) return false; // is it even a table?
			
			ITable it = (ITable) args[0];
			
			if (!it.toSql().equals(toPlural())) return false; // is the table name equal?
			
			for (Method meth : it.getActualClass().getMethods()) // are the column values equal?
			{
				if(meth.isAnnotationPresent(Column.class) && meth.getName().startsWith("get"))
				{
					if(!meth.invoke(it).equals(meth.invoke(this))) return false;
				}
			}
			
			return true;
			
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
		else if (m.getName().equals("toSql"))
		{
			return toPlural();
		}
		else if (m.getName().equals("getActualClass"))
		{
			return _mainClass;
		}
		else
		{
			throw new UnsupportedOperationException("method "+m.getName()+" is not implemented for query results");
		}
	}
	
	public static String lcFirstLetter(String s)
	{
		return Character.toLowerCase(s.charAt(0))+s.substring(1);
	}
	
	protected String toPlural()
	{
		String t = TableProxy.getActualLocalName(_mainClass);
		t = lcFirstLetter(t);
		return Inflector.pluralize(t);
	}

}
