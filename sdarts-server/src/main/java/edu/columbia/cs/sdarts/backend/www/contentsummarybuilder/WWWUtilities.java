package edu.columbia.cs.sdarts.backend.www.contentsummarybuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;

import org.htmlparser.Node;
import org.htmlparser.Parser;



/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class WWWUtilities {

	public static void writeFile(String in, File out) {
		try {
			if (!(new File(out.getParent()).exists())) {
				(new File(out.getParent())).mkdirs();
			}
			BufferedWriter bw = new BufferedWriter(new FileWriter(out));
			bw.write(in);
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String runLynx(File f) {
		int bufferSize = 40000;

		StringBuffer buffer = new StringBuffer(bufferSize);

		try {
			String cmdline[] = {"/usr/bin/lynx", "--dump", f.getPath() };
			Process p = Runtime.getRuntime().exec(cmdline);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			char[] cbuf = new char[1];

			while (stdInput.read(cbuf, 0, 1) != -1 || stdError.read(cbuf, 0, 1) != -1) {
				buffer.append(cbuf);
			}
			p.waitFor();
			stdInput.close();
			stdError.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		// Remove the References at the end of the dump
		int end = buffer.indexOf("\nReferences\n");

		if (end == -1) {
			end = buffer.length();
		}
		// Remove everything inside [   ] and do not write more than two consecutive spaces
		boolean recording = true;
		boolean wrotespace = false;
		StringBuffer output = new StringBuffer(end);

		for (int i = 0; i < end; i++) {
			if (recording) {
				if (buffer.charAt(i) == '[') {
					recording = false;
					if (!wrotespace) {
						output.append(' ');
						wrotespace = true;
					}
					continue;
				} else {
					char c = buffer.charAt(i);
					if (Character.isLetter(c) && c<128) {
						output.append(Character.toLowerCase(c));
						wrotespace = false;
					} else {
						if (!wrotespace) {
							output.append(' ');
							wrotespace = true;
						}
					}
				}
			} else {
				if (buffer.charAt(i) == ']') {
					recording = true;
					continue;
				}
			}
		}

		return output.toString();

	}




	public static int getNumberofCollections(String indexesDirectory) {

		// We go to the directory that contains the Lucene indexes and
		// add the appropriate directories
		Vector col = new Vector();
		Vector names = new Vector();
		File dir = new File(indexesDirectory);
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++) {

			if (!files[i].isDirectory())
				continue;

			String indexFilename = indexesDirectory	+ File.separator + files[i].getName();// + File.separator + "index";
			//System.out.println(indexFilename);
			File ind = new File(indexFilename);

			if (ind.exists() && ind.isDirectory()) {
				col.add(indexFilename);
				names.add(files[i].getName());
			}
		}
		
		return col.size();
	}


	public static HashMap loadHierarchy(String hierarchyFilename) {
		HashMap Classifiers = new HashMap();
		try {
			BufferedReader br =
				new BufferedReader(new FileReader(new File(hierarchyFilename)));
			String linein;
			while ((linein = br.readLine()) != null) {
				int k = linein.indexOf("#");
				String category = linein.substring(0, k);
				String classifier = linein.substring(k + 1, linein.length());
				Classifiers.put(category, classifier);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Classifiers;
	}

	// This function essentially transforms the file dbcategories.txt
	// so that each database is associated with every node in the path
	static public HashMap getDataBasesPerCategory(String filename) {

		HashMap Categs = new HashMap();

		try {
			//
			// We read the file once to read all the categories
			//
			BufferedReader br =
				new BufferedReader(new FileReader(new File(filename)));

			String linein;
			while ((linein = br.readLine()) != null) {
				int k = linein.indexOf("#");
				String category = linein.substring(0, k);
				Categs.put(category, new String());

			}
			br.close();

			// Now we have initialized the categories
			// Now for each database we will scan the HashMap
			// and we will add the datase into each entry 
			// for which its classification (e.g., Root/Health/Diseases)
			// ".startsWith" the key in the hash

			br = new BufferedReader(new FileReader(new File(filename)));

			while ((linein = br.readLine()) != null) {
				int k = linein.indexOf("#");
				String classification = linein.substring(0, k);
				String databases = linein.substring(k + 1, linein.length());
				StringTokenizer st = new StringTokenizer(databases, ",");

				while (st.hasMoreTokens()) {
					String db = st.nextToken();

					TreeSet keys = new TreeSet(Categs.keySet());
					for (Iterator it = keys.iterator(); it.hasNext();) {
						String category = (String) it.next();
						String dbs = (String) Categs.get(category);
						if (classification.startsWith(category)) {
							if (dbs.length() > 0) {
								dbs += "," + db;
							} else {
								dbs = db;
							}
						}
						Categs.put(category, dbs);
					}
				}

			}

			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return Categs;
	}


	// Exactly the same as getDatabasesPerCategory, but here each
	// database goes ONLY to the node where it has been classified
	// and not to the parent nodes
	static public HashMap getDBsPerCategory(String filename) {

		HashMap Categs = new HashMap();

		try {
			//
			// We read the file once to read all the categories
			//
			BufferedReader br =
				new BufferedReader(new FileReader(new File(filename)));

			String linein;
			while ((linein = br.readLine()) != null) {
				int k = linein.indexOf("#");
				String category = linein.substring(0, k);
				Categs.put(category, new String());

			}
			br.close();

			// Now we have initialized the categories
			// Now for each database we will scan the HashMap
			// and we will add the datase into each entry 
			// for which its classification (e.g., Root/Health/Diseases)
			// ".startsWith" the key in the hash

			br = new BufferedReader(new FileReader(new File(filename)));

			while ((linein = br.readLine()) != null) {
				int k = linein.indexOf("#");
				String classification = linein.substring(0, k);
				String databases = linein.substring(k + 1, linein.length());
				StringTokenizer st = new StringTokenizer(databases, ",");

				while (st.hasMoreTokens()) {
					String db = st.nextToken();

					TreeSet keys = new TreeSet(Categs.keySet());
					for (Iterator it = keys.iterator(); it.hasNext();) {
						String category = (String) it.next();
						String dbs = (String) Categs.get(category);
						if (classification.equals(category)) {
							if (dbs.length() > 0) {
								dbs += "," + db;
							} else {
								dbs = db;
							}
						}
						Categs.put(category, dbs);
					}
				}

			}

			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return Categs;
	}











	public static HashMap countDBsPerCategory(HashMap Categories) {

		HashMap DBsPerCategory = new HashMap();

		TreeSet keys = new TreeSet(Categories.keySet());
		for (Iterator it = keys.iterator(); it.hasNext();) {

			String category = (String) it.next();
			String dbs = (String) Categories.get(category);
			StringTokenizer st = new StringTokenizer(dbs, ",");
			Integer c = new Integer(st.countTokens());

			String shortCategoryName = new String();
			st = new StringTokenizer(category, "/");
			while (st.hasMoreTokens()) {
				shortCategoryName = st.nextToken();
			}
			DBsPerCategory.put(shortCategoryName, c);
		}

		return DBsPerCategory;
	}

	static public HashMap loadDBCategories(String filename) {

		HashMap DBCats = new HashMap();

		try {
			//
			// Load the DBcategories.txt file and find the set of categories for each database
			//
			BufferedReader br =
				new BufferedReader(new FileReader(new File(filename)));

			String linein;
			while ((linein = br.readLine()) != null) {
				int k = linein.indexOf("#");
				String category = linein.substring(0, k);
				String databases = linein.substring(k + 1, linein.length());
				StringTokenizer st = new StringTokenizer(databases, ",");

				while (st.hasMoreTokens()) {
					String db = st.nextToken();
					if (DBCats.containsKey(db)) {
						String cat = (String)DBCats.get(db);
						cat = cat +"," + category;
						DBCats.put(db,cat);					
					} else {
						DBCats.put(db, category);
					}
				}

			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return DBCats;
	}

	public static String cleanLine(String line) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c<128 && Character.isLetter(c)) {
				buffer.append(c);
			} else {
				buffer.append(' ');
			}
		}
		return buffer.toString().toLowerCase();
	}



	public static String getPage(String URLName) {
		StringBuffer buffer = new StringBuffer();

		try {
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection connection =
				(HttpURLConnection) new URL(URLName).openConnection();

			BufferedReader dataInput =
				new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			String line;

			while ((line = dataInput.readLine()) != null) {
				buffer.append(cleanLine(line.toLowerCase()));
				buffer.append('\n');
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return buffer.toString();
	}

	public static String getFile(String FileName) {
		StringBuffer buffer = new StringBuffer();

		try {
			BufferedReader dataInput =
				new BufferedReader(new FileReader(new File(FileName)));
			String line;

			while ((line = dataInput.readLine()) != null) {
				buffer.append(cleanLine(line.toLowerCase()));
				buffer.append('\n');
			}
			dataInput.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return buffer.toString();
	}

	public static TreeSet getWords(String TextFile) {
		TreeSet result = new TreeSet();
		StringTokenizer st = new StringTokenizer(TextFile);
		while (st.hasMoreTokens()) {
			result.add(st.nextToken());
		}
		return result;
	}

	
	public static String extractStrings(String url) throws ParserException 
  	{
		Parser parser = new Parser(url);
		parser.addScanner(new org.htmlparser.scanners.ScriptScanner());
		Node node;
		StringBuffer results= new StringBuffer();
		for (NodeIterator i = parser.elements();i.hasMoreNodes();) 
		{
		  node = i.nextNode();
		  if (! (node instanceof org.htmlparser.tags.ScriptTag) )
			results.append(node.toPlainTextString());
		}
		return results.toString();
  	}

}
