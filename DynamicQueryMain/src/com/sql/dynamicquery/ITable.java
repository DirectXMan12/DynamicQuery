/**
 * 
 */
package com.sql.dynamicquery;

import java.io.ObjectInputStream.GetField;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author DirectXMan12
 *
 */
public interface ITable
{
	public TableColumn[] getColumns();
	
	public TableColumn getColumn(String name);
	
	public DynamicQuery where(ISelectionPredicate p);
	
	public DynamicQuery project();
	
	public DynamicQuery project(TableColumn[] cl);
	
	public Class getActualClass();
}
