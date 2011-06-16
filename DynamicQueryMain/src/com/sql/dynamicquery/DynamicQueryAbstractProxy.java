/**
 * 
 */
package com.sql.dynamicquery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.sql.dynamicquery.OrderByFilter.DIRECTION;

/**
 * @author DirectXMan12
 *
 */
public abstract class DynamicQueryAbstractProxy implements InvocationHandler
{
	public static String lcFirstLetter(String s)
	{
		return Character.toLowerCase(s.charAt(0))+s.substring(1);
	}
	
	public static String ucFirstLetter(String word)
	{
		return Character.toUpperCase(word.charAt(0))+word.substring(1);
	}
	
	protected static String toPlural(ITable tbl)
	{
		String t = TableProxy.getActualLocalName(tbl.getActualClass());
		t = lcFirstLetter(t);
		return Inflector.pluralize(t);
	}
	
	protected static String toPlural(Class<?> cl)
	{
		String t = TableProxy.getActualLocalName(cl);
		t = lcFirstLetter(t);
		return Inflector.pluralize(t);
	}
	
	protected abstract Class<? extends ITable> getPrimaryTableClass();
	
	/**
	 * Don't override in implementing classes!
	 * (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
	{
		return handleInvoke(proxy, m, m.getName(), args, getPrimaryTableClass());
	}
	
	
	/**
	 * handle method switching here.  In final else, call super.handleInvoke()
	 * @param primaryClass primary class for the super version to use
	 * @param convience parameter for m.getName()
	 * @return results of invoked method
	 * (non-Javadoc)
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[]) for most of the parameter documentation
	 */
	public Object handleInvoke(Object proxy, Method m, String methodName, Object[] args, Class<? extends ITable> primaryClass) throws Exception
	{
		if (methodName.equals("toSql"))
		{
			return toPlural(primaryClass);
		}
		else if (m.isAnnotationPresent(Column.class) && !methodName.startsWith("get"))
		{
			return genTableColumn(proxy, m, args, primaryClass);
		}
		else if (methodName.equals("where"))
		{
			return new DynamicQuery(primaryClass).where((ISelectionPredicate) args[0]);
		}
		else if (methodName.equals("project"))
		{
			return new DynamicQuery(primaryClass).project();
		}
		else if (methodName.equals("join"))
		{
			return new DynamicQuery(primaryClass).join((ITable) args[0]);
		}
		else if (methodName.equals("order"))
		{
			return new DynamicQuery(primaryClass).order((TableColumn) args[0], (DIRECTION) args[1]);
		}
		else if (methodName.equals("group"))
		{
			return new DynamicQuery(primaryClass).group((TableColumn) args[0]);
		}
		else
		{
			throw new UnsupportedOperationException("method "+m.getName()+" is not implemented for "+this.getClass().getName());
		}
	}
	
	protected <T extends ITable> T getProxiedInstanceOf(Class<T> proxiedClass, InvocationHandler handlerInstance)
	{
		return proxiedClass.cast(Proxy.newProxyInstance(handlerInstance.getClass().getClassLoader(), new Class[] {proxiedClass}, handlerInstance));
	}
	
	protected TableColumn genTableColumn(Object proxy, Method m, Object[] args, Class<? extends ITable> primaryClass) throws Exception
	{
		return new TableColumn((ITable)proxy, primaryClass.getMethod(m.getName()));
	}
	
	public boolean isSameTable(Object o)
	{
		Class<? extends ITable> p = getPrimaryTableClass();
		if (p == o) return true;
		if (o instanceof ITable) return p.equals(((ITable)o).getActualClass());
		return p.equals(o.getClass());
	}
	
	public int BasicITableHashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getPrimaryTableClass() == null) ? 0 : getPrimaryTableClass().hashCode());
		return result;
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
