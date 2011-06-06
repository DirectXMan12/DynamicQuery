/**
 * 
 */
package com.sql.dynamicquery;

import java.util.ArrayList;

/**
 * @author DirectXMan12
 *
 */
public class AndPredicate extends ChainablePredicate
{
	private ISelectionPredicate _preds[];
	
	public AndPredicate(ISelectionPredicate p1, ISelectionPredicate p2)
	{
		_preds = new ISelectionPredicate[] {p1, p2};
	}
	
	/* (non-Javadoc)
	 * @see java.slq.dynamicquery.ISelectionPredicate#toSql()
	 */
	@Override
	public String toSql()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(_preds[0].toSql());
		sb.append(" and ");
		sb.append(_preds[1].toSql());
		
		return sb.toString();
	}

	@Override
	public ArrayList<ITable> referencedTables()
	{
		ArrayList<ITable> tl = new ArrayList<ITable>();
		
		for (ISelectionPredicate p : _preds)
		{
			tl.addAll(p.referencedTables());
		}
		
		return tl;
	}

}
