/**
 * 
 */
package com.sql.dynamicquery;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author DirectXMan12
 *
 */
public class Inflector
{
	private static Inflector instance;
	
	private HashMap<Pattern, String> _singulars;
	private HashMap<Pattern, String> _plurals;
	
	private Inflector()
	{
		_singulars = new HashMap<Pattern, String>();
		_plurals = new HashMap<Pattern, String>();
		
		// from Rails 3
		
		// populate plurals
		 _plurals.put(Pattern.compile("$", Pattern.CASE_INSENSITIVE), "s");
		 _plurals.put(Pattern.compile("s$", Pattern.CASE_INSENSITIVE), "s");
		 _plurals.put(Pattern.compile("(ax|test)is$", Pattern.CASE_INSENSITIVE), "\\1es");
		 _plurals.put(Pattern.compile("(octop|vir)us$", Pattern.CASE_INSENSITIVE), "\\1i");
		 _plurals.put(Pattern.compile("(octop|vir)i$", Pattern.CASE_INSENSITIVE), "\\1i");
		 _plurals.put(Pattern.compile("(alias|status)$", Pattern.CASE_INSENSITIVE), "\\1es");
		 _plurals.put(Pattern.compile("(bu)s$", Pattern.CASE_INSENSITIVE), "\\1ses");
		 _plurals.put(Pattern.compile("(buffal|tomat)o$", Pattern.CASE_INSENSITIVE), "\\1oes");
		 _plurals.put(Pattern.compile("([ti])um$", Pattern.CASE_INSENSITIVE), "\\1a");
		 _plurals.put(Pattern.compile("([ti])a$", Pattern.CASE_INSENSITIVE), "\\1a");
		 _plurals.put(Pattern.compile("sis$", Pattern.CASE_INSENSITIVE), "ses");
		 _plurals.put(Pattern.compile("(?:([^f])fe|([lr])f)$", Pattern.CASE_INSENSITIVE), "\\1\\2ves");
		 _plurals.put(Pattern.compile("(hive)$", Pattern.CASE_INSENSITIVE), "\\1s");
		 _plurals.put(Pattern.compile("([^aeiouy]|qu)y$", Pattern.CASE_INSENSITIVE), "\\1ies");
		 _plurals.put(Pattern.compile("(x|ch|ss|sh)$", Pattern.CASE_INSENSITIVE), "\\1es");
		 _plurals.put(Pattern.compile("(matr|vert|ind)(?:ix|ex)$", Pattern.CASE_INSENSITIVE), "\\1ices");
		 _plurals.put(Pattern.compile("([m|l])ouse$", Pattern.CASE_INSENSITIVE), "\\1ice");
		 _plurals.put(Pattern.compile("([m|l])ice$", Pattern.CASE_INSENSITIVE), "\\1ice");
		 _plurals.put(Pattern.compile("^(ox)$", Pattern.CASE_INSENSITIVE), "\\1en");
		 _plurals.put(Pattern.compile("^(oxen)$", Pattern.CASE_INSENSITIVE), "\\1");
		 _plurals.put(Pattern.compile("(quiz)$", Pattern.CASE_INSENSITIVE), "\\1zes");
		
		 // populate singulars
		 _singulars.put(Pattern.compile("(n)ews$", Pattern.CASE_INSENSITIVE), "\\1ews");
		 _singulars.put(Pattern.compile("([ti])a$", Pattern.CASE_INSENSITIVE), "\\1um");
		 _singulars.put(Pattern.compile("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", Pattern.CASE_INSENSITIVE), "\\1\\2sis");
		 _singulars.put(Pattern.compile("(^analy)ses$", Pattern.CASE_INSENSITIVE), "\\1sis");
		 _singulars.put(Pattern.compile("([^f])ves$", Pattern.CASE_INSENSITIVE), "\\1fe");
		 _singulars.put(Pattern.compile("(hive)s$", Pattern.CASE_INSENSITIVE), "\\1");
		 _singulars.put(Pattern.compile("(tive)s$", Pattern.CASE_INSENSITIVE), "\\1");
		 _singulars.put(Pattern.compile("([lr])ves$", Pattern.CASE_INSENSITIVE), "\\1f");
		 _singulars.put(Pattern.compile("([^aeiouy]|qu)ies$", Pattern.CASE_INSENSITIVE), "\\1y");
		 _singulars.put(Pattern.compile("(s)eries$", Pattern.CASE_INSENSITIVE), "\\1eries");
		 _singulars.put(Pattern.compile("(m)ovies$", Pattern.CASE_INSENSITIVE), "\\1ovie");
		 _singulars.put(Pattern.compile("(x|ch|ss|sh)es$", Pattern.CASE_INSENSITIVE), "\\1");
		 _singulars.put(Pattern.compile("([m|l])ice$", Pattern.CASE_INSENSITIVE), "\\1ouse");
		 _singulars.put(Pattern.compile("(bus)es$", Pattern.CASE_INSENSITIVE), "\\1");
		 _singulars.put(Pattern.compile("(o)es$", Pattern.CASE_INSENSITIVE), "\\1");
		 _singulars.put(Pattern.compile("(shoe)s$", Pattern.CASE_INSENSITIVE), "\\1");
		 _singulars.put(Pattern.compile("(cris|ax|test)es$", Pattern.CASE_INSENSITIVE), "\\1is");
		 _singulars.put(Pattern.compile("(octop|vir)i$", Pattern.CASE_INSENSITIVE), "\\1us");
		 _singulars.put(Pattern.compile("(alias|status)es$", Pattern.CASE_INSENSITIVE), "\\1");
		 _singulars.put(Pattern.compile("^(ox)en", Pattern.CASE_INSENSITIVE), "\\1");
		 _singulars.put(Pattern.compile("(vert|ind)ices$", Pattern.CASE_INSENSITIVE), "\\1ex");
		 _singulars.put(Pattern.compile("(matr)ices$", Pattern.CASE_INSENSITIVE), "\\1ix");
		 _singulars.put(Pattern.compile("(quiz)zes$", Pattern.CASE_INSENSITIVE), "\\1");
		 _singulars.put(Pattern.compile("(database)s$", Pattern.CASE_INSENSITIVE), "\\1");
		 
		 // populate exceptions
		 _singulars.put(Pattern.compile("person", Pattern.CASE_INSENSITIVE), "people"); 
		 _plurals.put(Pattern.compile("people", Pattern.CASE_INSENSITIVE), "person"); 
		 _singulars.put(Pattern.compile("man", Pattern.CASE_INSENSITIVE), "men"); 
		 _plurals.put(Pattern.compile("men", Pattern.CASE_INSENSITIVE), "man"); 
		 _singulars.put(Pattern.compile("child", Pattern.CASE_INSENSITIVE), "children"); 
		 _plurals.put(Pattern.compile("children", Pattern.CASE_INSENSITIVE), "child"); 
		 _singulars.put(Pattern.compile("sex", Pattern.CASE_INSENSITIVE), "sexes"); 
		 _plurals.put(Pattern.compile("sexes", Pattern.CASE_INSENSITIVE), "sex"); 
		 _singulars.put(Pattern.compile("move", Pattern.CASE_INSENSITIVE), "moves"); 
		 _plurals.put(Pattern.compile("moves", Pattern.CASE_INSENSITIVE), "move"); 
		 _singulars.put(Pattern.compile("cow", Pattern.CASE_INSENSITIVE), "kine"); 
		 _plurals.put(Pattern.compile("kine", Pattern.CASE_INSENSITIVE), "cow"); 

	}

	protected void addP(String re, String rep)
	{
		_plurals.put(Pattern.compile(re, Pattern.CASE_INSENSITIVE), rep);
	}
	
	protected void addS(String re, String rep)
	{
		_singulars.put(Pattern.compile(re, Pattern.CASE_INSENSITIVE), rep);
	}
	
	protected void addE(String singular, String plural)
	{
		_plurals.put(Pattern.compile(singular, Pattern.CASE_INSENSITIVE), plural);
		_singulars.put(Pattern.compile(plural, Pattern.CASE_INSENSITIVE), singular);;
	}
	
	public static Inflector get()
	{
		if (instance == null) instance = new Inflector();
		return instance;
	}
	
	public static void addPlural(String re, String rep)
	{
		Inflector.get().addP(re, rep);
	}
	
	public static void addSingular(String re, String rep)
	{
		Inflector.get().addS(re, rep);
	}
	
	public static void addException(String singular, String plural)
	{
		Inflector.get().addE(singular, plural);
	}

	public static String pluralize(String word)
	{
		Inflector i = Inflector.get();
		for (Pattern p : i._plurals.keySet())
		{
			Matcher m = p.matcher(word);
			if (m.find()) return m.replaceAll(i._plurals.get(p));
		}
		
		return word+"s";
	}
	
	public static String singularize(String word)
	{
		Inflector i = Inflector.get();
		for (Pattern p : i._singulars.keySet())
		{
			Matcher m = p.matcher(word);
			if (m.find()) return i._singulars.get(p).replaceAll("\\\\1", m.group(1));
		}
		
		return word.replaceAll("s$", "");
	}
}
