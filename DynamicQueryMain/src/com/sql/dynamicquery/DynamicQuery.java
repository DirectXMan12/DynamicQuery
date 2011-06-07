/**
 * 
 */
package com.sql.dynamicquery;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingDeque;

import com.sun.org.apache.bcel.internal.util.Class2HTML;

/**
 * @author DirectXMan12
 *
 */
public class DynamicQuery implements SQLConvertable, Collection<ITable>
{
	
	private LinkedList<ITable> _referencedTables;
	private LinkedList<TableColumn> _referencedCols;
	private LinkedList<ISelectionPredicate> _whereFilters;
	private LinkedList<IFilter> _filters;
	
	private Class<? extends ITable> _mainClass;
	
	public DynamicQuery()
	{
		_referencedTables = new LinkedList<ITable>();
		_referencedCols = new LinkedList<TableColumn>();
		_filters = new LinkedList<IFilter>();
		_whereFilters = new LinkedList<ISelectionPredicate>();
		
		_mainClass = null;
	}
	
	public DynamicQuery(DynamicQuery q)
	{
		_referencedTables = (LinkedList<ITable>) q.getReferencedTables().clone();
		_referencedCols = (LinkedList<TableColumn>) q.getReferencedColumns().clone();
		_filters = (LinkedList<IFilter>) q.getFilters().clone();
		_whereFilters = (LinkedList<ISelectionPredicate>) q.getWhereFilters().clone();
		
		_mainClass = q._mainClass;
	}
	
	public DynamicQuery(Class<? extends ITable> mainClass)
	{
		_referencedTables = new LinkedList<ITable>();
		_referencedCols = new LinkedList<TableColumn>();
		_filters = new LinkedList<IFilter>();
		_whereFilters = new LinkedList<ISelectionPredicate>();
		
		_mainClass = mainClass;
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
	
	public DynamicQuery where(ISelectionPredicate p)
	{	
		addReferencedTables(p.referencedTables());
	
		DynamicQuery q = new DynamicQuery(this);
		
		q.addWhereFilter(p);
		
		return q;
	}
	
	public DynamicQuery count()
	{
		DynamicQuery q = new DynamicQuery(this);
		q.addColumn(new CountColumn());
		
		return q;
	}
	
	private void addReferencedTables(ArrayList<ITable> tl)
	{
		Iterator<ITable> iter = tl.iterator();
		ITable i;	
		while(iter.hasNext())
		{
			i = iter.next();
			if (!_referencedTables.contains(i)) _referencedTables.add(i);
		}
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
	
	private LinkedList<ITable> getReferencedTables()
	{
		return _referencedTables;
	}
	
	private LinkedList<TableColumn> getReferencedColumns()
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
 
	private LinkedBlockingDeque<ITable> _results = null;
	
	protected void executeQuery()
	{
		_results = new LinkedBlockingDeque<ITable>(); 
		
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
			while(rs.next())
			{	
				HashMap<String, Object> cols = new HashMap<String, Object>();
				
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
				{
					cols.put(rs.getMetaData().getTableName(i).toLowerCase()+"."+rs.getMetaData().getColumnName(i).toLowerCase(), rs.getObject(i));
				}
				
				//ITable it;
				//if (_mainClass != null) it = _mainClass.cast( Proxy.newProxyInstance(ResultRowProxy.class.getClassLoader(), new Class[] {_mainClass}, new ResultRowProxy(getReferencedColumns(), cols, _mainClass)) );
				//else  it = (ITable) Proxy.newProxyInstance(ResultRowProxy.class.getClassLoader(), new Class[] {ITable.class}, new ResultRowProxy(getReferencedColumns(), cols, null));
				
				_results.add(_mainClass.cast( Proxy.newProxyInstance(ResultRowProxy.class.getClassLoader(), new Class[] {_mainClass}, new ResultRowProxy(getReferencedColumns(), cols, _mainClass)) ));
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
		return _results;
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
		return _results.contains(o);
	}

	@Override
	public Iterator<ITable> iterator()
	{
		executeQuery();
		return _results.iterator(); // TODO: make this better - specifically tailored to given top-level class?
	}

	@Override
	public Object[] toArray()
	{
		executeQuery();
		return _results.toArray();
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
