/*
 * Created on Feb 8, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.columbia.cs.sdarts.backend.www.classifier;


import java.io.File;


import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import VisualNumerics.math.MathException;

/**
 * @author Yan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Classifier {

	private Vector rules;
	private Vector categories;
	private String description;
	private String title;
	private String filename;
	private boolean isOrdered;
	private double[][] cm; //confusion matrix

	public Classifier() {
		rules = new Vector();
		categories = new Vector();
	}
	
	public void AddRule(Rule rule)
	{
		if (!categories.contains(rule.getCategory()))
			categories.add(rule.getCategory());
			
		rules.add(rule);
	}
	
	public boolean isOrdered() {
		return isOrdered;
	}

	public String getFilename() {
		return filename;
	}

	public void setDescription(String s) {
		this.description = new String(s);
	}

	public String getDescription() {
		return new String(description);
	}

	public void setTitle(String s) {
		this.title = new String(s);
	}

	public String getTitle() {
		return new String(title);
	}

	public String toString() {
		return new String(title);
	}

	public Vector getRules() {
		return rules;
	}

	public int getNumberOfRules() {
		return rules.size();
	}

	public Rule getRule(int index) {
		return new Rule((Rule) rules.get(index));
	}

	public TreeSet getWords() {

		TreeSet result = new TreeSet();

		for (Iterator it = rules.iterator(); it.hasNext();) {
			Rule r = (Rule) it.next();

			result.addAll(r.getWords());
			result.addAll(r.getNegWords());
		}

		return result;

	}

	public String[] getCategories() {
		return (String[]) categories.toArray(new String[0]);
	}

	public int resolveCategory(String category) {
		return categories.indexOf(category);
	}

	public int getNumberOfCategories() {
		return categories.size();
	}

	public void setConfusionMatrix(double[][] confm) {
		int n = getNumberOfCategories();

		cm = new double[n][n];

		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				this.cm[i][j] = confm[i][j];
	}


	public double[][] getInverseConfusionMatrix() {
		double[][] im = null;

		try {
			im = VisualNumerics.math.DoubleMatrix.inverse(cm);
		} catch (MathException  e) {
			System.out.println("The matrix is not inversible!");
		}
		return im;
	}

}

