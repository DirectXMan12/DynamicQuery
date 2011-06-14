/**
 * 
 */
package com.sql.dynamicquery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author DirectXMan12
 *
 */
public class DynamicQuery implements SQLConvertable, Collection<ITable>
{
	
	private LinkedHashSet<ITable> _referencedTables; // TODO: make some of these sets (referencedTables, etc)?
	private LinkedHashSet<TableColumn> _referencedCols;
	private LinkedList<ISelectionPredicate> _whereFilters;
	private LinkedList<IFilter> _filters;
	private LinkedHashSet<ITable> _refTablesIgnoreFrom;
	
	private Class<? extends ITable> _mainClass;
	
	public DynamicQuery()
	{
		_referencedTables = new LinkedHashSet<ITable>();
		_referencedCols = new LinkedHashSet<TableColumn>();
		_filters = new LinkedList<IFilter>();
		_whereFilters = new LinkedList<ISelectionPredicate>();
		_refTablesIgnoreFrom = new LinkedHashSet<ITable>();
		
		_mainClass = null;
	}
	
	public DynamicQuery(DynamicQuery q)
	{
		_referencedTables = (LinkedHashSet<ITable>) q.getReferencedTables().clone();
		_referencedCols = (LinkedHashSet<TableColumn>) q.getReferencedColumns().clone();
		_filters = (LinkedList<IFilter>) q.getFilters().clone();
		_whereFilters = (LinkedList<ISelectionPredicate>) q.getWhereFilters().clone();
		_refTablesIgnoreFrom = (LinkedHashSet<ITable>) q.getIgnoredReferencedTables().clone();
		
		_mainClass = q._mainClass;
	}
	
	public DynamicQuery(Class<? extends ITable> mainClass)
	{
		_referencedTables = new LinkedHashSet<ITable>();
		_referencedCols = new LinkedHashSet<TableColumn>();
		_filters = new LinkedList<IFilter>();
		_whereFilters = new LinkedList<ISelectionPredicate>();
		_refTablesIgnoreFrom = new LinkedHashSet<ITable>();
		
		_mainClass = mainClass;
		ITable tbl = proxyInstanceOf(_mainClass, new TableProxy(_mainClass));
		_referencedTables.add(tbl);
	}
	
	public DynamicQuery project()
	{
		DynamicQuery q = new DynamicQuery(this);
		for (ITable t : _referencedTables)
		{
			for(Method m : t.getActualClass().getMethods())
			{
				if(m.isAnnotationPresent(Column.class) && !m.getName().startsWith("get"))
				{
					q.addColumn(new TableColumn(t,m));
				}
			}
		}
		
		return q;
	}
	
	protected DynamicQuery project(ITable tbl)
	{
		_referencedTables.add(tbl);
		return project();
	}
	
	public DynamicQuery project(TableColumn col[])
	{
		DynamicQuery q = new DynamicQuery(this);
		
		for (TableColumn t : col)
		{
			q.addColumn(t);
		}
		
		return q;
	}
	
	public DynamicQuery join(ITable it)
	{
		DynamicQuery q = new DynamicQuery(this);
		
		q.addReferencedTables(Arrays.asList(it));
		q.addIgnoredReferencedTable(Arrays.asList(it));
		q.addFilter(new JoinFilter(it));
		return q;
	}
	
	public DynamicQuery on(ISelectionPredicate p)
	{
		IFilter lastFilter = _filters.getLast();
		if (!(lastFilter instanceof JoinFilter)) throw new RuntimeException("Error: previous operation wasn't a join");
		
		DynamicQuery q = new DynamicQuery(this);
		JoinFilter lastJoin = (JoinFilter) q._filters.getLast();
		lastJoin.on(p);
		return q;
	}
	
	public DynamicQuery where(ISelectionPredicate p)
	{	
		DynamicQuery q = new DynamicQuery(this);
		q.addReferencedTables(p.referencedTables());
		
		q.addWhereFilter(p);
		
		return q;
	}
	
	public DynamicQuery count()
	{
		DynamicQuery q = new DynamicQuery(this);
		q.addColumn(new CountColumn());
		
		return q;
	}
	
	private void addReferencedTables(List<ITable> tl)
	{
		/*Iterator<ITable> iter = tl.iterator();
		ITable i;	
		while(iter.hasNext())
		{
			i = iter.next();
			if (!_referencedTables.contains(i)) _referencedTables.add(i);
		}*/
		_referencedTables.addAll(tl);
	}
	
	private void addIgnoredReferencedTable(List<ITable> tl)
	{
		/*Iterator<ITable> iter = tl.iterator();
		ITable i;	
		while(iter.hasNext())
		{
			i = iter.next();
			if (!_refTablesIgnoreFrom.contains(i)) _refTablesIgnoreFrom.add(i);
		}*/
		_refTablesIgnoreFrom.addAll(tl);
	}
	
	public String toSql()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		for (TableColumn c : _referencedCols)
		{
			sb.append(c.toSql());
			sb.append(", ");
		}
		sb.delete(sb.length()-2, sb.length()-1);
		
		sb.append("from ");
		
		for (ITable t : _referencedTables)
		{
			if (_refTablesIgnoreFrom.contains(t)) continue;
			sb.append(t.toSql());
			sb.append(", ");
		}
		
		sb.delete(sb.length()-2, sb.length()-1);
		
		if (_filters.size() > 0)
		{
			for (IFilter f : _filters)
			{
				sb.append(f.toSql());
				sb.append(" ");
			}
		}
		
		if(_whereFilters.size() > 0)
		{
			sb.append("where ");
			
			for (ISelectionPredicate p : _whereFilters)
			{
				sb.append(p.toSql());
				sb.append(" and ");
			}
			sb.delete(sb.length()-5, sb.length());
		}
		
		//sb.append(";");
		return sb.toString();
	}
	
	private LinkedHashSet<ITable> getReferencedTables()
	{
		return _referencedTables;
	}
	
	private LinkedHashSet<TableColumn> getReferencedColumns()
	{
		return _referencedCols;
	}
	
	private LinkedList<IFilter> getFilters()
	{
		return _filters;
	}
	
	private LinkedList<ISelectionPredicate> getWhereFilters()
	{
		return _whereFilters;
	}
	
	private LinkedHashSet<ITable> getIgnoredReferencedTables()
	{
		return _refTablesIgnoreFrom;
	}
	
	private void addColumn(TableColumn col)
	{
		if(!_referencedCols.contains(col)) _referencedCols.add(col);
	}
	
	private void addFilter(IFilter f)
	{
		if (f instanceof WhereFilter) _whereFilters.add(((WhereFilter)f).getPredicate());
		_filters.add(f);
	}
	
	private void addWhereFilter(ISelectionPredicate p)
	{
		_whereFilters.add(p);
	}
 
	private LinkedBlockingDeque<ResultCluster> _results = null;
	
	public static <T extends ITable> T proxyInstanceOf(Class<T> tblClass, InvocationHandler proxyClassInstance)
	{
		return tblClass.cast( Proxy.newProxyInstance(proxyClassInstance.getClass().getClassLoader(), new Class[] {tblClass}, proxyClassInstance));
	}
	
	protected void executeQuery()
	{
		_results = new LinkedBlockingDeque<ResultCluster>(); 
		
		// TODO: make this conform to some sort of global config
		Connection conn = null;
		Properties connectionProps = new Properties();
	    //connectionProps.put("user", "testun");
	    //connectionProps.put("password", "testpass");
	    
	    ResultSet rs = null;
	    
	    try {
			conn = DriverManager.getConnection("jdbc:derby:testdb", connectionProps);
			Statement stmt = conn.createStatement();
		    
			//ResultSet rs = null;
			 rs = stmt.executeQuery(this.toSql());
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    
		try
		{
			HashMap<Integer, ResultCluster> groupedRes = new HashMap<Integer, ResultCluster>();
			while(rs.next())
			{	
				HashMap<String, Object> cols = new HashMap<String, Object>();
				
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
				{
					cols.put(rs.getMetaData().getTableName(i).toLowerCase()+"."+rs.getMetaData().getColumnName(i).toLowerCase(), rs.getObject(i));
				}
				
				String mainClassName = proxyInstanceOf(_mainClass, new TableProxy(_mainClass)).toSql();
				int mainKey = (Integer) cols.get(mainClassName+"."+"id");
				if (!groupedRes.containsKey(mainKey))
				{
					// create new entry
					ResultCluster rc = new ResultCluster(getReferencedTables(), proxyInstanceOf(_mainClass, new ResultRowProxy(getReferencedColumns(), cols, _mainClass)));
					groupedRes.put(mainKey, rc);
				}
				ResultCluster cl = groupedRes.get(mainKey);
				
				for(Method m : _mainClass.getMethods())
				{
					if (m.isAnnotationPresent(HasMany.class) && !m.getName().startsWith("get")) // || m.isAnnotationPresent(HasOne.class) || m.isAnnotationPresent(BelongsTo.class) -- TODO: figure out how to work BelongsTo into this
					{
						Class<?> retType = _mainClass.getMethod("get"+TableProxy.ucFirstLetter(Inflector.pluralize(m.getName()))).getReturnType();
						Class<? extends ITable> retClass;
						if (retType.isArray()) retClass = (Class<? extends ITable>) Class.forName(retType.getName().replaceFirst("\\[.", "").replaceFirst(";", ""));
						else retClass = (Class<? extends ITable>) retType;
						ITable inst = proxyInstanceOf(retClass, new TableProxy(retClass));
						
						if (retType.isArray()) cl.putInList(inst, proxyInstanceOf(retClass, new ResultRowProxy(getReferencedColumns(), cols, retClass))); // TODO: implement for deeper nesting(i.e. lists of resultclusters)
						else cl.putAsSingleton(inst, proxyInstanceOf(retClass, new ResultRowProxy(getReferencedColumns(), cols, retClass)));
					}
				}
				//groupedRes.put(mainKey, cl); // TODO: figgure out if this is actually needed (shouldn't be, b/c Java passes by ref)
			}
			
			for (ResultCluster rc : groupedRes.values())
			{
				_results.put(rc);
			}
		}
		catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected LinkedBlockingDeque<ITable> getResults()
	{
		if (_results.size() == 0) return new LinkedBlockingDeque<ITable>();
		LinkedBlockingDeque<ITable> res = new LinkedBlockingDeque<ITable>(_results.size());
		try
		{
			for (ResultCluster rc : _results)
			{
				res.put(proxyInstanceOf(rc.getMainEntryClass(), rc));
			}
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return res;
	}
	
	public int getCount()
	{
		DynamicQuery countedthis = this.count();
		countedthis.executeQuery();
		return (Integer) ((ResultRowProxy)countedthis.getResults().getFirst()).getColumnValue("null.count(*)");
	}
	
	// start of Collection<ITable> methods
	
	@Override
	public int size()
	{
		return getCount();
	}

	@Override
	public boolean isEmpty()
	{
		return getCount() == 0;
	}

	@Override
	public boolean contains(Object o)
	{
		if (_results == null) executeQuery();
		return getResults().contains(o);
	}

	@Override
	public Iterator<ITable> iterator()
	{
		executeQuery();
		return getResults().iterator(); // TODO: make this better - specifically tailored to given top-level class?
	}

	@Override
	public Object[] toArray()
	{
		executeQuery();
		return getResults().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return _results.toArray(a);
	}

	@Override
	public boolean add(ITable e)
	{
		throw new UnsupportedOperationException("Can't add to a result set");
	}

	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException("Can't remove from a result set");
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		executeQuery();
		return _results.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends ITable> c)
	{
		throw new UnsupportedOperationException("Can't add to a result set");
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		throw new UnsupportedOperationException("Can't remove from a result set");
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException("Can't add to or remove from a result set");
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException("Can't remove from a result set");
	}
	
}
