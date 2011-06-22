/**
 * 
 */
package com.sql.dynamicquery;


/**
 * @author DirectXMan12
 *
 */
public class WhereFilter implements IFilter, IGroupedClause
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
	
	@Override
	public String toDefinitionSql()
	{
		return this.toSql();
	}

	@Override
	public String getKeyword()
	{
		return "where";
	}
	
	
}
