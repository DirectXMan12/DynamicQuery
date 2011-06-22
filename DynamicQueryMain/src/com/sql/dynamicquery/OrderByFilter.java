/**
 * 
 */
package com.sql.dynamicquery;

/**
 * @author DirectXMan12
 *
 */
public class OrderByFilter implements IFilter, IGroupedClause
{
	public static enum DIRECTION { asc, desc };
	
	private TableColumn _sortingCol;
	private DIRECTION _dir;
	
	public OrderByFilter(TableColumn col, DIRECTION dir)
	{
		_sortingCol = col;
		_dir = dir;
	}
	
	public String subSql()
	{
		return String.format("%s %s",  _sortingCol.toSql(), _dir.toString().toUpperCase());
	}

	@Override
	public String toSql()
	{
		return String.format("order by %s", subSql());
	}

	@Override
	public String getKeyword()
	{
		return "order by";
	}
	
	@Override
	public String toDefinitionSql()
	{
		return this.toSql();
	}

}
