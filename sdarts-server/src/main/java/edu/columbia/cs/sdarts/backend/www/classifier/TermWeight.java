package edu.columbia.cs.sdarts.backend.www.classifier;


public class TermWeight implements Comparable {

	private String term;
	private double weight;

	public TermWeight(String term, double weight) {
		this.term=new String(term);
		this.weight=weight;
	}


	public void increaseSize() { weight++; }
	public void decreaseSize() { weight--; }

	public String getTerm()   { return term; }
	public double getWeight()   { return weight; }
	public void setWeight(double w)   { this.weight=w; }

	public String toString() {
		return "("+term+","+weight+")";
	}

	public int compareTo(Object o) {
		String q = new String();
		try {
			q = ( (TermWeight)o ).getTerm();
		} catch (Exception e) { throw new ClassCastException("Trying to compare TermWeight with non-compatible class") ; }

		return term.compareTo(q.toString());
	}

	public boolean equals(Object o) {
		String q;
		try {
			q = ( (TermWeight)o ).getTerm();
		} catch (Exception e) { return false; }

		if ( q.equals(this.term) ) { return true; }
		return false;
	}


}
