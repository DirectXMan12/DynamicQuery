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
public class TableProxy implements InvocationHandler {

	private Class<? extends ITable> _tblClass;
	
	public TableProxy(Class<? extends ITable> tc)
	{
		_tblClass = tc;
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
	{
		if (Arrays.asList(_tblClass.getMethods()).contains(m) && m.isAnnotationPresent(Column.class))
		{
			// this is a column method
			return new TableColumn((ITable)proxy, _tblClass.getMethod(m.getName()));
			//return new DynamicQuery().project(new TableColumn[] {new TableColumn(((ITable) proxy), m)});
		}
		else if (m.getName().equals("getColumn"))
		{

			try {
				return new TableColumn((ITable)proxy, _tblClass.getMethod((String)args[0]));
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
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
		else if (m.getName().equals("where"))
		{
			return new DynamicQuery().where((ISelectionPredicate) args[0]);
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
			return isTableEqual(args[0]);
		}
		else
		{
			return null;
		}
	}
	
	public boolean isTableEqual(Object o)
	{
		if (o instanceof Proxy) return _tblClass.equals(((ITable)o).getActualClass());
		return _tblClass.equals(o.getClass());
	}
	
	public static String getActualLocalName(ITable t)
	{
		return t.getActualClass().getName().replaceAll(t.getActualClass().getPackage().getName()+".", "");
	}
	
	public static String getActualLocalName(Class<?> t)
	{
		return t.getName().replaceAll(t.getPackage().getName()+".", "");
	}

}
