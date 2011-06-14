/**
 * 
 */
package com.sql.dynamicquery;

/**
 * @author DirectXMan12
 *
 */
public class JoinFilter implements IFilter
{
	private ITable _newTable;
	private ISelectionPredicate _onPart;
	
	/**
	 * 
	 */
	public JoinFilter(ITable t)
	{
		_newTable = t;
	}
	
	public void on(ISelectionPredicate p)
	{
		_onPart = p;
	}

	/* (non-Javadoc)
	 * @see com.sql.dynamicquery.IFilter#toSql()
	 */
	@Override
	public String toSql()
	{
		return String.format("join %s on %s", _newTable.toSql(), _onPart.toSql());
	}

}
