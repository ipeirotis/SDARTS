
package edu.columbia.cs.sdarts.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * A specialized <code>java.io.PrintWriter</code> designed specifically for
 * outputting XML.
 * <p>
 * The first output from the writer will usually be a standard
 * XML declaration, including &lt?XML ...&gt and
 * &lt!DOCTYPE . . . &gt. The &lt!DOCTYPE . . . &gt will include the main
 * DTD for the document, and will even include external parameter entities.
 * The <code>XMLWriter</code> learns about DOCTYPES statically, using the
 * {@link #addPublicDocType(String,String) addPublicDocType()} and
 * {@link #addSystemDocType(String,String) addSystemDocType()} methods,
 * which correspond to the PUBLIC and SYSTEM XML DOCTYPES. Since these methods
 * are static, they need be called only once for each DOCTYPE added.
 * <p>
 * There are also plenty of convenience methods for outputting all XML
 * structures, as well as strict control over indenting and carriage returns.
 * The <code>XMLWriter</code> keeps track of depth of indentation, and
 * whether it is in a particular namespace or not, and outputs accordingly.
 * There are methods to control these properties, as well.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class XMLWriter extends PrintWriter {
  // ------------ FIELDS ------------
  private static Map docTypeMap = new HashMap();
  public  static final String XML_VERSION = "1.0";
  public  static final String ENCODING = "UTF-8";
  private static final int PUBLIC = 0;
  private static final int SYSTEM = 1;
  private String        tabString = "";
  private String        namespaceString;
  private UnsynchStack  namespaceStack = new UnsynchStack();
  private boolean autoIndent = true;
  private boolean autoCR = true;
  private boolean isolateAttributes = false;
  private boolean isXSD = false;



  // ------------ METHODS ------------
  // -------- CONSTRUCTOR --------
  /**
   * Create an XMLWriter that <b>DOES NOT</b> output a standard XML
   * declaration header.
   * @param writer the <code>Writer</code> to wrap the
   * <code>XMLWriter</code> around.
   */
  public XMLWriter (Writer writer) {
    super (writer);
    namespaceStack.push("");
  }

  /**
   * Create an XMLWriter that <b>DOES NOT</b> output a standard XML
   * declaration header.
   * @param out the <code>OutputStream</code> to wrap the
   * <code>XMLWriter</code> around.
   */
  public XMLWriter (OutputStream out) {
    super (out);
    namespaceStack.push("");
  }

  /**
   * Create an XMLWriter that outputs a standard XML
   * declaration header, including a &lt!DOCTYPE . . . &gt header.
   * The <code>XMLWriter</code> must be pre-informed, via the static
   * <code>setPublicDocType()</code> and <code>setSystemDocType()</code>
   * methods, about all DOCTYPES passed in as
   * parameters.
   * <p>
   * If there is more than one DOCTYPE in the parameter array,
   * the first one will be declared the DOCTYPE, and the others will be
   * declared as external parameter entities. This is the way, for example,
   * that STARTS gets nested within SDLIP.
   * <p>
   * Note that the <code>autoFlush</code> property of the <code>PrintWriter</code>
   * superclass is set to <code>false</code>
   * @param out the <code>OutputStream</code> to wrap the
   * <code>XMLWriter</code> around.
   * @param docTypes the DOCTYPES used in the resulting document
   */
  public XMLWriter (OutputStream out, String[] docTypes) throws IOException {
    super (out);
    namespaceStack.push("");
    writeXMLHeading (docTypes);
  }

  /**
   * Create an XMLWriter that outputs a standard XML
   * declaration header, including a &lt!DOCTYPE . . . &gt header.
   * The <code>XMLWriter</code> must be pre-informed, via the static
   * <code>setPublicDocType()</code> and <code>setSystemDocType()</code>
   * methods, about all DOCTYPES passed in as
   * parameters.
   * <p>
   * If there is more than one DOCTYPE in the parameter array,
   * the first one will be declared the DOCTYPE, and the others will be
   * declared as external parameter entities. This is the way, for example,
   * that STARTS gets nested within SDLIP.
   * <p>
   * @param out the <code>OutputStream</code> to wrap the
   * <code>XMLWriter</code> around.
   * @param autoFlush whether the underlying stream automatically flushes
   * or not
   * @param docTypes the DOCTYPES used in the resulting document
   */
  public XMLWriter (OutputStream out, boolean autoFlush, String[] docTypes)
    throws IOException {
      super (out, autoFlush);
      namespaceStack.push("");
      writeXMLHeading (docTypes);
  }

  /**
   * Create an XMLWriter that outputs a standard XML
   * declaration header, including a &lt!DOCTYPE . . . &gt header.
   * The <code>XMLWriter</code> must be pre-informed, via the static
   * <code>setPublicDocType()</code> and <code>setSystemDocType()</code>
   * methods, about all DOCTYPES passed in as
   * parameters.
   * <p>
   * If there is more than one DOCTYPE in the parameter array,
   * the first one will be declared the DOCTYPE, and the others will be
   * declared as external parameter entities. This is the way, for example,
   * that STARTS gets nested within SDLIP.
   * <p>
   * @param out the <code>OutputStream</code> to wrap the
   * <code>XMLWriter</code> around.
   * @param autoFlush whether the underlying stream automatically flushes
   * or not
   * @param autoIndent whether to automatically indent each element
   * @param autoCR whether to automatically emit a carriage return after
   * each element or value
   * @param isolateAttributes whether each attribute of a printed XML element
   * should be on its own line (good for elements with lots of attributes)
   * @param docTypes the DOCTYPES used in the resulting document
   */
  public XMLWriter (OutputStream out, boolean autoFlush,
                    boolean autoIndent, boolean autoCR,
                    boolean isolateAttributes, String[] docTypes)
                      throws IOException {
    super (out, autoFlush);
    this.autoIndent = autoIndent;
    this.autoCR = autoCR;
    this.isolateAttributes = isolateAttributes;
    namespaceStack.push("");
    writeXMLHeading (docTypes);
  }

  /**
   * Create an XMLWriter that outputs a standard XML
   * declaration header, including a &lt!DOCTYPE . . . &gt header.
   * The <code>XMLWriter</code> must be pre-informed, via the static
   * <code>setPublicDocType()</code> and <code>setSystemDocType()</code>
   * methods, about all DOCTYPES passed in as
   * parameters.
   * <p>
   * If there is more than one DOCTYPE in the parameter array,
   * the first one will be declared the DOCTYPE, and the others will be
   * declared as external parameter entities. This is the way, for example,
   * that STARTS gets nested within SDLIP.
   * <p>
   * Note that the <code>autoFlush</code> property of the <code>PrintWriter</code>
   * superclass is set to <code>false</code>
   * @param out the <code>Writer</code> to wrap the
   * <code>XMLWriter</code> around.
   * @param docTypes the DOCTYPES used in the resulting document
   */
  public XMLWriter (Writer out, String[] docTypes) throws IOException {
    super (out);
    namespaceStack.push("");
    writeXMLHeading (docTypes);
  }
  
  /**
	 * Create an XMLWriter that outputs a standard XML
	 * declaration header, including a &lt!DOCTYPE . . . &gt header.
	 * The <code>XMLWriter</code> must be pre-informed, via the static
	 * <code>setPublicDocType()</code> and <code>setSystemDocType()</code>
	 * methods, about all DOCTYPES passed in as
	 * parameters.
	 * <p>
	 * If there is more than one DOCTYPE in the parameter array,
	 * the first one will be declared the DOCTYPE, and the others will be
	 * declared as external parameter entities. This is the way, for example,
	 * that STARTS gets nested within SDLIP.
	 * <p>
	 * Note that the <code>autoFlush</code> property of the <code>PrintWriter</code>
	 * superclass is set to <code>false</code>
	 * @param out the <code>Writer</code> to wrap the
	 * <code>XMLWriter</code> around.
	 * @param docTypes the DOCTYPES used in the resulting document
	 */
	public XMLWriter (Writer out, String[] docTypes, boolean isXSD) throws IOException {
	  super (out);
	  namespaceStack.push("");
	  this.isXSD = isXSD;
	  writeXMLHeading (docTypes);
	}

  /**
   * Create an XMLWriter that outputs a standard XML
   * declaration header, including a &lt!DOCTYPE . . . &gt header.
   * The <code>XMLWriter</code> must be pre-informed, via the static
   * <code>setPublicDocType()</code> and <code>setSystemDocType()</code>
   * methods, about all DOCTYPES passed in as
   * parameters.
   * <p>
   * If there is more than one DOCTYPE in the parameter array,
   * the first one will be declared the DOCTYPE, and the others will be
   * declared as external parameter entities. This is the way, for example,
   * that STARTS gets nested within SDLIP.
   * <p>
   * @param out the <code>Writer</code> to wrap the
   * <code>XMLWriter</code> around.
   * @param autoFlush whether the underlying stream automatically flushes
   * or not
   * @param docTypes the DOCTYPES used in the resulting document
   */
  public XMLWriter (Writer out, boolean autoFlush, String[] docTypes)
    throws IOException {
      super (out, autoFlush);
      namespaceStack.push("");
      writeXMLHeading (docTypes);
  }

  /**
   * Create an XMLWriter that outputs a standard XML
   * declaration header, including a &lt!DOCTYPE . . . &gt header.
   * The <code>XMLWriter</code> must be pre-informed, via the static
   * <code>setPublicDocType()</code> and <code>setSystemDocType()</code>
   * methods, about all DOCTYPES passed in as
   * parameters.
   * <p>
   * If there is more than one DOCTYPE in the parameter array,
   * the first one will be declared the DOCTYPE, and the others will be
   * declared as external parameter entities. This is the way, for example,
   * that STARTS gets nested within SDLIP.
   * <p>
   * @param out the <code>Writer</code> to wrap the
   * <code>XMLWriter</code> around.
   * @param autoFlush whether the underlying stream automatically flushes
   * or not
   * @param autoIndent whether to automatically indent each element
   * @param autoCR whether to automatically emit a carriage return after
   * each element or value
   * @param isolateAttributes whether each attribute of a printed XML element
   * should be on its own line (good for elements with lots of attributes)
   * @param docTypes the DOCTYPES used in the resulting document
   */
  public XMLWriter (Writer out, boolean autoFlush,
                    boolean autoIndent, boolean autoCR,
                    boolean isolateAttributes, String[] docTypes)
                      throws IOException {
    super (out, autoFlush);
    this.autoIndent = autoIndent;
    this.autoCR = autoCR;
    this.isolateAttributes = isolateAttributes;
    namespaceStack.push("");
    writeXMLHeading (docTypes);
  }


  // -------- WRITING --------
  /**
   * Increase the indentation of the next XML element by one tab.
   * If the <code>autoIndent</code> property is true, this indentation
   * will be printed before each element and value
   */
  public void indent () {
    tabString += "\t";
  }

  /**
   * Decrease the indentation of the next XML element by one tab
   * If the <code>autoIndent</code> property is true, this indentation
   * will be printed before each element and value
   */
  public void unindent () {
    tabString = tabString.substring(0,tabString.length()-1);
  }

  /**
   * Indicate that all future elements will have the given namespace.
   * <code>XMLWriter</code> stores invocations of this method on a stack,
   * so that it is possible to return to a previously-entered namespace
   * using the {@link #exitNamespace() exitNamespace()} method.
   * <b>NOTE:</b><code>XMLWriter</code> does not assign namespaces to
   * attributes, as this is considered tedious.
   * @param namespace the new namespace for future output
   */
  public void enterNamespace (String namespace) {
    namespaceStack.push (namespace);
    namespaceString = namespace;
  }

  /**
   * Return the <code>XMLWriter</code> to the namespace entered during
   * the previous invocation of {@link #enterNamespace(String) enterNamespace()}, or
   * to no namespace if there wasn't one.
   */
  public void exitNamespace () {
    namespaceStack.pop();
    namespaceString = (String) namespaceStack.peek();
    if (namespaceString.equals("")) {
      namespaceString = null;
    }
  }

  /**
   * Forget the history of all namespaces entered, and return to the
   * default namespace.
   */
  public void clearNamespaces () {
    namespaceStack.clear();
    namespaceStack.push ("");
    namespaceString = null;
  }

  /**
   * Tell <code>XMLWriter</code> whether to indent each element and value
   * it outputs. The size of this indentation is controlled by the
   * {@link #indent() indent()} and {@link #unindent() unindent()} methods.
   * @param autoIndent whether to automatically indent or not
   */
  public void setAutoIndent (boolean autoIndent) {
    this.autoIndent = autoIndent;
  }

  /**
   * Tell <code>XMLWriter</code> whether to emit a carriage return after
   * each element and value it outputs
   * @param autoCR whether to emit a carriage return after each element
   * and value outputted
   */
  public void setAutoCR (boolean autoCR) {
    this.autoCR = autoCR;
  }

  /**
   * Tell <code>XMLWriter</code> whether each attribute in a tag it prints
   * should appear on its own line, or not (good for large bunches of
   * attributes)
   * @param isolateAttributes whether or not each attribute gets its own
   * line
   */
  public void setIsolateAttributes (boolean isolateAttributes) {
    this.isolateAttributes = isolateAttributes;
  }

  /**
   * Sets <code>XMLWriter</code> to a state where the <code>autoIndent</code>
   * property is <code>true</code>, <code>autoCR</code> is <code>true</code>,
   * and <code>isolateAttributes</code> is <code>false</code>.
   */
  public void setDefaultFormat () {
    autoIndent = true;
    autoCR = true;
    isolateAttributes = false;
  }

  /**
   * Prints an entire XML element (opening tag, value, and closing tag)
   * all on one line. Such an element has no attributes.
   * @param name the name of the element
   * @param value the value of the element
   */
  public void printEntireElement (String name, String value)
    throws IOException {
      printEntireElement (name, value, true);
  }

  /**
   * Prints an entire XML element (opening tag, value, and closing tag).
   * Such an element has no attributes.
   * @param name the name of the element
   * @param value the value of the element
   * @param onOneLine whether the element should appear all on one line,
   * or just following whatever state the <code>autoCR</code> property is in
   */
  public void printEntireElement (String name, String value, boolean onOneLine)
    throws IOException {
      boolean oldAutoCR     = autoCR;
      boolean oldAutoIndent = autoIndent;
      if (onOneLine) {
        setAutoCR(false);
      }
      printStartElement (name);
      if (onOneLine) {
        setAutoIndent(false);
      }
      print (value);
      printEndElement (name);
      if (onOneLine) {
        setAutoCR (oldAutoCR);
        setAutoIndent (oldAutoIndent);
      }
      if (autoCR) {
        println();
      }
  }

  /**
   * Prints an entire XML element (opening tag, value, and closing tag)
   * all on one line. Such an element has no attributes.
   * @param name the name of the element
   * @param value the value of the element
   */
  public void printEntireElement (String name, int value)
    throws IOException {
      printEntireElement (name, ""+value);
  }

  /**
   * Prints an entire XML element (opening tag, value, and closing tag)
   * all on one line. Such an element has no attributes.
   * @param name the name of the element
   * @param value the value of the element
   */
  public void printEntireElement (String name, double value)
    throws IOException {
      printEntireElement (name, ""+value);
  }

  /**
   * Print only the starting part of an XML element<br>
   * Example: &lt element&gt
   * @param name the name of the element
   */
  public void printStartElement (String name) throws IOException {
    printStartElement (name, false);
  }

  /**
   * Print only the starting part of an XML element, and say whether
   * or not this element has attributes. If it does not, it will
   * appear as:<br>
   * &lt element&gt<br>
   * If it does, it will appear as
   * &lt element<br>
   * thus leaving a place for the attributes
   * @param name the name of the element
   * @param hasAttributes whether this element has attributes
   */
  public void printStartElement (String name, boolean hasAttributes)
    throws IOException {
      if (autoIndent) {
        print (tabString);
      }
      print ("<");
      if (namespaceString != null) {
        print (namespaceString + ":");
      }
      print (name);
      if (!hasAttributes) {
        print (">");
        if (autoCR) {
          println();
        }
      }
      else if (isolateAttributes) {
        println();
      }
  }

  /**
   * Print only the value from inside an element
   * @param value the value
   */
  public void printValue (String value) throws IOException {
    if (autoIndent) {
      print (tabString);
    }
    print (value);
    if (autoCR) {
      println();
    }
  }

  /**
   * Prints the character: &gt.  Used after several attributes
   * have been printed in a starting element. A common mistake
   * is to forget calling this method after printing attributes!
   */
  public void printStartElementClose () throws IOException {
    if (isolateAttributes) {
      print (tabString);
    }
    print (">");
    if (autoCR) {
      println();
    }
  }

  /**
   * Prints the ending part of the element. <br>
   * Example:<br>
   * &lt /element&gt
   * @param name the name of the element
   */
  public void printEndElement (String name) throws IOException {
    if (autoIndent) {
      print (tabString);
    }
    print ("</");
    if (namespaceString != null) {
        print (namespaceString + ":");
    }
    print (name);
    print (">");
    if (autoCR) {
      println();
    }
  }

  /**
   * Prints an element that has no attributes or values.<br>
   * Example:<br>
   * &lt element/&gt
   * @param name the name of the element
   */
  public void printEmptyElement (String name) throws IOException {
    if (autoIndent) {
      print (tabString);
    }
    print ("<");
    if (namespaceString != null) {
        print (namespaceString + ":");
    }
    print (name);
    print ("/>");
    if (autoCR) {
      println();
    }
  }

  /**
   * Prints the following: /&gt. Useful for closing an element
   * that has attributes but no value. A common mistake is to forget
   * to call this after printing attributes!
   */
  public void printEmptyElementClose () throws IOException {
    print ("/>");
    if (autoCR) {
      println();
    }
  }

  /**
   * Inside an element, print the namespace declaration, in the form
   * of:<br>
   * xmlns:<i>name</i>="<i>value</i>"
   * @param name the name of the namespace
   * @param value the value of the namespace
   */
  public void printNamespaceDeclaration (String name, String value)
    throws IOException {
      printAttribute ("xmlns:" + name, value);
  }

  /**
   * Inside an element, print the default namespace declaration, in the form
   * of:<br>
   * xmlns="<i>value</i>"
   * @param value the value of the namespace
   */
  public void printNamespaceDeclaration (String value)
    throws IOException {
      printAttribute ("xmlns", value);
  }

  /**
   * Inside an element, print an attribute, in the form of:<br>
   * <i>name</i>=<i>value</i>
   * @param name the name of the attribute
   * @param value the value of the attribute
   */
  public void printAttribute (String name, String value) throws IOException {
    if (isolateAttributes) {
      print (tabString + "\t");
    }
    print (" ");
    print (name);
    print ("=\"");
    print (value);
    print ("\"");
    if (isolateAttributes) {
      println();
    }
  }

  /**
   * Inside an element, print an attribute, in the form of:<br>
   * <i>name</i>=<i>value</i>
   * @param name the name of the attribute
   * @param value the value of the attribute
   */
  public void printAttribute (String name, int value) throws IOException {
    printAttribute (name, ""+value);
  }

  /**
   * Inside an element, print an attribute, in the form of:<br>
   * <i>name</i>=<i>value</i>
   * @param name the name of the attribute
   * @param value the value of the attribute
   */
  public void printAttribute (String name, double value) throws IOException {
    printAttribute (name, ""+value);
  }

  /**
   * Inside an element, print an attribute, in the form of:<br>
   * <i>name</i>=<i>value</i>
   * @param name the name of the attribute
   * @param value the value of the attribute
   */
  public void printAttribute (String name, boolean value) throws IOException {
    printAttribute (name, ""+value);
  }


  // -------- DOCTYPE MANAGEMENT --------
  /**
   * Notify all <code>XMLWriters</code> of the existence of a PUBLIC
   * DOCTYPE.
   * @param docType the name of the DOCTYPE
   * @param url the URL of the DOCTYPE DTD
   */
  public static void addPublicDocType (String docType, String url) {
    docTypeMap.put (docType, new Record ("PUBLIC", url));
  }

  /**
   * Notify all <code>XMLWriters</code> of the existence of a SYSTEM
   * DOCTYPE.
   * @param docType the name of the DOCTYPE
   * @param url the URL of the DOCTYPE DTD
   */
  public static void addSystemDocType (String docType, String url) {
    docTypeMap.put (docType, new Record ("SYSTEM", url));
  }

  /**
   * If for some reason you must have something appear before the XML
   * heading, override this method with an implementation that writes
   * what you want, and then calls its <code>super</code> version.
   */
  protected void writeXMLHeading (String[] docTypes) throws IOException {
    if (docTypes == null || docTypes.length == 0) {
      return;
    }

    String docType = docTypes[0];
    Record record = (Record) docTypeMap.get (docType);
    if (record == null && !isXSD) {
      throw new IOException ("invalid doctype: " + docType);
    }

    write ("<?xml version=\"");
    write (XML_VERSION);
    write ("\" encoding=\"");
    write (ENCODING);
    write ("\"?>\n");
    if (!isXSD)
    {
    	write ("<!DOCTYPE ");
    	write (docType);
    	write (" ");
    	write (record.type);
    	write (" \"");
    	write (record.url);
    	write ("\"");

    	int len = 0;
    	if ( (len = docTypes.length) > 1 ) {
        	write ("[\n");
        	for (int i = 1 ; i < len ; i++ ) {
              	String entity = docTypes [i];
              	record = (Record) docTypeMap.get (entity);
              	if (record == null) {
                	throw new IOException ("invalid doctype: " + entity);
              	}
              	entity = entity.toUpperCase();
              	String type = record.type;
              	String url  = record.url;
              	write ("\t<!ENTITY % ");
              	write (entity);
              	write (" ");
              	write (type);
              	write (" \"");
              	write (url);
              	write ("\">\n");
              	write ("\t%");
              	write (entity);
            	write (";\n");
        	}
        	write ("]");
    	}
   	 write (">\n");
  	}
  }

  public String GetDocTypeMapEntry(String entity) throws IOException 
  {
	Record record = (Record) docTypeMap.get (entity);
	if (record == null) {
		throw new IOException ("invalid doctype: " + entity);
	}
  	return record.url;
  }

  private static class Record {
    String type;
    String url;

    Record (String type, String url) {
      this.type = type;
      this.url = url;
    }
  }
}
