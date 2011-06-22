/**
 * 
 */
package com.sql.dynamicquery;

/**
 * @author DirectXMan12
 *
 */
public class HavingFilter implements IFilter, IGroupedClause
{

	private ISelectionPredicate _pred;
	
	public HavingFilter(ISelectionPredicate p)
	{
		_pred = p;
	}
	
	/* (non-Javadoc)
	 * @see com.sql.dynamicquery.IFilter#toSql()
	 */
	@Override
	public String toSql()
	{
		return String.format("having %", _pred.toSql());
	}
	
	public ISelectionPredicate getPredicate()
	{
		return _pred;
	}

	@Override
	public String getKeyword()
	{
		return "having";
	}
	
	@Override
	public String toDefinitionSql()
	{
		return this.toSql();
	}
}
