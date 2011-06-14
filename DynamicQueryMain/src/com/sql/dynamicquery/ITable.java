/**
 * 
 */
package com.sql.dynamicquery;


/**
 * @author DirectXMan12
 *
 */
public interface ITable extends SQLConvertable
{
	public TableColumn[] getColumns();
	
	public TableColumn getColumn(String name);
	
	public DynamicQuery where(ISelectionPredicate p);
	
	public DynamicQuery join(ITable it);
	
	public DynamicQuery project();
	
	public DynamicQuery project(TableColumn[] cl);
	
	public Class getActualClass();

}
