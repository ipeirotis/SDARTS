package edu.columbia.cs.sdarts.backend.doc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.frontend.SDARTS;
import edu.columbia.cs.sdarts.util.SDARTSHandlerBase;
import edu.columbia.cs.sdarts.util.UnsynchStack;
import gnu.regexp.RE;
import gnu.regexp.REException;
import gnu.regexp.RESyntax;

/**
 * Loads and creates a {@link edu.columbia.cs.sdarts.backend.doc.DocConfig DocConfig}
 * from the <code>doc_config.xml</code> file. There is only one method,
 * the static {@link #load(String)} method.
 * <p>
 * The directory to load from is always:
 * <code>SDARTS_HOME/config/<i>backEndLSPName</i></code>
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class DocConfigBuilder {
  // ------------ FIELDS ------------
  private static DCBHandler handler;


  // ------------ METHODS ------------
  /**
   * Loads a stored <code>DocConfig</code> from the XML file.
   * @param backEndLSPName the name of the
   * {@link edu.columbia.cs.sdarts.backend.BackEndLSP} to whom this file belongs.
   * @return the stored <code>DocConfig</node>
   * @exception BackEndException if something goes wrong during loading
   */
  public static DocConfig load (String backEndLSPName) throws BackEndException {
	try {
	  handler = new DCBHandler (backEndLSPName);	  
	  DocConfig config = handler.parse();
	  config.setBackEndLSPName (backEndLSPName);
	  return config;
	}
	catch (Exception e) {
	  throw new BackEndException (e.getMessage());
	}
  }


  // ------------ INNER CLASS ------------
  private static class DCBHandler extends SDARTSHandlerBase {
	// -------- CONSTANTS --------
	private static final int NONE               		= -1;
	private static final int DOC_CONFIG         		= 0;
	private static final int PATH               		= 1;
	private static final int EXTENSION          		= 2;
	private static final int LINKAGE_PREFIX     		= 3;
	private static final int LINKAGE_TYPE       		= 4;
	private static final int LANGUAGE           		= 5;
	private static final int STOP_WORDS         		= 6;
	private static final int FIELD_DESCRIPTOR   		= 7;
	private static final int START              		= 8;
	private static final int END                		= 9;
	private static final int DATE_FORMATS       		= 10;
	private static final int SIMPLE_DATE_FORMAT 		= 11;
	private static final int REGEXP             		= 12;
	private static final int WORD               		= 13;
	private static final int CLASSIFICATION_SCHEMA_PATH = 14;
	private static final int SPECIFICITY_THRESHOLD 		= 15;
	private static final int DOCUMENTS_PER_QUERY 		= 16;
	private static final int CACHE_LOCATION 			= 17;

	// -------- PARSING --------
	private int state = NONE;

	// -------- STORAGE --------
	private String       backEndLSPName;
	private DocConfig    docConfig;
	private UnsynchStack stack;
	private UnsynchStack paths;
	private UnsynchStack extensions;
	private UnsynchStack dateFormats;
	private UnsynchStack fieldDescriptors;


	public DCBHandler (String backEndLSPName) throws SAXException 
	{
		EnableValidation(true);
		EnableSchemaChecking(true);
		  
		this.backEndLSPName  = backEndLSPName;
		stack                = new UnsynchStack();
		paths                = new UnsynchStack();
		extensions           = new UnsynchStack();
		dateFormats          = new UnsynchStack();
		fieldDescriptors     = new UnsynchStack();
	}

	public DocConfig parse () throws IOException, SAXException {
	  if (docConfig == null) {
		try {
		  String configFilename = SDARTS.CONFIG_DIRECTORY + File.separator +
								  backEndLSPName + File.separator +
								  DocConstants.CONFIG_FILENAME;

		  parser.parse (new InputSource
						(new BufferedReader
						 (new InputStreamReader
						  (new FileInputStream (configFilename)))));
        
		}
		catch (Exception e) {
		  processException (e.getMessage());
		}
	  }

	  return docConfig;
	}

	public void startElement (String name, AttributeList attrs) throws SAXException {
	  if (name.equals ("doc-config")) {
		state = DOC_CONFIG;
		boolean recursive = true;
		String recursiveString = attrs.getValue ("recursive");
		if (recursiveString != null && recursiveString.equals("false")) {
			recursive = false;
		}
		boolean reIndex = true;
		String reIndexString = attrs.getValue ("re-index");
		if (reIndexString != null && reIndexString.equals ("false")) {
		  reIndex = false;
		}
		docConfig = new DocConfig (reIndex, recursive);
	  }
	  else if (name.equals("classification_schema_path")){
	  	state = CLASSIFICATION_SCHEMA_PATH;
	  }
	  else if (name.equals("specificity_threshold")){
		state = SPECIFICITY_THRESHOLD;
	  }
	  else if (name.equals("documents_per_query")){
		state = DOCUMENTS_PER_QUERY;
	  }
	  else if (name.equals("cache_location")){
		state = CACHE_LOCATION;
	  }
	  else if (name.equals ("path")) {
		state = PATH;
	  }
	  else if (name.equals ("extension")) {
		state = EXTENSION;
	  }
	  else if (name.equals ("linkage-prefix")) {
		state = LINKAGE_PREFIX;
	  }
	  else if (name.equals ("linkage-type")) {
		state = LINKAGE_TYPE;
	  }
	  else if (name.equals ("language")) {
		state = LANGUAGE;
	  }
	  else if (name.equals ("stop-words")) {
		state = STOP_WORDS;
	  }
	  else if (name.equals ("field-descriptor")) {
		state = FIELD_DESCRIPTOR;
		String fdName = attrs.getValue ("name");

		boolean skipStart = true;
		String skipStartVal = attrs.getValue("skip-start");
		if (skipStartVal != null && skipStartVal.equals ("false")) {
		  skipStart = false;
		}
		boolean skipEnd = true;
		String skipEndVal = attrs.getValue ("skip-end");
		if (skipEndVal != null && skipEndVal.equals ("false")) {
		  skipEnd = false;
		}
		fieldDescriptors.push
		  (new DocFieldDescriptor (fdName, skipStart, skipEnd));
	  }
	  else if (name.equals ("start")) {
		state = START;
	  }
	  else if (name.equals ("end")) {
		state = END;
	  }
	  else if (name.equals ("date-formats")) {
		state = DATE_FORMATS;
	  }
	  else if (name.equals ("simple-date-format")) {
		state = SIMPLE_DATE_FORMAT;
	  }
	  else if (name.equals ("regexp")) {
		state = REGEXP;
	  }
	  else if (name.equals ("word")) {
		state = WORD;
	  }
	  else {
		processException ("unknown element : " + name);
	  }
	}

	public void characters(char[] chars, int start, int length) throws SAXException {
	  //DocConfig tc = null;
	  String value = new String (chars, start, length);
	  switch (state) {
		/*case DOC_CONFIG:
		case FIELD_DESCRIPTOR:
		case STOP_WORDS:
		case DATE_FORMATS:
		case START:
		case END:
			processException ("invalid input");
		break;*/

		case PATH:
		  paths.add (value);
		break;

		case EXTENSION:
		  if (!value.startsWith (".")) {
			value = "." + value;
		  }
		  extensions.add (value);
		break;

		case LINKAGE_PREFIX:
		  docConfig.setLinkagePrefix (value);
		break;

		case LINKAGE_TYPE:
		  docConfig.setLinkageType (value);
		break;

		case LANGUAGE:
		  docConfig.setLanguage (value);
		break;

		case SIMPLE_DATE_FORMAT:
		  dateFormats.push (new SimpleDateFormat (value));
		break;

		case REGEXP:
		  try {
			stack.push (new RE (value, RE.REG_MULTILINE, RESyntax.RE_SYNTAX_SED));
		  }
		  catch (REException e) {
			processException (e.getMessage());
		  }
		break;

		case WORD:
		  stack.push (value);
		break;
		
		case CLASSIFICATION_SCHEMA_PATH:
			docConfig.setClassificationSchemaPath(value);	
		break;
	
		case SPECIFICITY_THRESHOLD:
			docConfig.setSpecificityThreshold(value);
		break;
		
		case DOCUMENTS_PER_QUERY:
		    docConfig.setMaxDocumentPerQuery(value);
		break;
		
		case CACHE_LOCATION:
			docConfig.setCacheLocation(value);
		break;
	  }
	}

	public void endElement (String name) throws SAXException {
	  RE end = null;
	  RE start = null;
	  //Object obj = null;
	  //SimpleDateFormat dateFormat = null;

	  switch (state) {
		case DOC_CONFIG:
		state = NONE;
		if (!paths.isEmpty()) {
		  docConfig.
			setPaths ((String[]) paths.toArray (new String[0]));
		  paths.clear();
		}
		if (!extensions.isEmpty()) {
			docConfig.
			  setExtensions ((String[]) extensions.toArray (new String[0]));
			  extensions.clear();
		}
		if (!fieldDescriptors.isEmpty()) {
		  docConfig.setFieldDescriptors ((DocFieldDescriptor[])
			fieldDescriptors.toArray (new DocFieldDescriptor[0]));
		  fieldDescriptors.clear();
		}
		if (!dateFormats.empty()) {
			docConfig.setDateFormats
			  ((SimpleDateFormat[]) dateFormats.toArray
				(new SimpleDateFormat[0]));
			dateFormats.clear();
		}
		break;

		case PATH:
		case EXTENSION:
		case LINKAGE_PREFIX:
		case LINKAGE_TYPE:
		case LANGUAGE:
		case CLASSIFICATION_SCHEMA_PATH:
		case SPECIFICITY_THRESHOLD:
		case DOCUMENTS_PER_QUERY:
		case CACHE_LOCATION:
		  state = DOC_CONFIG;
		break;

		case FIELD_DESCRIPTOR:
		  state = DOC_CONFIG;
		  DocFieldDescriptor dfd =
			(DocFieldDescriptor) fieldDescriptors.peek();
		  end = (RE) stack.pop();
		  start = (RE) stack.pop();
		  dfd.setStart (start);
		  dfd.setEnd (end);
		break;

		case STOP_WORDS:
		  state = DOC_CONFIG;
		  String[] stopWords = (String[]) stack.toArray (new String[0]);
		  stack.clear();
		  docConfig.setStopWords(stopWords);
		break;

		case START:
		  state = FIELD_DESCRIPTOR;
		break;

		case END:
		  state = FIELD_DESCRIPTOR;
		break;

		case DATE_FORMATS:
		  state = DOC_CONFIG;
		break;

		case SIMPLE_DATE_FORMAT:
		  state = DATE_FORMATS;
		break;

		case REGEXP:
		  // hack here, since START, END go to same state;
		  state = START;
		break;

		case WORD:
		  state = STOP_WORDS;
		break;
	  }
	}
  }
}
