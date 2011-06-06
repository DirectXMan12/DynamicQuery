package com.sql.dynamicquery.annotationprocessor;

import java.util.ArrayList;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.sun.source.util.JavacTask;

public class MainManipulatorRunner
{
	public static void go()
	{
	
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	
		//Get a new instance of the standard file manager implementation
		StandardJavaFileManager fileManager = compiler.
		        getStandardFileManager(null, null, null);
		        
		// Get the list of java file objects, in this case we have only 
		// one file, TestClass.java
		Iterable<? extends JavaFileObject> compUnits = 
		        fileManager.getJavaFileObjects("TestClass.java");
		
		ArrayList<String> opts = new ArrayList<String>(1);
		opts.add("-processor DynamicQueryAnnotationProcessor");
		// Create the compilation task
		JavacTask compTask = (JavacTask) compiler.getTask(null, fileManager, null, opts, null, compUnits);
		compTask.call();
	}

}
