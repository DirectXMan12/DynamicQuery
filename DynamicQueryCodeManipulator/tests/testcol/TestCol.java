package testcol;

import com.sql.dynamicquery.Column;
import com.sql.dynamicquery.DynamicQuery;
import com.sql.dynamicquery.ISelectionPredicate;
import com.sql.dynamicquery.ITable;
import com.sql.dynamicquery.TableColumn;

/**
 * @author DirectXMan12
 *
 */ 
public interface TestCol extends ITable
{
	@Column
	public int id();
	
	public String cheese();
}
