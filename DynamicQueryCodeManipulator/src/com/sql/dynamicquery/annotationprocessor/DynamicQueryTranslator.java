/**
 * 
 */
package com.sql.dynamicquery.annotationprocessor;

import java.util.ArrayList;
import java.util.Arrays;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

/**
 * @author DirectXMan12
 *
 */
public class DynamicQueryTranslator extends TreeTranslator
{
	private TreeMaker _make;
	private Messager _messager;
	private Name.Table _names;
	public ArrayList<JCTree> voidRes;
	public ArrayList<JCTree> dontProcess;
	public ArrayList<JCTree> addIdMethods;
	public ArrayList<JCTree> hasManyMethods;
	
	public DynamicQueryTranslator(TreeMaker m, Messager msgr, Name.Table names, ArrayList<JCTree> dp, ArrayList<JCTree> aidm, ArrayList<JCTree> hmm)
	{
		super();
		_make = m;
		_messager = msgr;
		_names = names;
		dontProcess = dp;
		voidRes = new ArrayList<JCTree>();
		addIdMethods = aidm;
		hasManyMethods = hmm;
	}

	
	/* (non-Javadoc)
	 * @see com.sun.tools.javac.tree.TreeTranslator#visitClassDef(com.sun.tools.javac.tree.JCTree.JCClassDecl)
	 */
	@Override
	public void visitClassDef(JCClassDecl classDecl) {
		//_messager.printMessage(Kind.NOTE, "hit class decl "+classDecl.name.toString());
		super.visitClassDef(classDecl);
		
		if (this.voidRes == null)
		{
			_messager.printMessage(Kind.NOTE, "--> skipping class def "+ classDecl.name.toString()+"...");
			this.result = classDecl;
			return;
		}
		else _messager.printMessage(Kind.NOTE, "--> processing class def "+ classDecl.name.toString()+"...");

		JCTree defs[] = classDecl.defs.toArray(new JCTree[] {});
		ArrayList<JCTree> defsAL = new ArrayList<JCTree>(Arrays.asList(defs));
		defsAL.addAll(voidRes);
		 
		this.result = classDecl;
		((JCClassDecl)this.result).defs = List.from(defsAL.toArray(new JCTree[] {}));
		//this.result = newClass;
	}
	
	@Override
	public void visitMethodDef(JCMethodDecl methDecl)
	{
		//_messager.printMessage(Kind.NOTE, "hit meth decl "+methDecl.getName().toString());
		super.visitMethodDef(methDecl);
		if (methDecl.name.toString().startsWith("get"))
		{
			this.result = methDecl;
			_messager.printMessage(Kind.NOTE, "--> skipping method def " + methDecl.getName().toString() + "...");
			return;
		}
		else if (dontProcess.contains(methDecl))
		{
			this.result = methDecl;
			_messager.printMessage(Kind.NOTE, "--> skipping method def b/c on don't process list -- " + methDecl.getName().toString() + "...");
			return;
		}
		else _messager.printMessage(Kind.NOTE, "--> processing method def " + methDecl.getName().toString() + "...");
		JCTree newGetter = genGetter(methDecl);

		
		if (addIdMethods.contains(methDecl))
		{
			this.voidRes.add(makeIdMethod(methDecl));
			this.voidRes.add(makeIdGetter(methDecl));
		}
		
		if (hasManyMethods.contains(methDecl)) this.voidRes.add(makeDQMethod(methDecl));
		else this.voidRes.add(makeTypeVoid(methDecl));
		
		this.result = newGetter;
	}
	
	// gain access to JCPrimativeTypeTree, whose constructor is protected
	public static class MyPrimTypeTree extends JCTree.JCPrimitiveTypeTree
	{

		protected MyPrimTypeTree(int typeTag)
		{
			super(typeTag);
		}
		
	}
	// gain access to JCIdent, whose constructor is protected
	public static class MyIdent extends JCTree.JCIdent
	{

		protected MyIdent(Name name, Symbol sym)
		{
			super(name, sym);
		}
		
	}
	
	public static class MyFieldAccess extends JCFieldAccess
	{

		protected MyFieldAccess(JCExpression selected, Name selector, Symbol sym)
		{
			super(selected, selector, sym);
		}
		
	}
	
	private JCTree makeTypeVoid(JCMethodDecl md)
	{
		JCMethodDecl newMd = (JCMethodDecl) md.clone();
		//newMd.restype = new MyPrimTypeTree(TypeTags.VOID);
		//ClassSymbol sym = new MyFieldAccess(Type., selector, sym)
		//newMd.restype = new MyIdent(name, sym);
		
		newMd.restype = new MyIdent(_names.fromString("TableColumn"), null);
		newMd.setPos(newMd.pos+1);
		return newMd;
	}

	private JCTree genGetter(JCMethodDecl md)
	{
		/*List<JCStatement> bodySts = List.nil();
		bodySts.add(_make.Return(_make.Literal(1)));
		JCBlock body = _make.Block(0, bodySts);*/
		String oldName = md.getName().toString();
		oldName = Character.toUpperCase(oldName.charAt(0))+oldName.substring(1); // capitalize first letter
		Name newName = Name.fromString(md.getName().table, "get"+oldName);
		
		JCMethodDecl newMd = (JCMethodDecl) md.clone();
		newMd.name = newName;
		
		
		return newMd;
	}
	
	private JCTree makeIdMethod(JCMethodDecl md)
	{
		JCMethodDecl newMd = (JCMethodDecl) md.clone();
		
		newMd.restype = new MyIdent(_names.fromString("TableColumn"), null);
		
		String oldName = md.getName().toString();
		Name newName = Name.fromString(md.getName().table, oldName+"Id");
		
		newMd.name = newName;
		
		newMd.setPos(newMd.pos+1);
		
		return newMd;
	}
	
	private JCTree makeIdGetter(JCMethodDecl md)
	{
		String oldName = md.getName().toString();
		oldName = Character.toUpperCase(oldName.charAt(0))+oldName.substring(1); // capitalize first letter
		Name newName = Name.fromString(md.getName().table, "get"+oldName+"Id");
		
		JCMethodDecl newMd = (JCMethodDecl) md.clone();
		
		newMd.name = newName;
		newMd.restype = new MyIdent(_names.fromString("Integer"), null); // Assume int, TODO: change maybe to not just assume int?
		
		return newMd;
	}
	
	private JCTree makeDQMethod(JCMethodDecl md)
	{
		JCMethodDecl newMd = (JCMethodDecl) md.clone();

		
		newMd.restype = new MyIdent(_names.fromString("DynamicQuery"), null);
		newMd.setPos(newMd.pos+1);
		return newMd;
	}
}
