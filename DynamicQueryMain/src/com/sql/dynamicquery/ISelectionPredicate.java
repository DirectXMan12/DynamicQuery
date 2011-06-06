/**
 * 
 */
package com.sql.dynamicquery;

import java.util.ArrayList;

/**
 * @author DirectXMan12
 *
 */
public interface ISelectionPredicate extends SQLConvertable
{
	public String toSql();
	
	public ArrayList<ITable> referencedTables();
}
