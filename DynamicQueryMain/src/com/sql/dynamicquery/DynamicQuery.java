/**
 * 
 */
package com.sql.dynamicquery;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList; 

/**
 * @author DirectXMan12
 *
 */
public class DynamicQuery implements SQLConvertable
{
	
	private LinkedList<ITable> _referencedTables;
	private LinkedList<TableColumn> _referencedCols;
	private LinkedList<ISelectionPredicate> _whereFilters;
	private LinkedList<IFilter> _filters;
	
	public DynamicQuery()
	{
		_referencedTables = new LinkedList<ITable>();
		_referencedCols = new LinkedList<TableColumn>();
		_filters = new LinkedList<IFilter>();
		_whereFilters = new LinkedList<ISelectionPredicate>();
	}
	
	public DynamicQuery(DynamicQuery q)
	{
		_referencedTables = (LinkedList<ITable>) q.getReferencedTables().clone();
		_referencedCols = (LinkedList<TableColumn>) q.getReferencedColumns().clone();
		_filters = (LinkedList<IFilter>) q.getFilters().clone();
		_whereFilters = (LinkedList<ISelectionPredicate>) q.getWhereFilters().clone();
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
			sb.append(TableProxy.getActualLocalName(c.getTable()));
			sb.append(".");
			sb.append(c.getName());
			sb.append(", ");
		}
		sb.delete(sb.length()-2, sb.length()-1);
		
		sb.append("from ");
		
		for (ITable t : _referencedTables)
		{
			sb.append(TableProxy.getActualLocalName(t));
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
	
}
