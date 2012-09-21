/*
 * Created on Feb 10, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package edu.columbia.cs.sdarts.backend.www.contentsummarybuilder;

import java.io.File;
import java.util.Vector;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.StringTokenizer;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.Reader;
import java.io.IOException;
import java.lang.StringBuffer;
import java.io.StringReader;

import  org.w3c.dom.*;
import org.apache.xerces.parsers.*;

import edu.columbia.cs.sdarts.frontend.SDARTS;
import edu.columbia.cs.sdarts.backend.doc.DocConstants;





import edu.columbia.cs.sdarts.backend.doc.DocConfig;
import edu.columbia.cs.sdarts.backend.www.classifier.Hierarchy;
import edu.columbia.cs.sdarts.backend.www.classifier.Node;
import edu.columbia.cs.sdarts.backend.www.classifier.Rule;

import edu.columbia.cs.sdarts.backend.www.WWWQueryProcessor;
import edu.columbia.cs.sdarts.common.LSPQuery;
import edu.columbia.cs.sdarts.common.LSPResults;
import edu.columbia.cs.sdarts.common.LSPDoc;

import edu.columbia.cs.sdarts.common.LSPSource;
import edu.columbia.cs.sdarts.common.LSPFilter;
import edu.columbia.cs.sdarts.common.LSPTerm;
import edu.columbia.cs.sdarts.common.LSPField;

/**
 * @author Yan
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class WWWContentSummaryBuilder {
	private DocConfig config;
	private Hierarchy hierarchy;
	private LSPQuery query;
	private WWWQueryProcessor queryProcessor;
	
	private WWWContentSummary cs;
	private TreeSet documents;
	
	private int costQueries;
	private int costDocuments;
	
	// We keep track of how A, and B in the Mandelbrot's formula are evolving to
	// be able to estimate their values for the whole collection
	private Vector docs;
	private Vector A;
	private Vector B;

//	private BufferedWriter bw;
		
	public Vector classifiers;
	
	private long passNo;
	
	public WWWContentSummaryBuilder(DocConfig cnfg) throws Exception 
	{
		config = cnfg;
		
		passNo = 1;
		
		try{
			hierarchy = new Hierarchy(new File(config.getClassificationSchemaPath()));		
			System.out.println("Hierarchy Created");
		}
		catch( Exception e)
		{
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		 
	}
	
	public String CreateContentSumary(WWWQueryProcessor queryProcessor) throws Exception
	{
		this.queryProcessor = queryProcessor;
		
		LSPQuery query = CreateQuery();
		
		classifiers = new Vector();	
		
		cs = new WWWContentSummary();
		
		costQueries = 0;
		
		costDocuments = 0;
		
		documents = new TreeSet();
		
		this.A = new Vector();
		this.B = new Vector();
		this.docs = new Vector();
		
		if (config.getCacheLocation().length() > 0)
			CreateDirectory(config.getCacheLocation());
		
		CheckClassificationNode("Root");
		
		System.out.print("Database " + config.getBackEndLSPName() + " was classified under the following node(s) : ");
		
		String ClassifiedAs = "";
		
		for (int i = 0; i < classifiers.size(); i++)
		{
			if (i > 0)
				ClassifiedAs = ClassifiedAs + ",";
			ClassifiedAs = ClassifiedAs + GetNodePath(classifiers.get(i).toString()) + classifiers.get(i).toString();			
		}
		
		System.out.println(ClassifiedAs);
		
		cs.DetectCommonWords();
		
		cs.ClearResults();
		
		passNo ++;
		
		CheckClassificationNode("Root");
		
		cs.PrintCoefficients();
		
		double EstimatedDBSize = EstimateDatabaseSize(10, 0.1, 0.9);
		
		System.out.println("Estimated database size = " + EstimatedDBSize);
		
		cs.WriteContentSummary(SDARTS.CONFIG_DIRECTORY + File.separator + config.getBackEndLSPName() + File.separator + DocConstants.CONTENT_SUMMARY_FILENAME, EstimatedDBSize);
		
		return ClassifiedAs;
	}
	
	
	private LSPQuery CreateQuery()
	{
		query = new LSPQuery("STARTS 1.0", false, "basic-1", 0.0, (int)config.getMaxDocumentPerQuery());
		
		LSPSource[] sources = new LSPSource[1];
		sources[0] = new LSPSource(config.getBackEndLSPName());
		query.setSources(sources);

		LSPField[] answer_fields = new LSPField[3];

		answer_fields[0] = new LSPField("basic1", "author");
		answer_fields[1] = new LSPField("basic1", "title");
		answer_fields[2] = new LSPField("basic1", "linkage");

		query.setAnswerFields(answer_fields);
		
		return query;
	}
	
	private void SetSearchString(String searchString)
	{
		LSPField field = new LSPField("basic1", "body-of-text");
		
		//We put AND between words
		LSPTerm term = new LSPTerm(field, null, searchString.replaceAll(" ", " AND "));
		LSPFilter filter = new LSPFilter(term);
		query.setFilter(filter);
	}
	
	private void CheckClassificationNode(String nodeName) throws Exception
	{
		System.out.println("Performing discovery of " + nodeName + " node");
		
		HashMap hitList = new HashMap();
		
		Node node = hierarchy.getNode(nodeName);
		
		String nodePath = GetNodePath(nodeName);
		
		Vector rules = node.getRules();
		
		long totalHits = 0;
		
		Runtime.getRuntime().gc();
		
		if (rules == null || rules.size() == 0)
		{
			classifiers.add(nodeName);
			return;
		}
		
		for (int i = 0; i < rules.size(); i++)
		{
			System.out.print("->");
			Rule currRule = (Rule)rules.get(i);
			System.out.print(nodePath + nodeName + "." + currRule.getCategory() + " = " + currRule.getString());
			
			try{
				String information = "";
				String Path = config.getCacheLocation() + "/" + currRule.getString() + "/";
				if (config.getCacheLocation().length() > 0)
				{
					try{
						information = ReadFromFile(Path + "info.result");
					}
					catch(Exception e){}
				}
				
				long numberOfHits = 0;
				
				if (information.length() == 0)
				{
					SetSearchString(currRule.getString());
			
					LSPResults results = queryProcessor.query(query);
				
					String path = config.getCacheLocation() + "/" + currRule.getString();
				
					CreateDirectory(path);
				
					path += "/";
				
					WriteToFile(path + "info.result", "<QueryInfo hits=\"" + results.getNumAvailable() + "\"/>");
				
					AddQueryResults(path, results);
					
					numberOfHits = results.getNumAvailable();
				}
				else
				{
					DOMParser parser = new DOMParser();
				  	Reader sReader = new StringReader(information);
   					org.xml.sax.InputSource source = new org.xml.sax.InputSource(sReader);
   					parser.parse(source);
   					Document doc = parser.getDocument();
					doc.normalize();
					Element element = doc.getDocumentElement();
									
					numberOfHits = Long.parseLong(element.getAttribute("hits"));
					
					AddCachedQueryResults(Path);
					
					System.out.print(" (cached) ");
					
				}
				
				System.out.println(" - " + numberOfHits + " document(s) available");
				
				totalHits += numberOfHits;
				
				if (hitList.containsKey(currRule.getCategory()))
				{
					Long l = new Long(hitList.get(currRule.getCategory()).toString());
					hitList.put(currRule.getCategory(), Long.toString(l.intValue() + numberOfHits));
				}
				
				else
					hitList.put(currRule.getCategory(), Long.toString(numberOfHits));
					
			}
			catch(Exception e)
			{
				throw(new Exception(e.getMessage()));
			}
			
			if (passNo == 2)
			cs.GetIntermediateResults();
		}
		
		boolean haveHits = false;
		
		Set categorySet = hitList.keySet();
	
		Iterator it = categorySet.iterator();
	
		while (it.hasNext())
		{
			String category = it.next().toString();
			Long l = new Long(hitList.get(category).toString());
			double specificity = l.doubleValue() / (double) totalHits;
			
			System.out.println("Specificity for " + category + " is " + specificity);
	
			if (specificity >= config.getSpecificityThreshold())
			{
				CheckClassificationNode(category);
				haveHits = true;
			}
				
		}
		
		if (!haveHits)
			classifiers.add(nodeName);
	}
	
	private String GetNodePath(String nodeName)
	{
		String name = new String();
		
		Node node = hierarchy.getNode(nodeName);
		
		for (int i = 0; i < node.getLevel(); i++)
		{
			name += node.getParentAtLevel(i) + ".";
		}
			
		return name;
	}
	
	private void AddQueryResults(String path, LSPResults results) throws Exception 
	{
		costQueries++;
		
		if (results.getNumDocs() == 0)
			return;
			
		LSPDoc[] LSPdocs = results.getDocs();
		
		for (int i = 0; i < results.getNumDocs(); i++)
		{
			String url = LSPdocs[i].getValue("linkage");
			
			if (url.startsWith("<![CDATA["))
				url = url.substring(9, url.length() - 3);
			
			if (documents.contains(url))
				continue;

			documents.add(url);
						
			this.costDocuments++;
			
			cs.increaseSampleSize();
			
//			get the contents of the retrieved document
			String r = new String();
			
			r = WWWUtilities.getPage(url);
			//r = WWWUtilities.extractStrings(url);
			
			WriteToFile(path + i + ".result", r);
			
			ProcessDocument(r);
		}
		
	}

	private void WriteToFile(String filename, String content) throws IOException
	{
		if (config.getCacheLocation().length() == 0)
			return;
			
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		bw.write(content);
		bw.close();		
	}
	
	private void CreateDirectory(String dirname) throws IOException
	{
		if (config.getCacheLocation().length() == 0)
			return;
			
		(new File(dirname)).mkdir();				
	}
	
	private String ReadFromFile(String filename) throws IOException
	{
		if (config.getCacheLocation().length() == 0)
			return "";
			
		StringBuffer sb = new StringBuffer("");
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while((line = br.readLine()) != null)
			sb.append(line);
			
		br.close();
		
		return sb.toString();		
	}
	
	private void AddCachedQueryResults(String path)
	{
		try{
			for (int i = 0; i < config.getMaxDocumentPerQuery(); i++)
			{
				String r = ReadFromFile(path + i + ".result");
				
				this.costDocuments++;
			
				cs.increaseSampleSize();

				ProcessDocument(r);

			}
		}
		catch(Exception e){ 
			String s = e.getMessage();}
	}
	
	private double EstimateDatabaseSize(int NumQrys, double lowBorder, double highBorder) throws Exception
	{
		double lowBound = cs.getSampleSize() * lowBorder;
		double highBound = cs.getSampleSize() * highBorder;
		
		double size = 0;
		
		TreeSet Queries = new TreeSet();
		
		int cnt = 0;
		
		while(cnt < NumQrys)
		{
			String word;
			
			do{
				word = cs.GetRandomWord(lowBound, highBound);
			}while(Queries.contains(word));
			
			Queries.add(word);
			
			SetSearchString(word);
			
			LSPResults results;
			try{
				results = queryProcessor.query(query);
			}
			catch(Exception e){
				throw new Exception(e.getMessage());
			}
			
			if (results.getNumAvailable() == 0)
				continue;
			
			System.out.println("Query >" + word + "< " + results.getNumAvailable() + " hits. Sample Frequency = " + cs.getSampleFrequency(word)); 
			
			size += (double)results.getNumAvailable() * (double)cs.getSampleSize() / (double)cs.getSampleFrequency(word);
			
			cnt ++;
		}
		
		return size / (double)NumQrys;
	}
	
	private void ProcessDocument(String doc)
	{
//		get the individual words
		doc.toLowerCase();
		
		TreeSet wordset = WWWUtilities.getWords(doc);// .getWordsStemmed(r);
	 	for (Iterator iter = wordset.iterator(); iter.hasNext();) {
			 String w = (String)iter.next();	
			 if (!cs.contains(w)) {
				 cs.addTerm(w, (passNo == 2));
			 }
			 else
				cs.increaseSampleFrequency(w);

		 }
		 
		 if (passNo != 2)
		 	return;
		 //now we calculate term frequencies
		StringTokenizer st = new StringTokenizer(doc);
		while (st.hasMoreTokens()) {
			cs.increaseSampleTermFrequency(st.nextToken());
		}

	}
	
}
