package edu.columbia.cs.sdarts.backend.www.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

public class Node {

    String name;
    Vector children;
    Node parent;
    int level;
    Classifier c; // If it is null, then the node is a leaf!

	/**
	 * Constructor
	 *
	 */
	Node(Node parent, String name) throws NoSuchFieldException {

		this.name = name;
		this.parent = parent;
		this.children = new Vector();
		
		//		Set the level of this node
		if (parent == null) {
			 level = 0;
		} else {
			 this.level = parent.getLevel() + 1;
		}
	}
	
	public void AddRule(Rule rule)
	{
		if (this.c == null)
			this.c = new Classifier();
			
		c.AddRule(rule);
	}
			
    /**
     * Auxilliary method for classification. It takes the Coverage vector and returns the Specificity vector
     *
     */
    private double[] getSpecVector(double[] covVector) {

        double[] result = new double[covVector.length];

        double sum = 0;

        for (int i = 0; i < covVector.length; i++) {
            sum += covVector[i];
        }

        if (sum == 0) {
            sum = 1;
        }

        for (int i = 0; i < covVector.length; i++) {
            result[i] = covVector[i] / sum;
        }

        return result;
    }

    /**
     * Return all the categories in the subtree of this node
     */
    public String[] getCategories() {

        TreeSet t = new TreeSet();

        t.add(name);
        for (int j = 0; j < children.size(); j++) {
            String[] s = ((Node) children.get(j)).getCategories();

            for (int i = 0; i < s.length; i++) {
                t.add(s[i]);
            }
        }
        return (String[]) t.toArray(new String[0]);
    }

    public String[] getChildren() {
        if (children.size() == 0) return null;
        String[] result = new String[children.size()];

        for (int j = 0; j < children.size(); j++) {
            result[j] = ((Node) children.get(j)).getName();
        }
        return result;

    }

    /**
     * Return all the categories in the subtree of this node
     */
    String[] getCategories(int levelLimit) {

        // If this node is already deeper than the limit, return null
        if (level > levelLimit) return null;

        TreeSet t = new TreeSet();

        t.add(name);

        // If the limit is smaller than the currect depth, then continue with the children
        if (level < levelLimit) {
            for (int j = 0; j < children.size(); j++) {
                String[] s = ((Node) children.get(j)).getCategories(levelLimit);

                for (int i = 0; i < s.length; i++) {
                    t.add(s[i]);
                }
            }
        }
        return (String[]) t.toArray(new String[0]);
    }

    String[] getCategories(String nodeName) {
        if (nodeName.equals(name))
            return getCategories();
        else {
            TreeSet t = new TreeSet();

            for (int j = 0; j < children.size(); j++) {
                String[] s = ((Node) children.get(j)).getCategories(nodeName);

                if (s != null) {
                    for (int i = 0; i < s.length; i++) {
                        t.add(s[i]);
                    }
                }
            }
            return (String[]) t.toArray(new String[0]);
        }
    }

    Node getParent() {
        return parent;
    }

    String[] getParents() {
        TreeSet t = new TreeSet();
        Node par = parent;

        while (par != null) {
            t.add(par.getName());
            par = par.getParent();
        }
        if (t.isEmpty())
            return null;
        else
            return (String[]) t.toArray(new String[0]);
    }

    public Node getParentAtLevel(int l) {

        if (l >= this.level) return this;

        Node par = parent;

        while (par != null) {
            if (par.getLevel() == l)
                return par;
            par = par.getParent();
        }

        return null;

    }

    public Node getNode(String nodeName) {

        if (nodeName.equals(name))
            return this;
        else {
            for (int j = 0; j < children.size(); j++) {
                Node n = ((Node) children.get(j)).getNode(nodeName);

                if (n != null) return n;
            }
        }
        return null;

    }

    String[] getParents(String nodeName) {
        if (nodeName.equals(name))
            return getCategories();
        else {
            for (int j = 0; j < children.size(); j++) {
                String[] s = ((Node) children.get(j)).getParents(nodeName);

                if (s != null) {
                    return s;
                }
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }

    public int getLevel() {
        return level;
    }


    String[] classifyVector(int[] correct, String[] categories, double ts, double tc) {

        // If the database has been pushed into a leaf node then it should be classified here
        if (c == null) return getCategories();

        TreeSet result = new TreeSet();

        int n = c.getNumberOfCategories();
        double[] corrCoverage = new double[n];
		for (int l=0; l<categories.length; l++) {
        	int k = c.resolveCategory(categories[l]);
        	if (k!=-1) corrCoverage[k] = correct[l];
		}

		double[] corSpec;
		double corSum = 0;

		for (int g = 0; g < corrCoverage.length; g++) {
			corSum += corrCoverage[g];
		}

		if (corSum == 0) {
			corSpec = corrCoverage;
		} else {
			corSpec = getSpecVector(corrCoverage);
		}


		for (int i = 0; i < corSpec.length; i++) {
			if (corSpec[i] >= ts && corrCoverage[i] >= tc) {
				String[] s = ((Node) children.get(i)).classifyVector(correct, categories, ts, tc);

				if (s != null) {
					for (int j = 0; j < s.length; j++) {
						result.add(s[j]);
					}
				}
			}
		}

        if (result.size() == 0) {
            return getCategories(this.level); //new String[0]; //null; //getCategories();
        } else {
            return (String[]) result.toArray(new String[0]);
        }

    }















    public static int CORRECT = 0;
    public static int PROBONLY = 1;
    public static int ADJUSTED = 2;

    String[] classifyLocal(File dbDir, String dbName, double ts, double tc, int method) {

        // If the database has been pushed into a leaf node then it should be classified here
        if (c == null) return getCategories();

        TreeSet result = new TreeSet();

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("cache" + File.separator + c.getFilename() + ".cache")));
            String line;

            while ((line = br.readLine()) != null) {

                if (line.indexOf(dbName) == -1) {
                    continue;
                }

                StringTokenizer st = new StringTokenizer(line, "#");
                //String clName = 
                st.nextToken();
                //String db = 
                st.nextToken();
                File dbContents = new File(dbDir + File.separator + dbName + ".contents");

                String categoryHits = st.nextToken();

                st = new StringTokenizer(categoryHits, ":");

                int n = c.getNumberOfCategories();
                double[] estCoverage = new double[n];
                double[] adjCoverage = new double[n];
                double[] corrCoverage = new double[n];

                while (st.hasMoreTokens()) {

                    String token = st.nextToken();
                    String category = token.substring(0, token.indexOf(","));
                    double hits = Double.parseDouble(token.substring(token.indexOf(",") + 1, token.length()));
                    int k = c.resolveCategory(category);

                    estCoverage[k] = hits;

                    BufferedReader bc = new BufferedReader(new FileReader(dbContents));
                    String inline;

                    while ((inline = bc.readLine()) != null) {
                        if (inline.startsWith(category) && inline.charAt(category.length()) == ':') {
                            hits = Double.parseDouble(inline.substring(category.length() + 1, inline.length()));
                        }
                    }
                    bc.close();
                    corrCoverage[k] = hits;

                }

                adjCoverage = VisualNumerics.math.DoubleMatrix.multiply(c.getInverseConfusionMatrix(), estCoverage);

                double[] corSpec;
                double[] estSpec;
                double[] adjSpec;
                double corSum = 0, estSum = 0;

                for (int g = 0; g < corrCoverage.length; g++) {
                    corSum += corrCoverage[g];
                    estSum += estCoverage[g];
                }

                if (corSum == 0) {
                    corSpec = corrCoverage;
                } else {
                    corSpec = getSpecVector(corrCoverage);
                }

                if (estSum == 0) {
                    estSpec = estCoverage;
                    adjSpec = estCoverage;
                } else {
                    estSpec = getSpecVector(estCoverage);
                    adjSpec = getSpecVector(adjCoverage);
                }

                for (int i = 0; i < corSpec.length; i++) {
                    if (method == CORRECT && corSpec[i] >= ts && corrCoverage[i] >= tc) {
                        String[] s = ((Node) children.get(i)).classifyLocal(dbDir, dbName, ts/corSpec[i], tc, method);

                        if (s != null) {
                            for (int j = 0; j < s.length; j++) {
                                result.add(s[j]);
                            }
                        }
                    } else if (method == PROBONLY && estSpec[i] >= ts && estCoverage[i] >= tc) {
                        String[] s = ((Node) children.get(i)).classifyLocal(dbDir, dbName, ts/estSpec[i], tc, method);

                        if (s != null) {
                            for (int j = 0; j < s.length; j++) {
                                result.add(s[j]);
                            }
                        }
                    } else if (method == ADJUSTED && adjSpec[i] >= ts && adjCoverage[i] >= tc) {
                        String[] s = ((Node) children.get(i)).classifyLocal(dbDir, dbName, ts/adjSpec[i], tc, method);

                        if (s != null) {
                            for (int j = 0; j < s.length; j++) {
                                result.add(s[j]);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result.size() == 0) {
            return getCategories(this.level); //new String[0]; //null; //getCategories();
        } else {
            return (String[]) result.toArray(new String[0]);
        }

    }

    int getCostLocal(File dbDir, String dbName, double ts, double tc, int method) {

        // If the database has been pushed into a leaf node then it should be classified here
        if (c == null) return 0;

        int result = c.getNumberOfRules();

        //TreeSet result = new TreeSet();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("cache" + File.separator + c.getFilename() + ".cache")));
            String line;

            while ((line = br.readLine()) != null) {

                if (line.indexOf(dbName) == -1) {
                    continue;
                }

                StringTokenizer st = new StringTokenizer(line, "#");
                //String clName = 
                st.nextToken();
                //String db = 
                st.nextToken();
                File dbContents = new File(dbDir + File.separator + dbName + ".contents");

                String categoryHits = st.nextToken();

                st = new StringTokenizer(categoryHits, ":");

                int n = c.getNumberOfCategories();
                double[] estCoverage = new double[n];
                double[] adjCoverage = new double[n];
                double[] corrCoverage = new double[n];

                while (st.hasMoreTokens()) {

                    String token = st.nextToken();
                    String category = token.substring(0, token.indexOf(","));
                    double hits = Double.parseDouble(token.substring(token.indexOf(",") + 1, token.length()));
                    int k = c.resolveCategory(category);

                    estCoverage[k] = hits;

                    BufferedReader bc = new BufferedReader(new FileReader(dbContents));
                    String inline;

                    while ((inline = bc.readLine()) != null) {
                        if (inline.startsWith(category) && inline.charAt(category.length()) == ':') {
                            hits = Double.parseDouble(inline.substring(category.length() + 1, inline.length()));
                        }
                    }
                    bc.close();
                    corrCoverage[k] = hits;

                }

                adjCoverage = VisualNumerics.math.DoubleMatrix.multiply(c.getInverseConfusionMatrix(), estCoverage);

                double[] corSpec;
                double[] estSpec;
                double[] adjSpec;
                double corSum = 0, estSum = 0;

                for (int g = 0; g < corrCoverage.length; g++) {
                    corSum += corrCoverage[g];
                    estSum += estCoverage[g];
                }

                if (corSum == 0) {
                    corSpec = corrCoverage;
                } else {
                    corSpec = getSpecVector(corrCoverage);
                }

                if (estSum == 0) {
                    estSpec = estCoverage;
                    adjSpec = estCoverage;
                } else {
                    estSpec = getSpecVector(estCoverage);
                    adjSpec = getSpecVector(adjCoverage);
                }

                for (int i = 0; i < corSpec.length; i++) {
                    if (method == CORRECT && corSpec[i] >= ts && corrCoverage[i] >= tc) {
                        result += ((Node) children.get(i)).getCostLocal(dbDir, dbName, ts/corSpec[i], tc, method);
                    } else if (method == PROBONLY && estSpec[i] >= ts && estCoverage[i] >= tc) {
                        result += ((Node) children.get(i)).getCostLocal(dbDir, dbName, ts/corSpec[i], tc, method);
                    } else if (method == ADJUSTED && adjSpec[i] >= ts && adjCoverage[i] >= tc) {
                        result += ((Node) children.get(i)).getCostLocal(dbDir, dbName, ts/corSpec[i], tc, method);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    String[] classifyWebFromProfile(DBProfile db, double ts) {

        if (c == null) return getCategories();

        Vector result = new Vector();

        double[] estSpec;
        double[] estCoverage = db.getCoverageVector(c);
        double estSum = 0;

        for (int g = 0; g < estCoverage.length; g++) {
            estSum += estCoverage[g];
        }

        if (estSum == 0) {
            estSpec = estCoverage;
        } else {
            estSpec = getSpecVector(estCoverage);
        }

        for (int i = 0; i < estSpec.length; i++) {
            if (estSpec[i] >= ts) {
                String[] s = ((Node) children.get(i)).classifyWebFromProfile(db, ts);

                if (s != null) {
                    for (int j = 0; j < s.length; j++) {
                        result.add(s[j]);
                    }
                }
            }
        }

        if (result.size() == 0) {
            result.add(name);
        }
        return (String[]) result.toArray(new String[0]);

    }

    String[] classifyWeb(Wrapper wrapper, double ts, double tc, int method) {

        if (c == null) return getCategories();

        String dbName = wrapper.toString();

        TreeSet result = new TreeSet();

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("cache" + File.separator + c.getFilename() + ".webcache")));
            String line;

            while ((line = br.readLine()) != null) {

                if (line.indexOf(dbName) == -1) {
                    continue;
                }

                if (line.charAt(line.indexOf(dbName) - 1) != ':' || line.charAt(line.indexOf(dbName) + dbName.length()) != ':') {
                    continue;
                }

                StringTokenizer st = new StringTokenizer(line, "#");
                //String clName = 
                st.nextToken();
                //String wrapperName = 
                st.nextToken();
                String categoryHits = st.nextToken();

                st = new StringTokenizer(categoryHits, ":");

                int n = c.getNumberOfCategories();
                double[] estCoverage = new double[n];
                double[] adjCoverage = new double[n];

                while (st.hasMoreTokens()) {

                    String token = st.nextToken();
                    String category = token.substring(0, token.indexOf(","));
                    double hits = Double.parseDouble(token.substring(token.indexOf(",") + 1, token.length()));
                    int k = c.resolveCategory(category);

                    estCoverage[k] = hits;
                }

                adjCoverage = VisualNumerics.math.DoubleMatrix.multiply(c.getInverseConfusionMatrix(), estCoverage);

                double[] estSpec;
                double[] adjSpec;
                double estSum = 0;

                for (int g = 0; g < n; g++) {
                    estSum += estCoverage[g];
                }

                if (estSum == 0) {
                    estSpec = estCoverage;
                    adjSpec = estCoverage;
                } else {
                    estSpec = getSpecVector(estCoverage);
                    adjSpec = getSpecVector(adjCoverage);
                }

                for (int i = 0; i < estSpec.length; i++) {
                    if (method == CORRECT) {
                        String[] s = null;//wrapper.getSubCategories(); PANOs//

                        if (s != null) {
                            for (int j = 0; j < s.length; j++) {
                                result.add(s[j]);
                            }
                        }
                        break;
                    } else if (method == PROBONLY && estSpec[i] >= ts && estCoverage[i] >= tc) {
                        String[] s = ((Node) children.get(i)).classifyWeb(wrapper, ts, tc, method);

                        if (s != null) {
                            for (int j = 0; j < s.length; j++) {
                                result.add(s[j]);
                            }
                        }
                    } else if (method == ADJUSTED && adjSpec[i] >= ts && adjCoverage[i] >= tc) {
                        String[] s = ((Node) children.get(i)).classifyWeb(wrapper, ts, tc, method);

                        if (s != null) {
                            for (int j = 0; j < s.length; j++) {
                                result.add(s[j]);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (result.size() == 0) {
            return getCategories();
        } else {
            return (String[]) result.toArray(new String[0]);
        }

    }

    int getCostWeb(Wrapper wrapper, double ts, double tc, int method) {

        if (c == null) return 0;

        String dbName = wrapper.toString();

        int result = c.getNumberOfRules();

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("cache" + File.separator + c.getFilename() + ".webcache")));
            String line;

            while ((line = br.readLine()) != null) {

                if (line.indexOf(dbName) == -1) {
                    continue;
                }

                if (line.charAt(line.indexOf(dbName) - 1) != ':' || line.charAt(line.indexOf(dbName) + dbName.length()) != ':') {
                    continue;
                }

                StringTokenizer st = new StringTokenizer(line, "#");
                //String clName = 
                st.nextToken();
                //String wrapperName = 
                st.nextToken();
                String categoryHits =    st.nextToken();

                st = new StringTokenizer(categoryHits, ":");

                int n = c.getNumberOfCategories();
                double[] estCoverage = new double[n];
                double[] adjCoverage = new double[n];

                while (st.hasMoreTokens()) {

                    String token = st.nextToken();
                    String category = token.substring(0, token.indexOf(","));
                    double hits = Double.parseDouble(token.substring(token.indexOf(",") + 1, token.length()));
                    int k = c.resolveCategory(category);

                    estCoverage[k] = hits;
                }

                adjCoverage = VisualNumerics.math.DoubleMatrix.multiply(c.getInverseConfusionMatrix(), estCoverage);

                double[] estSpec;
                double[] adjSpec;
                double  estSum = 0;

                for (int g = 0; g < n; g++) {
                    estSum += estCoverage[g];
                }

                if (estSum == 0) {
                    estSpec = estCoverage;
                    adjSpec = estCoverage;
                } else {
                    estSpec = getSpecVector(estCoverage);
                    adjSpec = getSpecVector(adjCoverage);
                }

                for (int i = 0; i < estSpec.length; i++) {
                    if (method == CORRECT) {
                        return result;

                    } else if (method == PROBONLY && estSpec[i] >= ts && estCoverage[i] >= tc) {
                        result += ((Node) children.get(i)).getCostWeb(wrapper, ts/estSpec[i], tc, method);

                    } else if (method == ADJUSTED && adjSpec[i] >= ts && adjCoverage[i] >= tc) {
                        result += ((Node) children.get(i)).getCostWeb(wrapper, ts/estSpec[i], tc, method);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
    
    public Vector getRules()
    {
		if (c != null && c.getNumberOfRules() > 0)
		{
			return c.getRules();
		}
    	else
    	{
			return new Vector();
    	}
    			
    }
}
