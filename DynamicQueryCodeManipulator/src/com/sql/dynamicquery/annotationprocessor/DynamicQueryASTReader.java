/**
 * 
 */
package com.sql.dynamicquery.annotationprocessor;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import com.sun.source.tree.MethodTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeMaker;
/**
 * @author DirectXMan12
 *
 */
public class DynamicQueryASTReader extends TreePathScanner<Object, Trees>
{
	private TreeMaker _maker;
	private JCTree.JCClassDecl _currClass; 
	
	public DynamicQueryASTReader(TreeMaker m)
	{
		super();
		_maker = m;
	}
	
	@Override
	public Object visitClass(com.sun.source.tree.ClassTree classTree, Trees trees)
	{
		TreePath path = getCurrentPath();
		TypeElement typeElem = (TypeElement) trees.getElement(path);
		
		_currClass = (JCClassDecl) typeElem;
		
		return super.visitClass(classTree, trees);
	}

	/* (non-Javadoc)
	 * @see com.sun.source.util.TreeScanner#visitMethod(com.sun.source.tree.MethodTree, java.lang.Object)
	 */
	@Override
	public Object visitMethod(MethodTree methodTree, Trees trees)
	{
		TreePath path = getCurrentPath();
		ExecutableElement execElem = (ExecutableElement) trees.getElement(path);
		if (execElem.getAnnotation(com.sql.dynamicquery.Column) == null) return false;
		
		JCTree.JCMethodDecl t = (JCMethodDecl) trees.getTree(execElem);
		t.restype = _maker.ClassLiteral()
		
		return super.visitMethod(methodTree, trees);
	}

}
