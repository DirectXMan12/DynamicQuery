package com.sql.dynamicquery.tests;

import com.sql.dynamicquery.*;

public interface User extends ITable
{
	@Column
	public int id();
	
	@Column
	public String name();

}
