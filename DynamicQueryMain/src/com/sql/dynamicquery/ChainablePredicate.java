package com.sql.dynamicquery;

public abstract class ChainablePredicate implements ISelectionPredicate
{
	public AndPredicate and(ISelectionPredicate p)
	{
		return new AndPredicate(this, p);
	}
	
	public String toDefinitionSql()
	{
		return this.toSql();
	}
}
