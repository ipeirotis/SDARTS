package edu.columbia.cs.sdarts.backend.www.classifier;

import java.util.Iterator;

public class TermSet extends java.util.TreeSet implements Comparable {

	public int compareTo(Object o) {
		Iterator it1 = this.iterator();
		Iterator it2;
		try {
			it2 = ( (TermSet)o ).iterator();
		} catch (Exception e) { throw new ClassCastException("Trying to compare TermSet with a non-compatible class") ; }

		while (it1.hasNext() && it2.hasNext()) {
			try {
				String t1 = (String)it1.next();
				String t2 = (String)it2.next();
				int c = t1.compareTo(t2);
				if (c!=0) return c;
			} catch (Exception e) { throw new ClassCastException("Trying to compare TermSet with a non-compatible class") ; }
		}
		// If we reached this point, one of the two sets has finished

		if ( it1.hasNext() && !it2.hasNext()) return -1;
		if (!it1.hasNext() &&  it2.hasNext()) return 1;
		return 0; //both iterators finished


		//return term.compareTo(q.toString());
	}

	public boolean equals(Object o) {
		Iterator it1 = this.iterator();
		Iterator it2;
		try {
			it2 = ( (TermSet)o ).iterator();
		} catch (Exception e) { return false; }

		while (it1.hasNext() && it2.hasNext()) {
			try {
				String t1 = (String)it1.next();
				String t2 = (String)it2.next();
				int c = t1.compareTo(t2);
				if (c!=0) return false;
			} catch (Exception e) { return false; }
		}
		// If we reached this point, one of the two sets has finished

		if (it1.hasNext() || it2.hasNext()) return false;
		else return true;

	}

	public TermSet intersection(TermSet ts) {

		TermSet copy = new TermSet();
		copy.addAll(this);
		copy.retainAll(ts);

		return copy;
	}

	public TermSet difference(TermSet ts) {

		TermSet copy = new TermSet();
		copy.addAll(this);
		copy.removeAll(ts);

		return copy;
	}



	public String toString() {
		StringBuffer result = new StringBuffer();
		Iterator it = this.iterator();
		while (it.hasNext()) {
			String t = (String) it.next();
			result.append(t);
			result.append(" ");
		}
		return result.toString().trim();
	}


}