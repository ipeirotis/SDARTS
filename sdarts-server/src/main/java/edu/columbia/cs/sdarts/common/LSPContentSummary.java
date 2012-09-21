
package edu.columbia.cs.sdarts.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.columbia.cs.sdarts.util.XMLWriter;

/**
 * Represents an "scontent-summary", as defined in the STARTS 1.0 spec.
 * Currently, because of the large size of content summariese and the
 * limitations of the SDLIP transport layer, content summaries are not
 * return by the {@link edu.columbia.cs.sdarts.frontend.FrontEndLSP FrontEndLSP}.
 * Rather, in general, their text XML representations are made available
 * via standard HTTP. However, an object version of this header is still
 * needed for content-summary creation and storage on the back-end, and
 * may be available via the <code>FrontEndLSP</code> in the future.
 * <p>
 * The back end developer instantiates this object and returns it from
 * the
 * {@link edu.columbia.cs.sdarts.backend.BackEndLSP#getContentSummary() getContentSummary()}
 * method of {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP}.
 * <p>
 * See the STARTS Specification for the meanings of all the properties
 * stored in this class.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LSPContentSummary extends LSPObject {
    // ------------ FIELDS ------------
    /** The version of STARTS: "Starts 1.0" */
    public static final String version = "Starts 1.0";

    // used as a key in field frequency mapping when field
    // is unspecified - a string that is 99.99% guaranteed not
    // to be a real field name anywhere
    private static final String UNSPECIFIED =
      new String ("LSPOBJECTADAPTERUNSPECIFIED");

    // the number of field-frequency-records that can be batched
    // in memory before being written to I/O. Adjust this for
    // performance if you like
    private static final int BATCH_SIZE = 1000;

    private Map     fieldFreqInfo;
    private boolean stemming;
    private boolean stopWords;
    private boolean caseSensitive;
    private boolean fields;
    private int     numDocs;


    /**
     * Create an <code>LSPContentSummary</code>
     * @param stemming whether the underlying collection supported stemming
     * @param stopWords whether the underyling collection supports stop-words
     * @param caseSensitive whether the underyling collection supports
     * case-sensitive queries
     * @param fields whether field names appear with the terms in a content
     * summary
     * @param numDocs the number of documents in the underlying collection
     */
    public LSPContentSummary (boolean stemming, boolean stopWords,
			      boolean caseSensitive,
			      boolean fields, int numDocs) {
	       this.stemming = stemming;
	       this.stopWords = stopWords;
	       this.caseSensitive = caseSensitive;
	       this.fields = fields;
	       this.numDocs = numDocs;
	       fieldFreqInfo = new HashMap();
    }

    /**
     * Add a field frequency information entry to the content summary,
     * specifying information about
     * term frequency (number of times a term appears throughout the collection)
     * @param field the field where this term appears (can be <code>null</code>
     * if unknown)
     * @param term the term
     * @param termFrequency term frequency
     */
    public void addTermFieldFreqInfo (LSPField field, LSPTerm term, int termFrequency) {
	       addFieldFreqImpl (field, term, new TermFreq (termFrequency));
    }

    /**
     * Add a field frequency information entry to the content summary,
     * specifying information about
     * doc frequency (number of documents that contain the term)
     * @param field the field where this term appears (can be <code>null<code>
     * if unknown)
     * @param term the term
     * @param docFrequency doc frequency
     */
    public void addDocFieldFreqInfo (LSPField field, LSPTerm term, int docFrequency) {
	       addFieldFreqImpl (field, term, new DocFreq (docFrequency));
    }

    /**
     * Add a field frequency information entry to the content summary,
     * specifying information about
     * term frequency (number of times a term appears throughout the collection)
     * and doc frequency (number of documents that contain the term)
     * @param field the field where this term appears (can be <code>null<code>
     * if unknown)
     * @param term the term
     * @param termFrequency term frequency
     * @param docFrequency doc frequency
     */
    public void addTermDocFieldFreqInfo (LSPField field, LSPTerm term,
					 int termFrequency,
					 int docFrequency) {
        addFieldFreqImpl (field, term, new TermDocFreq (termFrequency, docFrequency));
    }

    private void addFieldFreqImpl (LSPField field, LSPTerm term, LSPObject obj) {
      Map terms = null;
      if (field == null) {
        terms = (Map) fieldFreqInfo.get (UNSPECIFIED);
      }
      else {
        terms = (Map) fieldFreqInfo.get (field);
      }

	  if (terms == null) {
         terms = new HashMap();
         if (field == null) {
	      fieldFreqInfo.put (UNSPECIFIED, terms);
         }
         else {
          fieldFreqInfo.put (field, terms);
         }
      }

	  terms.put (term, obj);
    }

    /**
     * Whether the collection supports stemming
     * @return whether the collection supports stemming
     */
    public boolean getStemming() {
      return stemming;
    }

    /**
     * Whether the collection supports stop-words
     * @return whether the collection supports stop-words
     */
    public boolean getStopWords() {
      return stopWords;
    }

    /**
     * Whether the collection supports case-sensitive queries
     * @return whether the collection supports case-sensitive queries
     */
    public boolean getCaseSensitive() {
      return caseSensitive;
    }

    /**
     * Whether field names appear in the frequency entries of the content
     * summary
     * @return Whether field names appear in the frequency entries of the
     * content summary
     */
    public boolean getFields() {
      return fields;
    }

    /**
     * Return the number of documents in the collection
     * @return the number of documents in the collection
     */
    public int getNumDocs() {
      return numDocs;
    }

    /**
     * Generates an XML representation of <code>LSPContentSummary</code>,
     * and writes it directly to a <code>Writer</code>. This method is
     * safer to call because <code>LSPContentSummaries</code> tend to
     * be huge - best to write it directly to disk or the network.
     * @param depth the depth in a nested XML string that this element
     * appears
     * @writer the writer to write the XML to
     */
    public void toXML (XMLWriter writer) throws IOException {
      writer.setDefaultFormat();
      writer.setIsolateAttributes(true);
      writer.enterNamespace(STARTS.NAMESPACE_NAME);
      writer.printStartElement ("scontent-summary", true);
      writer.printNamespaceDeclaration
        (STARTS.NAMESPACE_NAME, STARTS.NAMESPACE_VALUE);
		writer.printAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
	  writer.printAttribute("xsi:schemaLocation", STARTS.NAMESPACE_VALUE + " " + writer.GetDocTypeMapEntry(STARTS.NAMESPACE_NAME + ":smeta-attributes"));
      writer.printAttribute ("version", version);
      writer.printAttribute ("stemming", stemming);
      writer.printAttribute ("stopwords", stopWords);
      writer.printAttribute ("case-sensitive", caseSensitive);
      writer.printAttribute ("fields", fields);
      writer.printAttribute ("numdocs", numDocs);
      writer.printStartElementClose();
      writer.setIsolateAttributes (false);
      writer.flush();

      writer.indent();
	  for (Iterator it1 = fieldFreqInfo.keySet().iterator() ; it1.hasNext() ; ) {
        Object obj = it1.next();
        writer.printStartElement ("field-freq-info");
        writer.indent();
        if (!(obj instanceof String)) {
           LSPField field = (LSPField) obj;
	       field.toXML (writer);
        }
        int iterations = 0;
	    Map terms = (Map) fieldFreqInfo.get (obj);
	    for (Iterator it2 = terms.keySet().iterator() ; it2.hasNext() ; ) {
	    	LSPTerm term = (LSPTerm) it2.next();
		    term.toXML (writer);
		    LSPObject obj1 = (LSPObject) terms.get (term);
		    obj1.toXML (writer);
            if (++iterations == BATCH_SIZE) {
              writer.flush();
              iterations = 0;
            }
	    }
        writer.unindent();
        writer.printEndElement("field-freq-info");
        writer.flush();
      }
      writer.unindent();
      writer.printEndElement("scontent-summary");
      writer.flush();
      writer.exitNamespace();
    }


    // ------------ INNER CLASSES ------------
    private class TermFreq extends LSPObject {
      int     freq;

      public TermFreq (int freq) {
	    this.freq = freq;
      }

	  public void toXML (XMLWriter writer) throws IOException {
        writer.setDefaultFormat();
        writer.printEntireElement("term-freq", freq);
      }
    }

      private class DocFreq extends LSPObject {
    	int     freq;

	    public DocFreq (int freq) {
	           this.freq = freq;
	    }

	    public void toXML (XMLWriter writer) throws IOException {
          writer.setDefaultFormat();
          writer.printEntireElement("doc-freq", freq);
        }
       }

       private class TermDocFreq extends LSPObject {
	       TermFreq termFreq;
	       DocFreq  docFreq;

	       public TermDocFreq (int termFreq, int docFreq) {
	         this.termFreq = new TermFreq (termFreq);
	         this.docFreq  = new DocFreq (docFreq);
           }

           public void toXML (XMLWriter writer) throws IOException {
             termFreq.toXML (writer);
             docFreq.toXML (writer);
          }
        }
}

