

package edu.columbia.cs.sdarts.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.columbia.cs.sdarts.util.XMLWriter;


/**
 * Represents the "doc" element as defined in the STARTS 1.0 specification.
 * This encapsulates one document in a response to a query. It is generated
 * on the back end and includes the following information:
 * <ul>
 * <li> Raw score of document. Not normalized. (<code>double</code>)
 * <li> Size of document in KB(<code>int</code>)
 * <li> Document count - the number of tokens in the document (<code>int</code>)
 * <li> Sources where this document appears
 * ({@link edu.columbia.cs.sdarts.common.LSPSource LSPSource})
 * <li> Field/value pairings that describe the document, for example "title,
 * Neuromancer." (<code>LSPField</code>, <code>String</code>)
 * <li> Term statistics. These are the terms used in the rankings of the query.
 * Statistics include number of times the term apepars within document,
 * term weight within document, and number of documents in the source that
 * contain the term.
 * (<code>LSPTerm</code>, <code>double</code>, <code>double</code>, <code>int
 * </code>)
 * </ul>
 * The back end developer typically instantiates one <code>LSPDoc</code> for
 * each record in the response from the underlying collection, and then
 * bundles all the doc objects into an
 * {@link edu.columbia.cs.sdarts.common.LSPResults LSPResults}
 * object.
 * <p>
 * There are getter methods for reading what is inside the doc object,
 * but in general these are not used. They are cumbersome and use some
 * public inner classes. This is because mostly, the framework reads this
 * object using the standard <code>toXML()</code> method.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LSPDoc extends LSPObject {
  // ------------ FIELDS ------------
  /** The version of STARTS: "Starts 1.0" */
  public final String version = "Starts 1.0";
  private List   sources      = new ArrayList();
  private List   fieldValues  = new ArrayList();
  private List   termStats    = new ArrayList();
  private double rawScore;
  private int docSize;
  private int docCount;



  // ------------ METHODS ------------
  // -------- CONSTRUCTOR --------
  /**
   * Creates a new, empty instance.
   */
  public LSPDoc() {}

  /**
   * Add a source to the list of sources where this document appears
   * @param source the source to add
   */
  public void addSource (LSPSource source) {
    sources.add (source);
  }

  /**
   * Add a field/value pairing that describes this document in some way
   * @param field the field (example: <code>new LSPField (LSPField.TITLE)
   * </code>)
   * @param value the value (example: <code>Neuromancer</code>)
   */
  public void addFieldValue (LSPField field, String fieldValue) {
    FieldValue fv = new FieldValue (field, fieldValue);
    fieldValues.add (fv);
  }

  /**
   * Remove a field/value pairing. This is sometimes needed in cases
   * where an LSPDoc is created from some underlying collection before
   * the "answer-fields" criterion can be applied. When that happens,
   * the criterion is applied during post-processing, and sometimes
   * field/value pairings have to be removed.
   * @param field the field in the field/value pairing to be removed
   */
  public void removeFieldValue (LSPField field) {
    for (Iterator it = fieldValues.iterator() ; it.hasNext() ; ) {
      FieldValue fv = (FieldValue) it.next();
      if (fv.getField().equals(field)) {
        it.remove();
        break;
      }
    }
  }

  /**
   * Add a term statistic. Typically, these terms come from the ranking
   * that was used in the query.
   * @param term the term
   * @param termFreq the number of times the term appears within the document
   * @param termWeight the weight of the term within the document
   * @param docFreq the number of documents in the source that have this term
   */
  public void addTermStat (LSPTerm term, int termFreq,
                           double termWeight, int docFreq) {
    TermStat ts = new TermStat (term, termFreq, termWeight, docFreq);
    termStats.add (ts);
  }

  /**
   * Set the un-normalized score of the document. This is the score that
   * the original collection assigned to it.
   * @param rawScore the score
   */
  public void setRawScore(double rawScore) {
    this.rawScore = rawScore;
  }

  /**
   * Set the size of the document, in kilobytes.
   * @param docSize the size
   */
  public void setDocSize(int docSize) {
    this.docSize = docSize;
  }

  /**
   * Set the number of tokens that appear in the document
   * @param docCount the number of tokens
   */
  public void setDocCount(int docCount) {
    this.docCount = docCount;
  }

  public void toXML (XMLWriter writer) throws IOException {
    writer.setDefaultFormat();
    writer.enterNamespace(STARTS.NAMESPACE_NAME);
    writer.printStartElement("sqrdocument", true);
    writer.printAttribute ("version", version);
    writer.printStartElementClose();
    writer.indent();
    writer.printEntireElement ("rawscore", rawScore);
    for (Iterator it = sources.iterator() ; it.hasNext() ; ) {
      LSPSource source = (LSPSource) it.next();
      source.toXML(writer);
    }
    for (Iterator it = fieldValues.iterator() ; it.hasNext() ; ) {
      FieldValue fv = (FieldValue) it.next();
      fv.toXML (writer);
    }
    writer.printStartElement("term-stats-list");
    for (Iterator it = termStats.iterator() ; it.hasNext() ; ) {
      TermStat ts = (TermStat) it.next();
      ts.toXML(writer);
    }
    writer.printEndElement ("term-stats-list");
    writer.printEntireElement ("docsize", docSize);
    writer.printEntireElement ("doccount", docCount);
    writer.unindent();
    writer.printEndElement ("sqrdocument");
    writer.exitNamespace();
  }

  /**
   * @return the sources this document appears in
   */
  public LSPSource[] getSources () {
    return (LSPSource[]) sources.toArray (new LSPSource [0]);
  }

  /**
   * @return the field/value pairings describing this document
   */
  public FieldValue[] getFieldValues () {
    return (FieldValue[]) fieldValues.toArray (new FieldValue[0]);
  }

  /**
   * Gets the value from a particular field name
   * @param fieldName (use one from {@link edu.columbia.cs.sdarts.common.FieldNames FieldNames})
   * @return value the value for that field
   */
  public String getValue (String fieldName) {
    if (fieldValues == null) {
      return null;
    }
    int len = fieldValues.size();
    for (int i = 0 ; i < len ; i++) {
      FieldValue fv = (FieldValue) fieldValues.get(i);
      if (fv.getField().getName().equals(fieldName)) {
        return fv.getValue().getValue();
      }
    }
    return null;
  }

  /**
   * In general, this is not used, but the data is accessible.
   * @return the term statistics describing this document
   */
  public TermStat[] getTermStats () {
    return (TermStat[]) termStats.toArray (new TermStat[0]);
  }


  /**
   * @return the raw score of the document
   */
  public double getRawScore() {
    return rawScore;
  }

  /**
   * @return the size in KB of the document
   */
  public int getDocSize() {
    return docSize;
  }

  /**
   * @return the number of tokens in the document
   */
  public int getDocCount() {
    return docCount;
  }


  public class FieldValue {
    /**
     * An inner class of <code>LSPDoc</code> that contains one field/value
     * pairing describing the document. Avoid using this class, it is only
     * used for the programmatic interface onto the values in the
     * <code>LSPDoc</code>. In general, these values are read via the
     * doc's <code>toXML()</code> method.
     */
    private LSPField field;
    private String fieldValue;

    private FieldValue (LSPField field, String fieldValue) {
      this.field = field;
      this.fieldValue = fieldValue;
    }

    public LSPField getField () {
      return field;
    }

    public LSPValue getValue () {
      return new LSPValue (fieldValue);
    }

    public void toXML (XMLWriter writer) throws IOException {
      writer.indent();
      writer.printStartElement("doc-term");
      writer.indent();
      field.toXML (writer);
      writer.unindent();
      writer.indent();
      writer.printEntireElement ("value", fieldValue);
      writer.unindent();
      writer.printEndElement ("doc-term");
      writer.unindent();
    }
  }

  public class TermStat {
    /**
     * An inner class of <code>LSPDoc</code> that contains one term statistic
     * describing the document. Avoid using this class, it is only
     * used for the programmatic interface onto the values in the
     * <code>LSPDoc</code>. In general, these values are read via the
     * doc's <code>toXML()</code> method.
     */
    private LSPTerm term;
    private int    termFreq;
    private double termWeight;
    private int    docFreq;

    private TermStat (LSPTerm term, int termFreq,
		      double termWeight, int docFreq) {
      this.term = term;
      this.termFreq = termFreq;
      this.termWeight = termWeight;
      this.docFreq  = docFreq;
    }

    public LSPTerm getTerm () {
      return term;
    }

    public double getTermFreq () {
      return termFreq;
    }

    public double getTermWeight () {
      return termWeight;
    }

    public int getDocFreq () {
      return docFreq;
    }

    public void toXML (XMLWriter writer) throws IOException {
      writer.indent();
      term.toXML (writer);
      writer.printEntireElement ("term-freq", termFreq);
      writer.printEntireElement ("term-weight", termWeight);
      writer.printEntireElement ("doc-freq", docFreq);
      writer.unindent();
    }
  }
}
