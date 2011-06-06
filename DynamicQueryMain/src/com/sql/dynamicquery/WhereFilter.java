/**
 * 
 */
package com.sql.dynamicquery;

import java.util.LinkedList;

/**
 * @author DirectXMan12
 *
 */
public class WhereFilter implements IFilter
{
	private ISelectionPredicate _pred;
	
	public WhereFilter(ISelectionPredicate p)
	{
		_pred = p;
	}

	@Override
	public String toSql()
	{
		return String.format("where %s", _pred.toSql());
	}
	
	public ISelectionPredicate getPredicate()
	{
		return _pred;
	}
}
