/**
 * 
 */
package com.sql.dynamicquery.annotationprocessor.tests;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sql.dynamicquery.annotationprocessor.DynamicQueryAnnotationProcessor;
import com.sun.source.util.JavacTask;

/**
 * @author DirectXMan12
 *
 */
public class TranslatorTests
{

	public static JavaCompiler compiler; 
	public static StandardJavaFileManager fileManager;
	public static final String PROJECT_ROOT_DIR = "C:/Users/DirectXMan12/Eclipse Workspace/PersonalProjects/DynamicQueryCodeManipulator/";
	public static final String PROJECT_SRC_COMPLETE_TESTS_DIR = PROJECT_ROOT_DIR+"tests/com/sql/dynamicquery/annotationprocessor/tests/";
	
	@BeforeClass
	public static void setUpBeforeClass()
	{
		
		compiler = ToolProvider.getSystemJavaCompiler();
	
		//Get a new instance of the standard file manager implementation
		fileManager = compiler.
		        getStandardFileManager(null, null, null);
		
		// Get the list of java file objects, in this case we have only 
		// one file, TestClass.java
		Iterable<? extends JavaFileObject> compUnits = fileManager.getJavaFileObjects("C:/Users/DirectXMan12/Eclipse Workspace/PersonalProjects/DynamicQueryCodeManipulator/tests/testcol/TestCol.java");
		
		// set up options
		//ArrayList<String> opts = new ArrayList<String>(2);
		//opts.add("-classpath .;\"C:\\Users\\DirectXMan12\\Eclipse Workspace\\PersonalProjects\\QueryTests\\bin\";\"C:\\Users\\DirectXMan12\\Eclipse Workspace\\PersonalProjects\\DynamicQueryCodeManipulator\\bin\"");
		//opts.add("-processor com.sql.dynamicquery.annotationprocessor.DynamicQueryAnnotationProcessor");
		//opts.add("-classpath");
		//opts.add(".;\"C:\\Users\\DirectXMan12\\Eclipse Workspace\\PersonalProjects\\QueryTests\\bin\";\"C:\\Users\\DirectXMan12\\Eclipse Workspace\\PersonalProjects\\DynamicQueryCodeManipulator\\bin\"");
		//opts.add("-processor");
		//opts.add("com.sql.dynamicquery.annotationprocessor.DynamicQueryAnnotationProcessor");
		
		DiagnosticCollector<JavaFileObject> diagnostics = null;//new DiagnosticCollector<JavaFileObject>();
		
		// Create the compilation task
		JavacTask compTask = (JavacTask) compiler.getTask(null, fileManager, diagnostics, /*opts*/null, null, compUnits);
		
		// set up processors
		ArrayList<DynamicQueryAnnotationProcessor> procs = new ArrayList<DynamicQueryAnnotationProcessor>(1);
		procs.add(new DynamicQueryAnnotationProcessor());
		compTask.setProcessors(procs);
		
		compTask.call();
		
		/*for (Diagnostic<? extends JavaFileObject> d : diagnostics.getDiagnostics())
		{
			/*if(d.getKind() == Kind.ERROR || d.getKind() == Kind.WARNING || d.getKind() == Kind.MANDATORY_WARNING)*/// System.out.println("Diagnostic Message: "+d.toString());
		//}
	}
	
	private DiagnosticCollector<JavaFileObject> compileFiles(String classpath, String... filenames)
	{
		Iterable<? extends JavaFileObject> compUnits = fileManager.getJavaFileObjects(filenames);
		ArrayList<File> cp = new ArrayList<File>((Collection<? extends File>) fileManager.getLocation(StandardLocation.CLASS_PATH));
		cp.add(new File(classpath));
		try {
			fileManager.setLocation(StandardLocation.CLASS_PATH, cp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		
		JavacTask compTask = (JavacTask) compiler.getTask(null, fileManager, diagnostics, null, null, compUnits);
		compTask.call();
		return diagnostics;
	}
	
	@Test
	public void testBasicCompilation()
	{
		DiagnosticCollector<JavaFileObject> diags = compileFiles(PROJECT_ROOT_DIR+"/tests",PROJECT_SRC_COMPLETE_TESTS_DIR+"TestClass.java");
		for (Diagnostic<? extends JavaFileObject> d : diags.getDiagnostics())
		{
			System.out.println("Diagnostic Message: "+d.toString());
		}
		assertEquals(0, diags.getDiagnostics().size());
	}

}
