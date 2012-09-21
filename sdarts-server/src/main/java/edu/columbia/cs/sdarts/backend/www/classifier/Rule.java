package edu.columbia.cs.sdarts.backend.www.classifier;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class Rule {
	String category;
	int pos;
	int neg;
	AbstractSet words;
	AbstractSet negwords;

	public Rule (String cat, String rule) {
		this.category = cat;
		
		String tok;
		StringTokenizer st = new StringTokenizer(rule," ");
		words = new TreeSet();
		while ( st.hasMoreTokens() ) {
			tok = st.nextToken();
			words.add(tok);
		}
	}

	public Rule(Rule rule) {
		this.category=new String(rule.category);
		this.pos=rule.pos;
		this.neg=rule.neg;
		words = new TreeSet();
		words.addAll(rule.words);
		negwords = new TreeSet();
		negwords.addAll(rule.negwords);
	}

	public Rule(String category, AbstractSet terms) {
		this.category=new String(category);
		this.pos=0;
		this.neg=0;
		words = new TreeSet();
		words.addAll(terms);
		negwords =  new TreeSet();
	}

	public Rule(String category, AbstractSet terms, AbstractSet negterms) {
		this.category=new String(category);
		this.pos=0;
		this.neg=0;
		words = new TreeSet();
		words.addAll(terms);
		negwords = new TreeSet();
		negwords.addAll(negterms);
	}


	public void addPosWord(String t) {
		words.add(t);
	}

	public void addNegWord(String t) {
		negwords.add(t);
	}

	public String toString() {
		String r = category +" " + pos + " " + neg + " IF " ;
		for (Iterator it = words.iterator() ; it.hasNext(); )
			r += "Word  ~  "+(String)it.next() + " ";
		for (Iterator it = negwords.iterator() ; it.hasNext(); )
			r += "Word  !~  "+(String)it.next() + " ";
		r+=".";
		return r;
	}

	public String getString() {
		String r = new String() ;
		for (Iterator it = words.iterator() ; it.hasNext(); )
			r += (String)it.next() + " ";
		return r.trim();
	}


	public void setPositive(int positive) {
		this.pos=positive;
	}

	public void setNegative(int negative) {
		this.neg=negative;
	}


	public double getPrecision () {
		return (pos+neg != 0)? ( (float)pos/(pos+neg) ) : 1;
	}

	public String getCategory() {
		return new String(category);
	}

	public AbstractSet getWords() {
		return words;
	}

	public AbstractSet getNegWords() {
		return negwords;
	}

}
