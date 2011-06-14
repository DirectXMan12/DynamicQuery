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
		if (m.isAnnotationPresent(Column.class) && Arrays.asList(_tblClass.getMethods()).contains(m) && !m.getName().startsWith("get"))
		{
			// this is a column method
			return new TableColumn((ITable)proxy, _tblClass.getMethod(m.getName()));
			//return new DynamicQuery().project(new TableColumn[] {new TableColumn(((ITable) proxy), m)});
		}
		else if (m.isAnnotationPresent(HasMany.class) && Arrays.asList(_tblClass.getMethods()).contains(m) && !m.getName().startsWith("get"))
		{
			// TODO: implement
			return null;
		}
		else if (m.isAnnotationPresent(HasMany.class) && m.getName().startsWith("get") && Arrays.asList(_tblClass.getMethods()).contains(m))
		{
			// TODO: implement
			return null;
		}
		else if (m.isAnnotationPresent(BelongsTo.class) && Arrays.asList(_tblClass.getMethods()).contains(m) && m.getName().endsWith("Id") && !m.getName().startsWith("get"))
		{
			return new TableColumn((ITable)proxy, _tblClass.getMethod(m.getName()));
		}
		else if (m.isAnnotationPresent(BelongsTo.class) && Arrays.asList(_tblClass.getMethods()).contains(m) && !m.getName().startsWith("get"))
		{
			throw new UnsupportedOperationException("You haven't specified a single object instance yet!");
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
			return new DynamicQuery(_tblClass).where((ISelectionPredicate) args[0]);
		}
		else if (m.getName().equals("project"))
		{
			return new DynamicQuery(_tblClass).project();
		}
		else if (m.getName().equals("join"))
		{
			return new DynamicQuery(_tblClass).join((ITable) args[0]);
		}
		else if (m.getName().equals("getActualClass"))
		{
			return _tblClass;
		}
		else if (m.getName().equals("toString"))
		{
			return _tblClass.getName();
		}
		else if (m.getName().equals("toSql"))
		{
			return toPlural();
		}
		else if (m.getName().equals("equals"))
		{
			return isTableEqual(args[0]);
		}
		else if (m.getName().equals("hashCode"))
		{
			return ITableHashCode();
		}
		else
		{
			return null;
		}
	}
	
	public static String ucFirstLetter(String word)
	{
		return Character.toUpperCase(word.charAt(0))+word.substring(1);
	}
	
	protected String toPlural()
	{
		String t = getActualLocalName(_tblClass);
		t = Character.toLowerCase(t.charAt(0))+t.substring(1);
		return Inflector.pluralize(t);
	}
	
	public boolean isTableEqual(Object o)
	{
		if (_tblClass == o) return true;
		if (o instanceof ITable) return _tblClass.equals(((ITable)o).getActualClass());
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

	public int ITableHashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_tblClass == null) ? 0 : _tblClass.hashCode());
		return result;
	}
}
