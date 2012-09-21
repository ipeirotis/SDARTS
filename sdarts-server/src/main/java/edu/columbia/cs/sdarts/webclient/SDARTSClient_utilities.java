/*
 *SDARTSClient_utilities.java
 *Author: Sergey Sigelman (ss1792@cs.columbia.edu)
 *For: SDARTS Web Client project - Columbia University CS Dept.
 *Professor: Luis Gravano (gravano@cs.columbia.edu)
 */

package edu.columbia.cs.sdarts.webclient;

import gnu.regexp.RE;
import gnu.regexp.REMatch;

import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import org.xml.sax.AttributeList;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SDARTSClient_utilities {
	private class CIHandler extends HandlerBase {
		StringBuffer buffer;
		private Vector values;

		public CIHandler() {
		}

		public void characters(char characters[], int start, int length) {
			String s = new String(characters, start, length);
			if (buffer == null)
				buffer = new StringBuffer(s);
			else
				buffer.append(s);
		}

		public void startElement(String name, AttributeList attrs) throws SAXException {
			String namespace = name.substring(0, name.indexOf(':') + 1);
			name = removeNamespace(name);
			if (name.equals("subcolName") || name.equals("subcolDesc"))
				buffer = new StringBuffer();
		}

		public void endElement(String name) {
			name = removeNamespace(name);
			if (name.equals("subcolName") || name.equals("subcolDesc")) {
				String value = buffer.toString();

				value = value.replaceAll("\n", "");
				value = value.replaceAll("\t", "");

				value.trim();

				values.add(value);
			}
		}

		public String[] parse(Reader reader) throws Exception {
			values = new Vector();

			org.apache.xerces.parsers.SAXParser p = new org.apache.xerces.parsers.SAXParser();
			//p.setFeature("http://xml.org/sax/features/validation", true); 
			//ParserFactory.makeParser ("com.microstar.xml.SAXDriver");
			p.setDocumentHandler(this);
			p.parse(new InputSource(reader));

			int listSize = values.size();
			String[] results = new String[listSize];
			for (int i = 0; i < listSize; i++)
				results[i] = (String) values.get(i);
			return results;
		}
	}

	private class MDHandler extends HandlerBase {
		private static final int ABSTRACT = 1;
		private static final int ALGORITHM = 5;
		private static final int CONTENT_SUMMARY_LINKAGE = 8;
		private static final int DATE = 3;
		private static final int FIELDS = 6;
		private static final int LANGUAGE = 2;
		private static final int LINKAGE = 7;
		private static final int NONE = 0;
		private static final int STOPWORDS = 4;
		private static final int CLASSIFICATION = 9;
		private HashMap map;
		private String pcdata;
		private String serverURL;
		private int state = NONE;
		private Vector values;

		public MDHandler(String serverURL) {
			super();
			this.serverURL = serverURL;
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			if (state != NONE) {
				if (pcdata == null)
					pcdata = new String(ch, start, length).trim();
				else
					pcdata += new String(ch, start, length).trim();
			}
		}

		public void endElement(String name) {
			try {
				switch (state) {
					case ABSTRACT :
						map.put("abstract", pcdata);
						state = NONE;
						break;
					case LANGUAGE :
						map.put("source-language", pcdata);
						state = NONE;
						break;
					case DATE :
						map.put("date-changed", pcdata);
						state = NONE;
						break;
					case STOPWORDS :
						if (removeNamespace(name).equals("stop-word-list")) {
							map.put("stop-word-list", values);
							state = NONE;
							break;
						} else
							values.add(pcdata);
						break;
					case ALGORITHM :
						map.put("ranking-algorithm-id", pcdata);
						state = NONE;
						break;
					case FIELDS :
						if (removeNamespace(name).equals("fields-supported")) {
							map.put("fields-supported", values);
							state = NONE;
						}
						break;
					case LINKAGE :
						map.put("linkage", fixLinkage(new URL(pcdata), serverURL));
						state = NONE;
						break;
					case CONTENT_SUMMARY_LINKAGE :
						if (pcdata.equals("none"))
							map.put("content-summary-linkage", "none");
						else
							map.put("content-summary-linkage", fixLinkage(new URL(pcdata), serverURL));
						state = NONE;
						break;
					case CLASSIFICATION:
						map.put("classification", pcdata);
						state = NONE;
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			pcdata = null;
		}

		public HashMap parse(Reader reader) throws Exception {
			map = new HashMap();
			//org.apache.xml.SAXParser p = new org.apache.xerces.parsers.SAXParser();
			org.apache.xerces.parsers.SAXParser p = new org.apache.xerces.parsers.SAXParser();
			//p.setFeature("http://xml.org/sax/features/validation", true); 
			//ParserFactory.makeParser ("com.microstar.xml.SAXDriver");
			p.setDocumentHandler(this);
			p.parse(new InputSource(reader));
			//p.setDocumentHandler(this);
			//p.parse(new InputSource(reader));
			return map;
		}

		public void startElement(String name, AttributeList attrs) throws SAXException {
			String namespace = name.substring(0, name.indexOf(':') + 1);
			name = removeNamespace(name);
			if (name.equals("abstract"))
				state = ABSTRACT;
			else if (name.equals("source-language"))
				state = LANGUAGE;
			else if (name.equals("date-changed"))
				state = DATE;

			else if (name.equals("stop-word-list"))
				values = new Vector();

			else if (name.equals("word"))
				state = STOPWORDS;
			else if (name.equals("ranking-algorithm-id"))
				state = ALGORITHM;

			else if (name.equals("query-parts-supported"))
				map.put("query-parts-supported", attrs.getValue("parts"));
			else if (name.equals("fields-supported")) {
				values = new Vector();
				state = FIELDS;
			} else if (name.equals("field") && state == FIELDS)
				values.add(attrs.getValue("name"));
			else if (name.equals("linkage"))
				state = LINKAGE;
			else if (name.equals("content-summary-linkage"))
				state = CONTENT_SUMMARY_LINKAGE;
			else if (name.equals("classification"))
				state = CLASSIFICATION;
			else
				state = NONE;
		}

	}

	public Vector computeIntersection(Vector input) {
		// takes a Vector of Vectors of Objects and returns a Vector of
		// strings present in ALL input vectors (intersection)

		HashSet result = new HashSet();
		Iterator inputIt = input.iterator();
		while (inputIt.hasNext())
			result.addAll((Vector) inputIt.next());

		Iterator resultIt = result.iterator();
		Object ob;

		while (resultIt.hasNext()) {
			ob = resultIt.next();
			inputIt = input.iterator();
			while (inputIt.hasNext()) {
				if (!((Vector) inputIt.next()).contains(ob)) {
					resultIt.remove();
					break;
				}
			}
		}

		return new Vector(result);
	}

	private String createANDFieldFilter(String fieldName, String value) {
		// parse the string into individula phrases
		Enumeration phrases = getANDTerms(value);
		boolean madeOneFilter = false;
		String filter = "";
		while (phrases.hasMoreElements()) {
			if (madeOneFilter) {
				filter =
					"     <starts:filter>\n"
						+ filter
						+ "     <starts:boolean-op name='and'/>\n"
						+ createSimpleFilter(fieldName, (String) phrases.nextElement())
						+ "     </starts:filter>\n";
			} else {
				filter = createSimpleFilter(fieldName, (String) phrases.nextElement());
				madeOneFilter = true;
			}
		}

		return filter;
	}

	private String createFieldFilter(String fieldName, String value) {
		// parse the string into individula phrases
		Enumeration phrases = getORTerms(value);
		boolean madeOneFilter = false;
		String filter = "";
		while (phrases.hasMoreElements()) {
			if (madeOneFilter) {
				filter =
					"     <starts:filter>\n"
						+ filter
						+ "     <starts:boolean-op name='or'/>\n"
						+ createANDFieldFilter(fieldName, (String) phrases.nextElement())
						+ "     </starts:filter>\n";
			} else {
				filter = createANDFieldFilter(fieldName, (String) phrases.nextElement());
				madeOneFilter = true;
			}
		}

		return filter;
	}

	private String createSimpleFilter(String fieldName, String value) {
		return "     <starts:filter>\n"
			+ "         <starts:term>\n"
			+ "             <starts:field name='"
			+ fieldName
			+ "'/>\n"
			+ "             <starts:value>"
			+ value
			+ "</starts:value>\n"
			+ "         </starts:term>\n"
			+ "     </starts:filter>\n";
	}

	/*substitute linkage of 'localhost' to the actual host name */
	public String fixLinkage(String str, String server_URL) throws Exception {
		String result = new String(); // holds the resulting changed string
		// a reg.ex. to look for the needed field
		RE re = new RE("<[^>]*field[^>]+name=[\"\']linkage[\"\'].*?<[^>]*?value>(.*?)<", RE.REG_ICASE);

		URL url;
		URL serverURL = new URL(server_URL);
		int current_string_index = 0; // index into the input string

		// find all matches
		REMatch[] matches = re.getAllMatches(str);
		for (int i = 0; i < matches.length; i++) {
			url = new URL(matches[i].toString(1));
			if (url.getHost().equalsIgnoreCase("localhost")) {
				url = new URL(url.getProtocol(), serverURL.getHost(), url.getPort(), url.getFile());
			}
			result += str.substring(current_string_index, matches[i].getSubStartIndex(1));
			result += url;
			current_string_index = matches[i].getSubEndIndex(1);
		}

		// copy over the remainder of the string
		result += str.substring(current_string_index);

		return result;
	}

	String fixLinkage(URL url, String server_URL) throws Exception {
		if (url.getHost().equalsIgnoreCase("localhost"))
			url = new URL(url.getProtocol(), new URL(server_URL).getHost(), url.getPort(), url.getFile());

		return url.toString();
	}

	private Enumeration getANDTerms(String in) {
		Vector terms = new Vector();
		String phrase = "";
		String token;
		StringTokenizer tok = new StringTokenizer(in);
		while (tok.hasMoreTokens()) {
			token = tok.nextToken();
			if (token.equalsIgnoreCase("and")) {
				if (!phrase.equals("")) {
					terms.add(phrase.trim());
					phrase = "";
				}
			} else
				phrase += token + " ";
		}

		if (!phrase.equals(""))
			terms.add(phrase.trim());

		return terms.elements();
	}

	public String getEndNotes() {
		return "<br>";
	}

	private Enumeration getORTerms(String in) {
		Vector terms = new Vector();
		String phrase = "";
		String token;
		StringTokenizer tok = new StringTokenizer(in);
		while (tok.hasMoreTokens()) {
			token = tok.nextToken();
			if (token.equalsIgnoreCase("or")) {
				if (!phrase.equals("")) {
					terms.add(phrase.trim());
					phrase = "";
				}
			} else
				phrase += token + " ";
		}

		if (!phrase.equals(""))
			terms.add(phrase.trim());

		return terms.elements();
	}

	public Vector getUsedFieldValuePairs(String[] allFields, String[] allFieldValues) {
		Vector pairs = new Vector();

		for (int i = 0; i < allFieldValues.length; i++)
			if (!allFieldValues[i].equals("")) {
				pairs.add(allFields[i]);
				pairs.add(parseString(allFieldValues[i]));
			}

		return pairs;
	}

	private String getXSLTrans(Vector namesAndFullNames) {
		String namesToFullNames = "";

		Iterator it = namesAndFullNames.iterator();
		while (it.hasNext())
			namesToFullNames += "<xsl:if test=\"$collectionName='"
				+ (String) it.next()
				+ "'\">"
				+ "<xsl:text>"
				+ (String) it.next()
				+ "</xsl:text>"
				+ "</xsl:if>";

		return "<?xml version='1.0'?>"
			+ "<xsl:stylesheet "
			+ "xmlns:starts=\"http://sdarts.cs.columbia.edu/STARTS/\" "
			+ "xmlns:sdlip=\"http://interlib.org/SDLIP/1.0#\" "
			+ "xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
			+ "<xsl:template match=\"/\">"
			+ "<html>"
			+ "<head>"
			+ "<title>SDARTS Search Results</title>"
			+ "<style>"
			+ "<xsl:text>th {background-color: #673591; font-size: large; color: white}</xsl:text>"
			+ "<xsl:text>caption,p {font-family: Verdana, Arial, Helvetica, sans-serif; font-weight: 500; font-size: large}</xsl:text>"
			+ "</style>"
			+ "</head>"
			+ "<body>"
			+ "<center>"
			+ "<xsl:variable name=\"numdocs\"  select=\"string(/sdlip:SearchResult/sdlip:doc/sdlip:propList/starts:sqresults[@numdocs != '0'])\"/>"
			+ "<xsl:choose>"
			+ "<xsl:when test=\"not($numdocs)\">"
			+ "<h1>"
			+ "<xsl:text>The search produced no results.</xsl:text>"
			+ "</h1>"
			+ "</xsl:when>"
			+ "<xsl:otherwise>"
			+ "<xsl:for-each select=\"/sdlip:SearchResult/sdlip:doc/sdlip:propList\">"
			+ "<xsl:variable name=\"collectionName\" select=\"normalize-space(starts:sqresults/starts:source)\"/>"
			+ "<xsl:choose>"
			+ "<xsl:when test=\"starts:sqresults/@numdocs = 0\">"
			+ "<p>"
			+ "<xsl:text>No matches in </xsl:text>"
			+ "<i>"
			+ namesToFullNames
			+ "</i>"
			+ "</p><br/><br/>"
			+ "</xsl:when>"
			+ "<xsl:otherwise>"
			+ "<xsl:variable name=\"hasAuthor\">"
			+ "<xsl:value-of select=\".//starts:doc-term/starts:field[@name='author']/../starts:value/text()\"/>"
			+ "</xsl:variable>"
			+ "<xsl:variable name=\"hasScore\">"
			+ "<xsl:value-of select=\".//starts:sqrdocument/starts:rawscore[number(text())!=0.0]\"/>"
			+ "</xsl:variable>"
			+ "<xsl:variable name=\"numAvailable\">"
			+ "<xsl:value-of select=\"number(normalize-space(starts:sqresults/@numavailable))\"/>"
			+ "</xsl:variable>"
			+ "<xsl:variable name=\"numReturned\">"
			+ "<xsl:value-of select=\"number(normalize-space(starts:sqresults/@numdocs))\"/>"
			+ "</xsl:variable>"
			+ "<table border=\"1\">"
			+ "<caption>"
			+ "<xsl:text>Results from </xsl:text>"
			+ "<i>"
			+ 
		//"<xsl:value-of select=\"$collectionName\"/>" +
		namesToFullNames
			+ "</i>"
			+ "<xsl:if test=\"number($numAvailable)\">"
			+ "<br/>"
			+ "<font size=\"-2\">"
			+ "<xsl:text>Retrieved </xsl:text>"
			+ "<xsl:value-of select=\"$numReturned\"/>"
			+ "<xsl:text> documents out of </xsl:text>"
			+ "<xsl:value-of select=\"$numAvailable\"/>"
			+ "<xsl:text> available</xsl:text>"
			+ "</font>"
			+ "</xsl:if>"
			+ "</caption>"
			+ "<tr>"
			+ "<xsl:if test=\"string-length($hasScore) &gt; 0\">"
			+ "<th>Ranking</th>"
			+ "</xsl:if>"
			+ "<xsl:if test=\"string-length($hasAuthor) &gt; 0\">"
			+ "<th>Author</th>"
			+ "</xsl:if>"
			+ "<th>Title</th>"
			+ "</tr>"
			+ "<xsl:for-each select=\".//starts:sqrdocument\">"
			+ "<xsl:variable name=\"score\" select=\"substring(normalize-space(starts:rawscore),1,7)\"/>"
			+ "<xsl:variable name=\"author\" select=\"normalize-space(starts:doc-term/starts:field[@name='author']/../starts:value)\"/>"
			+ "<xsl:variable name=\"title\" select=\"normalize-space(starts:doc-term/starts:field[@name='title']/../starts:value)\"/>"
			+ "<xsl:variable name=\"linkage\" select=\"normalize-space(starts:doc-term/starts:field[@name='linkage']/../starts:value)\"/>"
			+ "<tr>"
			+ "<xsl:if test=\"string-length($hasScore) &gt; 0\">"
			+ "<td><xsl:value-of select=\"$score\"/></td>"
			+ "</xsl:if>"
			+ "<xsl:if test=\"not($hasAuthor='')\">"
			+ "<td><xsl:value-of select=\"$author\"/></td>"
			+ "</xsl:if>"
			+ "<td><a href=\"{$linkage}\"><xsl:value-of select=\"$title\"/></a></td>"
			+ "</tr>"
			+ "</xsl:for-each>"
			+ "</table>"
			+ "<br/><br/>"
			+ "</xsl:otherwise>"
			+ "</xsl:choose>"
			+ "</xsl:for-each>"
			+ "</xsl:otherwise>"
			+ "</xsl:choose>"
			+ "</center>"
			+ "</body>"
			+ "</html>"
			+ "</xsl:template>"
			+ "</xsl:stylesheet>";
	}
	
	public String makeQueryFilter(Vector fieldsAndValues) throws Exception 
	{
		return makeQueryFilter(fieldsAndValues, true);
	}

	public String makeQueryFilter(Vector fieldsAndValues, boolean joinFieldsWithAnd) throws Exception 
	{
		if (fieldsAndValues.size() == 0)
			throw new Exception("You must specify some values in the search fields!");

		String filter = "";
		boolean madeOneFilter = false;

		Iterator it = fieldsAndValues.iterator();
		while (it.hasNext())
			if (madeOneFilter) 
			{
				filter = "     <starts:filter>\n" + filter;
				
				if ( joinFieldsWithAnd )
					filter += "     <starts:boolean-op name='and'/>\n";
				else
					filter += "     <starts:boolean-op name='or'/>\n";
				
				filter += createFieldFilter((String) it.next(), (String) it.next());
				filter += "     </starts:filter>\n";
			} 
			else 
			{
				filter = createFieldFilter((String) it.next(), (String) it.next());
				madeOneFilter = true;
			}

		return filter;
	}

	public String makeQueryFooter() {
		return "     <starts:answer-fields>\n"
			+ "         <starts:field name='author'/>\n"
			+ "         <starts:field name='title'/>\n"
			+ "         <starts:field name='linkage'/>\n"
			+ "     </starts:answer-fields>\n"
			+ "     <starts:sort-by-fields>\n"
			+ "	  <starts:score/>\n"
			//+ "         <starts:sort-by-field ascending-descending='d'>\n"
			//+ "             <starts:field name='title'/>\n"
			//+ "         </starts:sort-by-field>\n"
			+ "     </starts:sort-by-fields>\n"
			+ "</starts:squery>\n";
	}

	public String makeQueryHeader( int maxResults) {
		return "<?xml version='1.0' encoding='UTF-8'?>\n"
	  		+ "<starts:squery xmlns:starts='http://sdarts.cs.columbia.edu/STARTS/'" +//	  		  " xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +//	  		  " xsi:schemaLocation='http://sdarts.cs.columbia.edu/STARTS/ " +
//				startsDTDURL + "' " +	  		  " version='STARTS 1.0' " 
			//+ "<!DOCTYPE starts:squery SYSTEM 'http://www.cs.columbia.edu/~dli2test/dtd/starts.dtd'>\n"
			//+ "<starts:squery xmlns:starts='http://www.cs.columbia.edu/~dli2test/STARTS/' "
			
			+ "drop-stop='false' max-docs='"
			+ String.valueOf(maxResults)
			+ "'>\n";
	}

	public String makeQueryRanking(Vector fieldsAndValues) {
		String ranking = "     <starts:ranking>\n";
		Enumeration terms;
		Iterator it = fieldsAndValues.iterator();
		String fieldName;

		while (it.hasNext()) {
			fieldName = (String) it.next();
			terms = getANDTerms((String) it.next());
			while (terms.hasMoreElements())
				ranking += "         <starts:term weight='0.5'>\n"
					+ "             <starts:field name='"
					+ fieldName
					+ "'/>\n"
					+ "             <starts:value>"
					+ (String) terms.nextElement()
					+ "</starts:value>\n"
					+ "         </starts:term>\n";
		}

		ranking += "     </starts:ranking>\n";
		return ranking;
	}

	public String makeQuerySources(String[] sources) {
		String result = "";
		for (int i = 0; i < sources.length; i++)
			result += "     <starts:source>" + sources[i] + "</starts:source>\n";

		return result;
	}

	public String[] parseCollectionInfo(String collInfo) throws Exception {
		CIHandler cih = new CIHandler();
		String[] collinfo = cih.parse(new StringReader(collInfo));
		return collinfo;
	}

	public HashMap parseMetadata(String metadata, String serverURL) throws Exception {
		MDHandler mdh = new MDHandler(serverURL);
		HashMap mdata = mdh.parse(new StringReader(metadata));
		return mdata;
	}

	//this utility method is used to parse out quotes or any other special characters used in the input boxes
	private String parseString(String unParsedString) {
		StringTokenizer tok = new StringTokenizer(unParsedString, " \"+-");

		String parsedString = "";
		int count = 0;
		while (tok.hasMoreTokens())
			parsedString += tok.nextToken() + " ";

		return parsedString.trim().toLowerCase();
	}

	public String removeNamespace(String input) {
		int i = input.indexOf(':');
		return (i < 0) ? input : input.substring(i + 1);
	}

	public String transformOut(String queryResult, Vector namesAndFullNames) throws Exception {
		javax.xml.transform.TransformerFactory factory = javax.xml.transform.TransformerFactory.newInstance();

		javax.xml.transform.Source xsl = new javax.xml.transform.stream.StreamSource(new StringReader(getXSLTrans(namesAndFullNames)));
		javax.xml.transform.Templates template = factory.newTemplates(xsl);
		javax.xml.transform.Transformer transformer = template.newTransformer();
		javax.xml.transform.Source xml = new javax.xml.transform.stream.StreamSource(new StringReader(queryResult));
		java.io.ByteArrayOutputStream oStream = new java.io.ByteArrayOutputStream();
		javax.xml.transform.stream.StreamResult result = new javax.xml.transform.stream.StreamResult(oStream);

		transformer.transform(xml, result);

		return oStream.toString();

		//		XSLTProcessorFactory proc = new XSLTProcessorFactory();
		//		XSLTInputSource xmlIn = new XSLTInputSource(new StringReader(queryResult));
		//		XSLTInputSource xslIn = new XSLTInputSource(new StringReader(getXSLTrans(namesAndFullNames)));
		//		ByteArrayOutputStream out = new ByteArrayOutputStream();
		//		XSLTResultTarget target = new XSLTResultTarget(out);
		//		proc.getProcessor().process(xmlIn, xslIn, target);
		//		return out.toString();
	}
}
