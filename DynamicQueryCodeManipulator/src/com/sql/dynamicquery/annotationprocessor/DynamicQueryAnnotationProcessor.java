package com.sql.dynamicquery.annotationprocessor;

import java.util.ArrayList;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Name;

@SupportedAnnotationTypes({"*"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class DynamicQueryAnnotationProcessor extends AbstractProcessor
{
	private Filer _filer;
	private Messager _messager;
	private Elements _elemUtils;
	private JavacProcessingEnvironment _env;
	public boolean _alreadyProcessed = false;
	
	/* (non-Javadoc)
	 * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.processing.ProcessingEnvironment)
	 */
	@Override
	public synchronized void init(ProcessingEnvironment env)
	{
		super.init(env);
		
		_filer = env.getFiler();
		_messager = env.getMessager();
		_elemUtils = env.getElementUtils();
		_env = (JavacProcessingEnvironment) env;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		if(!roundEnv.processingOver() && !_alreadyProcessed)
		{
			for (Element oe : roundEnv.getRootElements())
			{
				boolean procThis = false;
				ArrayList<JCTree> dontProcess = new ArrayList<JCTree>();
				//_messager.printMessage(Kind.NOTE, "loop hit outer element "+oe.toString());
				for (Element e : oe.getEnclosedElements())
				{
					//_messager.printMessage(Kind.NOTE, "loop hit element "+e.toString());
					if (e.getAnnotation(com.sql.dynamicquery.Column.class) != null) procThis = true;
					else dontProcess.add((JCTree) Trees.instance(_env).getTree(e));
				}
				if (procThis)
				{
					JCTree tree = (JCTree) Trees.instance(_env).getTree(oe);
					TreeTranslator trans = new DynamicQueryTranslator(TreeMaker.instance(_env.getContext()),_messager, Name.Table.instance(_env.getContext()), dontProcess);
					tree.accept(trans);
					_messager.printMessage(Kind.NOTE, "finished outer elem "+oe.toString());
				}
			}
			_alreadyProcessed = true;
		}
		else _messager.printMessage(Kind.NOTE, "processing over");
		return false;
	}

}
