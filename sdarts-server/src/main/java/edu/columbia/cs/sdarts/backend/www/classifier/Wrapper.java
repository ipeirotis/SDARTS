package edu.columbia.cs.sdarts.backend.www.classifier;

import java.util.AbstractSet;
import java.util.StringTokenizer;
import java.util.TreeSet;



public abstract class Wrapper implements Comparable {

    String title;
    String description;
    String categories;
    Hierarchy hierarchy;

    private int expDelay;
    private int stdevDelay;
    public abstract String getQuery(AbstractSet words);

    public abstract int getMatches(String query);
    public abstract String getSite();
    public abstract int getMatchesAndRemoveDocs(String query);

    //public abstract boolean hasCachedContent();
	//public abstract void deleteCachedContent();

    public void setDescription(String s) {
        this.description = new String(s);
    }

    public String getDescription() {
        return new String(description);
    }

    public void setHierarchy(Hierarchy hier) {
        this.hierarchy = hier;
    }

    public void setCategories(String categories) {
        // A comma-separated list of categories
        this.categories = categories;
    }

    public String[] getCategories() {

        StringTokenizer st = new StringTokenizer(categories, ",");
        TreeSet t = new TreeSet();

        while (st.hasMoreTokens()) {
            String s = st.nextToken();

            // Add the category
            t.add(s);

            // Add its children
            String[] c = hierarchy.getCategories(s);

            for (int i = 0; i < c.length; i++) {
                t.add(c[i]);
            }

            //Add its parents
            c = hierarchy.getParents(s);
            for (int i = 0; i < c.length; i++) {
                t.add(c[i]);
            }

        }
        return (String[]) t.toArray(new String[0]);
    }

    public String[] getSubCategories() {

        StringTokenizer st = new StringTokenizer(categories, ",");
        TreeSet t = new TreeSet();

        while (st.hasMoreTokens()) {
            String s = st.nextToken();

            // Add the category
            t.add(s);

            // Add its children
            String[] c = hierarchy.getCategories(s);

            for (int i = 0; i < c.length; i++) {
                t.add(c[i]);
            }

        }
        return (String[]) t.toArray(new String[0]);
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

    public void setDelay(int exp, int std) {
        expDelay = exp;
        stdevDelay = std;
    }

    public int compareTo(Object o) {
        String q;

        try {
            q = ( (Wrapper) o ).toString();
        }
        catch (Exception e) {
            throw new ClassCastException("Trying to compare TermWeight with non-compatible class") ;
        }
        return this.toString().compareTo(q.toString());
    }




}
