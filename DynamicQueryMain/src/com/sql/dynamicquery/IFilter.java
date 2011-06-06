package com.sql.dynamicquery;

import java.util.ArrayList;

public interface IFilter extends SQLConvertable
{
	public String toSql();
}
