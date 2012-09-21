package edu.columbia.cs.sdarts.common;

import java.io.IOException;
import java.io.Reader;

import org.xml.sax.AttributeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.util.SDARTSHandlerBase;

/**
 * Creates an {@link edu.columbia.cs.sdarts.common.LSPContentSummary LSPContentSummary}.
 * Its concrete method specifies how to read in STARTS XML to create
 * the content summary, while its abstract methods specify how a
 * content summary ought to be saved or loaded.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public abstract class LSPContentSummaryBuilder {
  /**
   * An implementation of this method should load an
   * <code>LSPContentSummary</code> from some kind of persistent storage
   * @param backEndLSPName the name of the
   * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} to whom the
   * <code>LSPContentSummary</code> belongs
   * @return an <code>LSPContentSummary</code> that has been loaded
   */
  public abstract LSPContentSummary load (String backEndLSPName)
    throws BackEndException;

  /**
   * An implementation of this method should save an
   * <code>LSPContentSummary</code> to some kind of persistent storage
   * @param backEndLSPName the name of the
   * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP} to whom the
   * <code>LSPContentSummary</code> belongs
   * @param contentSummary the <code>LSPContentSummary</code> to be saved
   */
  public abstract void save (String backEndLSPName, LSPContentSummary contentSummary)
    throws BackEndException;

  /**
   * Will create an <code>LSPContentSummary</code> by reading in STARTS
   * XML from the specified <code>Reader</code>
   * @param reader the <code>Reader</code> from which to read the STARTS
   * XML to build to <code>LSPContentSummary</code>
   */
  public LSPContentSummary fromXML (Reader reader) throws BackEndException {
    try {
      LCSBHandler handler = new LCSBHandler ();
      LSPContentSummary contentSummary = handler.parse (reader);
      return contentSummary;
    }
    catch (Exception e) {
      throw new BackEndException (e.getMessage());
    }
  }



  // ------------ INNER CLASSES ------------
  private class LCSBHandler extends SDARTSHandlerBase {
    // ------------ FIELDS ------------
    // -------- CONSTANTS --------
    private static final int NONE              = -1;
    private static final int SCONTENT_SUMMARY  = 0;
    private static final int FIELD_FREQ_INFO   = 1;
    private static final int FIELD             = 2;
    private static final int TERM              = 3;
    private static final int VALUE             = 4;
    private static final int TERMFREQ          = 5;
    private static final int DOCFREQ           = 6;


    // -------- PARSING --------
    private int state;
    private LSPContentSummary contentSummary;
    private LSPField field;
    private LSPTerm term;
    private int termFreq = -1;
    private int docFreq  = -1;



    // ------------ METHODS ------------
    public LCSBHandler () throws SAXException, javax.xml.parsers.ParserConfigurationException 
    {
    }

    public LSPContentSummary parse (Reader reader) throws IOException, SAXException 
    {
	  super.parse (new InputSource (reader));
      return contentSummary;
    }

    public void startElement (String name, AttributeList attrs)
	throws SAXException {
        name = super.removeNamespace(name);
        if (name.equals ("scontent-summary")) {
          state = SCONTENT_SUMMARY;
          boolean stemming =
            new Boolean (attrs.getValue ("stemming")).booleanValue();
          boolean stopWords =
            new Boolean (attrs.getValue ("stopwords")).booleanValue();
          boolean caseSensitive =
            new Boolean (attrs.getValue ("case-sensitive")).booleanValue();
          boolean fields =
            new Boolean (attrs.getValue ("fields")).booleanValue();
          int numDocs =
            new Integer (attrs.getValue ("numdocs")).intValue();
          contentSummary =
            new LSPContentSummary (stemming, stopWords, caseSensitive,
                                   fields, numDocs);
        }
        else if (name.equals ("field-freq-info")) {
          state = FIELD_FREQ_INFO;
        }
        else if (name.equals ("field")) {
          state = FIELD;
          String fieldName = attrs.getValue ("name");
          field = new LSPField (fieldName);
        }
        else if (name.equals ("term")) {
          state = TERM;
          if (termFreq != -1 && docFreq != -1) {
              contentSummary.
                addTermDocFieldFreqInfo (field, term, termFreq, docFreq);
              term = null;
              termFreq = -1;
              docFreq = -1;
            }
            else if (termFreq != -1) {
              contentSummary.addTermFieldFreqInfo (field, term, termFreq);
              term = null;
              termFreq = -1;
            }
            else if (docFreq != -1) {
              contentSummary.addDocFieldFreqInfo(field, term, docFreq);
              term = null;
              docFreq = -1;
            }
        }
        else if (name.equals ("value")) {
          state = VALUE;
        }
        else if (name.equals ("term-freq")) {
          state = TERMFREQ;
        }
        else if (name.equals ("doc-freq")) {
          state = DOCFREQ;
        }
        else {
          processException ("unknown element: " + name);
        }
    }

    public void characters (char[] ch, int start, int length)
	throws SAXException {
        String value = new String (ch, start, length);

        switch (state) {
          case SCONTENT_SUMMARY:
          case FIELD:
          case FIELD_FREQ_INFO:
          case TERM:
            processException ("invalid value");
          break;

          case VALUE:
            term = new LSPTerm (null, null, value);
          break;

          case TERMFREQ:
            termFreq = new Integer (value).intValue();
          break;

          case DOCFREQ:
            docFreq = new Integer (value).intValue();
          break;
        }
    }

    public void endElement (String name) throws SAXException {
        switch (state) {
          case SCONTENT_SUMMARY:
            state = NONE;
          break;

          case FIELD:
            state = FIELD_FREQ_INFO;
          break;

          case FIELD_FREQ_INFO:
            state = SCONTENT_SUMMARY;
            field = null;
            if (termFreq != -1 && docFreq != -1) {
              contentSummary.
                addTermDocFieldFreqInfo (field, term, termFreq, docFreq);
            }
            else if (termFreq != -1) {
              contentSummary.addTermFieldFreqInfo (field, term, termFreq);
            }
            else if (docFreq != -1) {
              contentSummary.addDocFieldFreqInfo(field, term, docFreq);
            }
            else {
              processException ("bad fieldfreq state");
            }
            term = null;
            termFreq = -1;
            docFreq = -1;
          break;

          case TERM:
          case TERMFREQ:
          case DOCFREQ:
            state = FIELD_FREQ_INFO;
          break;

          case VALUE:
            state = TERM;
          break;
        }
      }
    }
}