package com.sql.dynamicquery;


public interface IFilter extends SQLConvertable
{
	public String toSql();
}
