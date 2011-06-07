/**
 * 
 */
package com.sql.dynamicquery;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author DirectXMan12
 *
 */
public class InPredicate extends ChainablePredicate
{
	private TableColumn _lhs;
	private Collection<?> _rhs;
	
	
	public InPredicate(TableColumn col, Collection<?> list)
	{
		_lhs = col;
		_rhs = list;
	}
	
	/* (non-Javadoc)
	 * @see com.sql.dynamicquery.ISelectionPredicate#toSql()
	 */
	@Override
	public String toSql()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s in (", _lhs.toSql()));
		if (_rhs instanceof SQLConvertable) sb.append(((SQLConvertable) _rhs).toSql());
		else
		{
			for (Object i : _rhs)
			{
				sb.append(i.toString());
				sb.append(",");
			}
			sb.replace(sb.length()-1, sb.length(), ""); // remove last comma
		}

		sb.append(")");
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see com.sql.dynamicquery.ISelectionPredicate#referencedTables()
	 */
	@Override
	public ArrayList<ITable> referencedTables()
	{
		ArrayList<ITable> res = new ArrayList<ITable>();
		res.add(_lhs.getTable());
		return res;
	}

}
