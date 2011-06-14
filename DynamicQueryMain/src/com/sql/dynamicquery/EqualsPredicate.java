/**
 * 
 */
package com.sql.dynamicquery;

import java.util.ArrayList;

/**
 * @author DirectXMan12
 *
 */
public class EqualsPredicate extends ChainablePredicate
{

	private TableColumn _lhs;
	private Object _rhs;
	
	public EqualsPredicate(TableColumn col, Object val)
	{
		_lhs = col;
		_rhs = val;
	}

	@Override
	public String toSql()
	{
		if (_rhs instanceof Integer) return String.format("%s = %s", _lhs.toSql(), _rhs.toString());
		else if (_rhs instanceof SQLConvertable) return String.format("%s = %s", _lhs.toSql(), ((SQLConvertable) _rhs).toSql());
		else return String.format("%s = \"%s\"", _lhs.toSql(), _rhs.toString());
	}

	@Override
	public ArrayList<ITable> referencedTables()
	{
		ArrayList<ITable> res = new ArrayList<ITable>();
		res.add(_lhs.getTable());
		return res;
	}

}