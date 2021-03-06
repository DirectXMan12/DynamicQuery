/**
 * 
 */
package com.sql.dynamicquery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author DirectXMan12
 *
 */
public class TableProxy extends DynamicQueryAbstractProxy implements InvocationHandler {

	private Class<? extends ITable> _tblClass;
	
	public TableProxy(Class<? extends ITable> tc)
	{
		_tblClass = tc;
	}
	
	public TableProxy()
	{
		
	}
	
	@Override
	Object copyOf(DynamicQueryAbstractProxy t)
	{
		TableProxy res = (TableProxy) t;
		res._tblClass = this._tblClass;
		return res;
	}
	
	
	@Override
	public Object handleInvoke(Object proxy, Method m, String methodName, Object[] args, Class<? extends ITable> primaryClass) throws Exception
	{
		if (m.isAnnotationPresent(BelongsTo.class) && m.getName().endsWith("Id") && !m.getName().startsWith("get"))
		{
			return new TableColumn((ITable)proxy, _tblClass.getMethod(m.getName()));
		}
		else if (m.getName().equals("getColumn"))
		{
			return this.genTableColumn(proxy, m, args, _tblClass);
		}
		else if (m.getName().equals("getColumns"))
		{
			ArrayList<TableColumn> tbls = new ArrayList<TableColumn>();
			
			for (Method m1 : this.getClass().getMethods())
			{
				if (m1.isAnnotationPresent(Column.class)) tbls.add(new TableColumn((ITable)proxy, m1));
			}
			
			return (TableColumn[]) tbls.toArray();
		}
		else if (m.getName().equals("getActualClass"))
		{
			return _tblClass;
		}
		else if (m.getName().equals("toString"))
		{
			return _tblClass.getName();
		}
		else if (m.getName().equals("equals"))
		{
			return isSameTable(args[0], (ITable) proxy);
		}
		else if (m.getName().equals("hashCode"))
		{
			return BasicITableHashCode();
		}
		else
		{
			return super.handleInvoke(proxy, m, methodName, args, primaryClass);
		}
	}

	@Override
	protected Class<? extends ITable> getPrimaryTableClass()
	{
		return _tblClass;
	}
}
