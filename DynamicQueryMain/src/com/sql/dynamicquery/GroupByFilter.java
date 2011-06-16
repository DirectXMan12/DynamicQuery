/**
 * 
 */
package com.sql.dynamicquery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author DirectXMan12
 *
 */
public class GroupByFilter implements IFilter
{	
	private TableColumn _groupingCol;
	private ArrayList<TableColumn> _groupingCols;
	
	public GroupByFilter(TableColumn col)
	{
		_groupingCol = col;
	}
	
	public GroupByFilter(Collection<TableColumn> cols)
	{
		_groupingCols = new ArrayList<TableColumn>(cols);
	}
	
	public List<TableColumn> getReferencedColumns()
	{
		if (_groupingCol != null) return Arrays.asList(_groupingCol);
		else return _groupingCols;
	}
	
	/* (non-Javadoc)
	 * @see com.sql.dynamicquery.IFilter#toSql()
	 */
	@Override
	public String toSql()
	{
		if (_groupingCol != null) return String.format("group by %s", _groupingCol.toSql());
		else
		{
			StringBuilder sb = new StringBuilder("group by ");
			for (TableColumn col : _groupingCols)
			{
				sb.append(col.toSql());
				sb.append(",");
			}
			sb.delete(sb.length()-1, sb.length());
			
			return sb.toString();
		}
	}
}
