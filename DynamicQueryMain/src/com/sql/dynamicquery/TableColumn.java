/**
 * 
 */
package com.sql.dynamicquery;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import com.sun.tools.javac.util.Name.Table;

/**
 * @author DirectXMan12
 *
 */
public class TableColumn implements SQLConvertable
{
	private ITable _parentTable;
	private String _columnName;
	private String _columnAlias = null;
	
	public TableColumn(ITable pt, String name)
	{
		_parentTable = pt;
		_columnName = name;
	}
	
	public TableColumn(ITable t, Method m)
	{
		_parentTable = t;
		_columnName = m.getName();
	}
	
	public ITable getTable() { return _parentTable; }
	public String getName() { return _columnName; }
	
	public EqualsPredicate eq(Number val)
	{
		return new EqualsPredicate(this, val);
	}
	
	public EqualsPredicate eq(SQLConvertable val)
	{
		return new EqualsPredicate(this, val);
	}
	
	public EqualsPredicate eq(Object val)
	{
		return new EqualsPredicate(this, val.toString());
	}
	
	public InPredicate in(Collection<?> list)
	{
		return new InPredicate(this, list);
	}
	
	public String toDefinitionSql()
	{
		if (_columnAlias == null) return toSql();
		else return _parentTable.toSql()+"."+_columnName+" as "+_columnAlias;
	}

	public String toSql()
	{
		if (_columnAlias == null) return _parentTable.toSql()+"."+_columnName;
		else return _columnAlias;
	}

	public void setAlias(String a)
	{
		_columnAlias = a;
	}
	
	public String getAlias()
	{
		return _columnAlias;
	}
	
	public Boolean isAliased()
	{
		return _columnAlias != null;
	}
	
	public TableColumn as(String alias)
	{
		TableColumn res = (TableColumn) this.clone();
		res.setAlias(alias);
		return res;
	}

	protected void copyAttrsTo(TableColumn tc)
	{
		tc._columnName = this._columnName;
		tc._columnAlias = this._columnAlias;
		tc._parentTable = this._parentTable;
	}
	
	@Override
	protected Object clone()
	{
		Object res = this.getClass().cast(new Object());
		this.copyAttrsTo((TableColumn) res);
		return res;
		
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_columnName == null) ? 0 : _columnName.hashCode());
		result = prime * result + ((_parentTable == null) ? 0 : _parentTable.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof TableColumn)) return false;
		TableColumn other = (TableColumn) obj;
		if (_columnName.equals(other._columnName) && _parentTable.equals(other._parentTable)) return true;
		else return false;
	}

	public CountColumn count()
	{
		return new CountColumn(this);
	}
}
