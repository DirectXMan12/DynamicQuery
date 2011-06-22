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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

import sun.security.action.GetLongAction;

import com.sun.tools.javac.util.List;

/**
 * @author DirectXMan12
 *
 */
public class ResultCluster<T extends ITable> extends DynamicQueryAbstractProxy implements InvocationHandler
{
	private Set<ITable> _referencedTables;
	private T _mainEntry;
	private HashMap<ITable, Object> _otherValues;
	
	@Override
	Object copyOf(DynamicQueryAbstractProxy t)
	{
		ResultCluster<T> res = (ResultCluster<T>) t;
		res._mainEntry = _mainEntry;
		res._referencedTables = new LinkedHashSet<ITable>(_referencedTables);
		res._otherValues = (HashMap<ITable, Object>) _otherValues.clone();
		
		return res;
	}
	
	public ResultCluster()
	{
		
	}
	
	/**
	 * 
	 */
	public ResultCluster(Set<ITable> referencedTables, T currEntry)
	{
		_referencedTables = referencedTables;
		_otherValues = new HashMap<ITable, Object>();
		_mainEntry = currEntry;
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
	public Object handleInvoke(Object proxy, Method m, String methodName, Object[] args, Class<? extends ITable> primaryClass) throws Exception
	{
		if (m.getName().equals("getColumns"))
		{
			return _mainEntry.getColumns();
		}
		else if (m.isAnnotationPresent(HasMany.class) && !m.getName().startsWith("get"))
		{
			Class<?> resType = _mainEntry.getActualClass().getMethod("get"+TableProxy.ucFirstLetter(m.getName())).getReturnType();
			Class<? extends ITable> colType;
			if (!resType.isArray()) colType = (Class<? extends ITable>) resType;
			else colType = (Class<? extends ITable>) Class.forName(resType.getName().replaceFirst("\\[.", "").replaceFirst(";", ""));
			TableColumn idCol = ((TableColumn)colType.getMethod(lcFirstLetter(Inflector.singularize(((ITable)proxy).toSql()))+"Id").invoke(getProxiedInstanceOf(colType, new TableProxy(colType))));

		
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
			// TODO: implement non-get BelongsTo in ResultCluster
			return null;
		}
		else if (m.isAnnotationPresent(BelongsTo.class) && m.getName().startsWith("get"))
		{
			// TODO: implement get BelongsTo in ResultCluster
			return null;
		}
		else if (m.getName().equals("equals"))
		{
			if (!(args[0] instanceof ITable)) return false; // is it even a table?
			
			ITable it = (ITable) args[0];
			
			if (!it.toSql().equals(toPlural(_mainEntry))) return false; // is the table name equal?
			
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
			sb.append("ResultCluster(ITable Proxy): [(main entry: ");
			sb.append(_mainEntry.toSql());
			sb.append(") ");
			for (TableColumn c : _mainEntry.getColumns())
			{
				if (c.getTable() != null && !c.getTable().equals(_mainEntry)) continue;
				sb.append(c.toSql());
				sb.append(" = ");
				if (c instanceof IAggregateColumn)
				{
					String methName = "get"+getActualLocalName(c.getClass()).replace("Column", "");
					sb.append(ITable.class.getMethod(methName, TableColumn.class).invoke(_mainEntry, c));
				}
				else
				{
					String methName = "get"+ucFirstLetter(c.getName());
					sb.append(_mainEntry.getActualClass().getMethod(methName).invoke(_mainEntry));
				}
				sb.append(", ");
			}
			sb.replace(sb.length()-2, sb.length(), "");
			sb.append("], [(nested types) ");
			for (ITable tbl : _otherValues.keySet())
			{
				sb.append(tbl.toSql());
				sb.append(" = ");
				sb.append(_otherValues.get(tbl).getClass());
				sb.append(", ");
			}
			sb.replace(sb.length()-2, sb.length(), "");
			sb.append("]");
			
			return sb.toString();
		}
		else
		{
			return super.handleInvoke(proxy, m, methodName, args, primaryClass);
		}
		
	}

	@Override
	protected Class<? extends ITable> getPrimaryTableClass()
	{
		return _mainEntry.getActualClass();
	}
}
