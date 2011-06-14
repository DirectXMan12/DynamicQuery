package com.sql.dynamicquery.tests;

import com.sql.dynamicquery.*;

public interface App extends ITable
{
	@Column
	public int id();
	
	@Column
	public String name();
	
	@BelongsTo
	public User user();

}
