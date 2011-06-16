/**
 * 
 */
package com.sql.dynamicquery;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author DirectXMan12
 * Used to indicate that this method in the helpers is only for use after the
 * results have been collected
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterResultsOnly
{

}
