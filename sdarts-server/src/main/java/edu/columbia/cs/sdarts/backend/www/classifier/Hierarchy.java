package edu.columbia.cs.sdarts.backend.www.classifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



public class Hierarchy {

	Node Root;
	Node currNode;
	
	HandlerBase hb;
	
	
	private class HierarchyHandler extends HandlerBase {
		
//		private Node currNode = null;

		
		public HierarchyHandler() {
		}

		public void characters(char characters[], int start, int length) {
			String s = new String(characters, start, length);
			
			
			
		}

		public void startElement(String name, AttributeList attrs) throws SAXException {
			
			try{
			
				if (name == "query") 
				{
					Rule rule = new Rule(attrs.getValue("category"), attrs.getValue("text"));
					currNode.AddRule(rule);
					return;	
				}
				
				
				if (name == "node")
				{
					if (Root == null)
					{	
						Root = new Node(null, attrs.getValue("name"));
						currNode = Root;
					}
					else
					{
						Node newNode = new Node(currNode, attrs.getValue("name"));
						currNode.children.add(newNode);
						currNode = newNode;
					}
						
				}
			}
			catch(Exception e)
			{
				throw new SAXException(e.getMessage());
			}
		}

		public void endElement(String name) {
			if (name == "node")
				currNode = currNode.parent;
		}

		public Node parse(BufferedReader reader) throws Exception {

			org.apache.xerces.parsers.SAXParser p = new org.apache.xerces.parsers.SAXParser();
			//p.setFeature("http://xml.org/sax/features/validation", true); 
			//ParserFactory.makeParser ("com.microstar.xml.SAXDriver");
			p.setDocumentHandler(this);
			p.parse(new InputSource(reader));
			return Root;

		}
	}
/*
	public Hierarchy (File nodes) throws NoSuchFieldException  {

		boolean found=false;

		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(nodes));

			while ( (line = br.readLine())!=null  ) {

				// Ignore comment lines
				if (line.startsWith("%")) continue;

				// Ignore spaces or tabs
				while (line.startsWith(" ") || line.startsWith("\t")) {
						line=line.substring(1,line.length());
				}

				if (line.startsWith("Root") && line.charAt("Root".length())=='#') {
				//To avoid e.g the category 'Rootanic' to match the category 'Root'

					//line format is category#ClassifierURL

					// We get the url (one char after the category, until the end of the line)
					int k=line.indexOf("#");

					String clURL = line.substring(k+1,line.length()); // The classifier's URL for the child

					if (clURL.equals("NONE")) { //Then the child is a leaf node
						Root = new Node(null, "Root", null, nodes);
					} else if (clURL.startsWith("http")) {
						URL url = new URL(clURL);
						Classifier nodeClassifier = new Classifier(url);
						Root = new Node(null, "Root", nodeClassifier, nodes);
					} else {
						String FullPath = nodes.getParent() + '\\'+ clURL.replace('/', '\\');
						Classifier nodeClassifier = new Classifier(new File(FullPath));
						Root = new Node(null, "Root", nodeClassifier, nodes);
					}

					found = true;

				}
			}
		} catch (Exception e) { e.printStackTrace(); }
		if (found==false) throw new NoSuchFieldException("There is no definition for Root node!\n");

	}
*/
	public Hierarchy (File nodes) throws NoSuchFieldException  {
		HierarchyHandler h = new HierarchyHandler();
		try
		{
			Root = h.parse(new BufferedReader(new FileReader(nodes)));
		}
		catch(Exception e)
		{
			throw new NoSuchFieldException(e.getMessage());
		}
	}
	
	public String[] getCategories(String nodeName) {
		return Root.getCategories(nodeName);
	}

	public String[] getCategories(int levelLimit) {
		return Root.getCategories(levelLimit);
	}

	public String[] getCategories() {
		return Root.getCategories();
	}

	public String[] getParents(String nodeName) {
		return Root.getParents(nodeName);
	}

	public Node getNode(String nodeName) {
		return Root.getNode(nodeName);
	}

	public static int CORRECT = 0;
	public static int PROBONLY = 1;
	public static int ADJUSTED = 2;
	public String[] classifyLocal(File dbDir, String dbName, double ts, double tc, int method) {
		return Root.classifyLocal(dbDir, dbName, ts, tc, method);
	}

	public int getCostLocal(File dbDir, String dbName, double ts, double tc, int method) {
		return Root.getCostLocal(dbDir, dbName, ts, tc, method);
	}

	public String[] classifyWeb(Wrapper wrapper, double ts, double tc, int method) {
		return Root.classifyWeb(wrapper, ts, tc, method);
	}

	public int getCostWeb(Wrapper wrapper, double ts, double tc, int method) {
		return Root.getCostWeb(wrapper, ts, tc, method);
	}

	public String[] classifyWebFromProfile(DBProfile db, double ts) {
		return Root.classifyWebFromProfile( db, ts);
	}
}
