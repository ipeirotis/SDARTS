
package edu.columbia.cs.sdarts.backend.impls.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.omg.CORBA.IntHolder;
import org.xml.sax.AttributeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.lucene.document.DateField;
import com.lucene.document.Document;
import com.lucene.document.Field;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.backend.doc.DocConstants;
import edu.columbia.cs.sdarts.backend.doc.DocFieldDescriptor;
import edu.columbia.cs.sdarts.backend.doc.lucene.DocumentEnum;
import edu.columbia.cs.sdarts.common.FieldNames;
import edu.columbia.cs.sdarts.frontend.SDARTS;
import edu.columbia.cs.sdarts.util.SDARTSHandlerBase;
import edu.columbia.cs.sdarts.util.UnsynchStack;


/**
 * An implementation of {@link edu.columbia.cs.sdarts.backend.doc.lucene.DocumentEnum}
 * used to parse XML files for indexing by the Lucene search
 * engine. There is no need to instantiate this class; it is automatically
 * used by the {@link edu.columbia.cs.sdarts.backend.impls.xml.XMLBackEndLSP XMLBackEndLSP}.
 * Basically, it uses the <code>doc_style.xsl</code> file to learn how to
 * extract fields from the document. The SDARTS Design Document contains
 * more information on how this works, but the basic process is as follows:
 * <ul>
 * <li>Convert the file to be parsed into a new XML document that looks
 * something like this:<br>
 * <pre>
 * &lt;starts:intermediate&gt;
 * &lt;starts:sqr-document&gt;
 *    &lt;starts:doc-term&gt;
 *      &lt;starts:field name="title"/&gt;
 *      &lt;starts:value&gt;Study on Pulmonology&lt;/starts:value&gt;
 *    &lt;/starts:doc-term&gt;
 *    &lt;starts:doc-term&gt;
 *    . . . . . . .
 * &lt;/starts:sqr-document&gt;
 * &lt;/starts:intermediate&gt;
 * </pre>
 * As you can see, this format is a subset of the normal STARTS XML
 * format for reporting results. For more information about the
 * <code>starts_intermediate</code> format, see 
 * <a href="http://www.cs.columbia.edu/~dli2test/dtd/starts_intermediate.dtd">
 * starts_intermediate.dtd</a>.
 * <li>Do not output this new document as a file, but send
 * as a sequence of SAX events back to this class, which then uses them
 * to build the Lucene <code>Document</code> accordingly.
 * </ul>
 * <p>
 * Currently, XSL processing is being carried out by the
 * <a href="http://xml.apache.org/xalan">Apache Xalan</a> XSL
 * processor. All Xalan-related code is confined to this class.
 * A future version may want to hide the Xalan code behind another
 * interface, in order to make it easier to switch to another
 * XSL processor.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public final class XMLDocumentEnum extends DocumentEnum {
	  //private StylesheetRoot 	stylesheet;
  private Templates 		template;
  //private XSLTProcessor  	processor;
  private Transformer 		transformer;
  private XDHandler      	handler;
  Document       			document;
  boolean        			fdSet;
  
  /**
   * Builds a Lucene <code>Document</code> from an XML file
   */
  public Document createDocument(File f, IntHolder storeTokenCountHere) throws BackEndException 
  {
      // If processor is null, that means that the
      // stylsheet is null, so load and compile it.
      if (transformer == null) 
      {
        try 
        {
			TransformerFactory 		factory 			= TransformerFactory.newInstance();
			String 					styleSheetFilename 	= 	SDARTS.CONFIG_DIRECTORY + 
															File.separator +
            												getDocConfig().getBackEndLSPName() + 
            												File.separator +
            												DocConstants.STYLESHEET_FILENAME;
			Source 					xsl 				= new StreamSource(new FileInputStream(styleSheetFilename));
			template 									= factory.newTemplates(xsl);
			transformer 								= template.newTransformer();
        }
        catch (Exception e) 
        {
          e.printStackTrace();
          throw new BackEndException (e.getMessage());
        }
      }

      // If field descriptors have not been set on docConfig, make a note
      // to do it
      fdSet = (getDocConfig().getFieldDescriptors() != null);

      // Start parsing
      try 
      {
       // System.out.println ("XSL processing " + f.getName());
       // long 					startParse 		= System.currentTimeMillis();
        //XSLTResultTarget 		target 			= new XSLTResultTarget (new XDHandler(storeTokenCountHere));
        FileInputStream 		fis 			= new FileInputStream (f.getPath());
		Source 					xml 			= new StreamSource(fis);
		ByteArrayOutputStream 	oStream 		= new java.io.ByteArrayOutputStream();
		StreamResult 			result			= new StreamResult(oStream);
		
		transformer.transform( xml, result );
		
        //stylesheet.process(new XSLTInputSource (fis), target);
	 	//org.apache.xerces.parsers.SAXParser p = new org.apache.xerces.parsers.SAXParser();
		//p.setDocumentHandler(new XDHandler(storeTokenCountHere));
		XDHandler				handler = new XDHandler(storeTokenCountHere);
		handler.parse(new InputSource(new StringReader(oStream.toString())));
		//p.parse(new InputSource(oStream.toString()));
		
       // long stopParse  = System.currentTimeMillis();
       // System.out.println ("Processed in " + (stopParse-startParse) + " ms");
      }
      catch (Exception e) 
      {
        e.printStackTrace();
        throw new BackEndException (e.getMessage());
      }

      // Done
      return document;
  }

  private class XDHandler extends SDARTSHandlerBase {
      private int state;
      private static final int UNKNOWN      = 0;
      private static final int INTERMEDIATE = 1;
      private static final int SQRDOCUMENT  = 2;
      private static final int DOC_TERM     = 3;
      private static final int FIELD        = 4;
      private static final int VALUE        = 5;

      private UnsynchStack fields = new UnsynchStack();
      private String fieldName;
      private StringBuffer fieldValue;
      private IntHolder tokenCount;

      public XDHandler (IntHolder tokenCount) throws SAXException, ParserConfigurationException
      {
		//EnableValidation(true);
		
        this.tokenCount = tokenCount;
        fieldValue = new StringBuffer();
      }

      public void startElement (String name, AttributeList atts)
        throws SAXException {
        name = this.removeNamespace(name);
	if (name.equals ("intermediate")) {
	    state = INTERMEDIATE;
	}
        else if (name.equals ("sqrdocument")) {
          state = SQRDOCUMENT;
        }
        else if (name.equals ("doc-term")) {
          state = DOC_TERM;
        }
        else if (name.equals ("field")) {
          state = FIELD;
          fieldName = atts.getValue("name");
        }
        else if (name.equals ("value")) {
          state = VALUE;
        }
        else {
          throw new SAXException ("unknown element: " + name);
        }
      }

      public void characters (char[] ch, int start, int length)
        throws SAXException {
        String val = new String (ch, start, length).trim();
        if (val.equals("")) {
          return;
        }
        switch (state) {
          case VALUE:
            fieldValue.append (val);
            fieldValue.append (" ");
            StringTokenizer st = new StringTokenizer (val, " ");
            tokenCount.value += st.countTokens();
          break;

          default:
            throw new SAXException ("illegal value location: " + val);
        }
      }

      public void endElement (String name) throws SAXException {
          switch (state) {
	    case INTERMEDIATE:
	      break;

            case SQRDOCUMENT:
              XMLDocumentEnum.this.document =
                new Document();
              for (Iterator it = fields.iterator() ; it.hasNext() ; ) {
                Field f = (Field) it.next();
                document.add(f);
              }
              if (!XMLDocumentEnum.this.fdSet) {
                List fds = new LinkedList();
                for (Iterator it = fields.iterator() ; it.hasNext() ; ) {
                  Field f = (Field) it.next();
                  fds.add (new DocFieldDescriptor (f.name(),true,true));
                }
                getDocConfig().
                  setFieldDescriptors
                    ((DocFieldDescriptor[])
                      fds.toArray(new DocFieldDescriptor[0]));
              }
              fields.clear();
	      state = INTERMEDIATE;
            break;

            case DOC_TERM:
              Field f = null;
              if (FieldNames.isDate(fieldName)) {
                try {
                  long dateNum = parseDate (fieldValue.toString());
                  f = Field.Keyword (fieldName, DateField.timeToString(dateNum));
                }
                catch (BackEndException e) {
                  throw new SAXException (e.getMessage());
                }
              }
              else {
                String fieldVal = makeValue (fieldValue.toString());
                if (fieldName.equals (FieldNames.BODY_OF_TEXT)) {
                  f = Field.Text (fieldName, new StringReader (fieldVal));
                }
                else {
                  f = new Field (fieldName, fieldVal, true, true, true);
                }

              }
              fields.push (f);
              fieldName = null;
              fieldValue.setLength(0);
              state = SQRDOCUMENT;
            break;

            case FIELD:
            case VALUE:
              state = DOC_TERM;
            break;
          }
      }
  }
}
