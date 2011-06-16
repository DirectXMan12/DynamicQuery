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
public class ResultRowProxy extends DynamicQueryAbstractProxy implements InvocationHandler
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
			
			if (t.getTable() != null) _tblClasses.add(t.getTable().getActualClass());
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
			if (c.isAliased() && c.getAlias().equals(colName)) return c;
			if (c.getTable() != null && c.getTable().toSql().equals(colParts[0]) && c.getName().equals(colParts[1])) return c;
		}
		
		return null;
	}
	
	protected void setColumn(String colName, Object val)
	{
		if (colName == null || getColumnFromFullName(colName) == null)
		{
			throw new RuntimeException("Attempted to add a null column or null column name to result set.  Column gotten from column name '"+colName+"'");
		}
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
	
	public Object getColumnValue(TableColumn col)
	{
		return _rowResults.get(col);
	}
	
	@Override
	public Object handleInvoke(Object proxy, Method m, String methodName, Object[] args, Class<? extends ITable> primaryClass) throws Exception
	{
		if (methodName.equals("getColumns"))
		{
			return new ArrayList<TableColumn>(_rowResults.keySet()).toArray(new TableColumn[] {});
		}
		else if (m.isAnnotationPresent(HasMany.class) && !methodName.startsWith("get"))
		{
			Class<?> resType = _mainClass.getMethod("get"+TableProxy.ucFirstLetter(methodName)).getReturnType();
			Class<? extends ITable> colType;
			if (!resType.isArray()) colType = (Class<? extends ITable>) resType;
			else colType = (Class<? extends ITable>) Class.forName(resType.getName().replaceFirst("\\[.", "").replaceFirst(";", ""));
			TableColumn idCol = ((TableColumn)colType.getMethod(lcFirstLetter(Inflector.singularize(((ITable)proxy).toSql()))+"Id").invoke(getProxiedInstanceOf(colType, new TableProxy(colType))));

		
			EqualsPredicate w = idCol.eq((Number)_mainClass.getMethod("getId").invoke(proxy));
			return new DynamicQuery(colType).where(w);
		}
		else if (methodName.startsWith("get") && (m.isAnnotationPresent(Column.class) || (methodName.endsWith("Id") && m.isAnnotationPresent(BelongsTo.class)) ))
		{
			return _rowResults.get(getColumnFromString(lcFirstLetter(methodName.substring(3))));
		}
		else if(methodName.equals("getCount"))
		{
			return (Integer) getColumnValue((TableColumn)args[0]);
		}
		else if (methodName.equals("equals"))
		{
			if (!(args[0] instanceof ITable)) return false; // is it even a table?
			
			ITable it = (ITable) args[0];
			
			if (!it.toSql().equals(toPlural(_mainClass))) return false; // is the table name equal?
			
			for (Method meth : it.getActualClass().getMethods()) // are the column values equal?
			{
				if(meth.isAnnotationPresent(Column.class) && meth.getName().startsWith("get"))
				{
					if(!meth.invoke(it).equals(meth.invoke(this))) return false;
				}
			}
			
			return true;
			
		}
		else if (methodName.equals("toString"))
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
		else if (methodName.equals("getActualClass"))
		{
			return _mainClass;
		}
		else
		{
			return super.handleInvoke(proxy, m, methodName, args, primaryClass);
		}
	}

	@Override
	protected Class<? extends ITable> getPrimaryTableClass()
	{
		return _mainClass;
	}

}
