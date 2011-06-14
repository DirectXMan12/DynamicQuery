/**
 * 
 */
package com.sql.dynamicquery;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import com.sun.tools.javac.util.List;

/**
 * @author DirectXMan12
 *
 */
public class ResultCluster<T extends ITable> implements InvocationHandler
{
	private Set<ITable> _referencedTables;
	private T _mainEntry;
	private HashMap<ITable, Object> _otherValues;
	
	/**
	 * 
	 */
	public ResultCluster(Set<ITable> referencedTables, T currEntry)
	{
		_referencedTables = referencedTables;
		_otherValues = new HashMap<ITable, Object>();
		_mainEntry = currEntry;
	}
	
	private <R extends ITable> R getProxiedInstanceOf(Class<R> c)
	{
		return c.cast(Proxy.newProxyInstance(TableProxy.class.getClassLoader(), new Class[] {c}, new TableProxy(c)));
	}
	
	public Class<T> getMainEntryClass()
	{
		return _mainEntry.getActualClass();
	}
	
	public T getMainEntry()
	{
		return _mainEntry;
	}
	
	private void initToListIfNotPresent(ITable entityType)
	{
		 if (!_otherValues.containsKey(entityType)) _otherValues.put(entityType, new LinkedBlockingDeque<ITable>());
	}
	private <R extends ITable> void initToClusterListIfNotPresent(R entityType)
	{
		 if (!_otherValues.containsKey(entityType)) _otherValues.put(entityType, new LinkedBlockingDeque<ResultCluster<R>>());
	}
	
	public void putInList(ITable entityType, ITable entityInstance)
	{
		initToListIfNotPresent(entityType);
		
		((LinkedBlockingDeque<ITable>)_otherValues.get(entityType)).add(entityInstance); // TODO: ignore multiples (can't just compare b/c ResultRowProxies have all columns, not just ones for type)
	}
	
	public <R extends ITable> void putInList(R entityType, ResultCluster<R> val)
	{
		initToClusterListIfNotPresent(entityType);
		
		((LinkedBlockingDeque<ResultCluster<R>>)_otherValues.get(entityType)).add(val);
	}
	
	public boolean putAsSingleton(ITable entityType, ITable entityInstance)
	{
		if(_otherValues.containsKey(entityType)) return false;
		
		_otherValues.put(entityType, entityInstance);
		return true;
	}

	@Override
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
	{
		if (m.getName().equals("getColumns"))
		{
			return _mainEntry.getColumns();
		}
		else if (m.isAnnotationPresent(Column.class) && !m.getName().startsWith("get"))
		{
			// this is a column method
			return new TableColumn((ITable)proxy, m);
			//return new DynamicQuery().project(new TableColumn[] {new TableColumn(((ITable) proxy), m)});
		}
		else if (m.isAnnotationPresent(HasMany.class) && !m.getName().startsWith("get"))
		{
			Class<?> resType = _mainEntry.getActualClass().getMethod("get"+TableProxy.ucFirstLetter(m.getName())).getReturnType();
			Class<? extends ITable> colType;
			if (!resType.isArray()) colType = (Class<? extends ITable>) resType;
			else colType = (Class<? extends ITable>) Class.forName(resType.getName().replaceFirst("\\[.", "").replaceFirst(";", ""));
			TableColumn idCol = ((TableColumn)colType.getMethod(lcFirstLetter(Inflector.singularize(((ITable)proxy).toSql()))+"Id").invoke(getProxiedInstanceOf(colType)));

		
			EqualsPredicate w = idCol.eq((Number)_mainEntry.getActualClass().getMethod("getId").invoke(_mainEntry));
			return new DynamicQuery(colType).where(w);
		}
		else if (m.isAnnotationPresent(HasMany.class) && m.getName().startsWith("get"))
		{
			Class<? extends ITable> cl = (Class<? extends ITable>) Class.forName(m.getReturnType().getName().replaceFirst("\\[.", "").replaceFirst(";", ""));
			ITable inst = DynamicQuery.proxyInstanceOf(cl, new TableProxy(cl));
			
			Collection res = ((Collection)_otherValues.get(inst));
			Object realRes[] = (Object[]) Array.newInstance(cl, res.size());
			int i = 0;
			for (Iterator iter = res.iterator(); iter.hasNext(); i++) realRes[i] = cl.cast(iter.next());
			return realRes;
		}
		else if (m.getName().startsWith("get") && m.isAnnotationPresent(Column.class))
		{
			return m.invoke(_mainEntry);
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
			sb.append("ResultCluster(ITable Proxy): [");
			// TODO: fill in this info
			sb.append("]");
			
			return sb.toString();
		}
		else if (m.getName().equals("toSql"))
		{
			return toPlural();
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
		String t = TableProxy.getActualLocalName(_mainEntry.getActualClass());
		t = lcFirstLetter(t);
		return Inflector.pluralize(t);
	}

}
