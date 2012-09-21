

package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;


/**
 * Represents the "smeta-attributes" element of STARTS 1.0. This is the
 * object with which the back end responds when queried for its meta-
 * attributes. See the STARTS 1.0 specification for the meanings of all
 * properties stored in this class.
 * <p>
 * The back end developer instantiates this object and returns it from
 * the
 * {@link edu.columbia.cs.sdarts.backend.BackEndLSP#getMetaAttributes() getMetaAttributes()}
 * method of {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP}.
 * <p>
 * There are getter methods for reading all the attributes after
 * instantiation, but they are rarely used; the framework reads this
 * object using the <code>toXML()</code> method. The
 * {@link edu.columbia.cs.sdarts.backend.StandardQueryProcessor StandardQueryProcessor} does
 * use some of them.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LSPMetaAttributes extends LSPObject {
  // ------------ FIELDS ------------
  /** The version of STARTS: "Starts 1.0" */
  public static final String version = "Starts 1.0";
  private String source;
  private LSPField[] fieldsSupported;
  private LSPModifier[] modifiersSupported;
  private LSPObject[] fieldModifierCombinations;
  private String queryPartsSupported;
  private double[] scoreRange;
  private String rankingAlgorithmId;
  private String[] tokenizerIdList;
  private String sampleDatabaseResults;
  private String[] stopWordList;
  private boolean turnOffStopWords;
  private LSPMetaAttributeSet metaAttributeSet;
  private String classification;

  /**
   * See the STARTS 1.0 specification for more information about the
   * meanings of these parameters.<p>
   * @param source cannot be <code>null</code>
   * @param fieldsSupported cannot be <code>null</code>
   * @param modifiersSupported cannot be <code>null</code>
   * @param fieldModifierCombinations <i>optional</i>. A <code>List</code>
   * where every even element is a supported <code>LSPField</code> and every
   * odd element is a supported <code>LSPModifier</code> that can be used
   * with the preceding <code>LSPField</code>.
   * @param queryPartsSupported <i>optional</i>
   * @param scoreRange cannot be <code>null</code>. A <code>double</code>
   * array of length 2. The first element is minimum possible score, and the
   * second is maximum.
   * @param rankingAlgorithmId cannot be <code>null</code>
   * @param tokenizerIdList <i>optional</i>
   * @param sampleDatabaseResults cannot be <code>null</code>
   * @param stopWordList cannot be <code>null</code>
   * @param turnOffStopWords <code>true</code> or <code>false</code>.
   * @param metaAttributeSet cannot be <code>null</code>. See the
   * <code>LSPMetaAttributeSet</code> class.
   */
  public LSPMetaAttributes (String source, LSPField[] fieldsSupported,
                            LSPModifier[] modifiersSupported,
                            LSPObject[] fieldModifierCombinations,
                            String queryPartsSupported,
                            double[] scoreRange,
                            String rankingAlgorithmId,
                            String[] tokenizerIdList,
                            String sampleDatabaseResults,
                            String[] stopWordList,
                            boolean turnOffStopWords,
                            LSPMetaAttributeSet metaAttributeSet,
                            String classification) {
    if (source == null || fieldsSupported == null ||
        modifiersSupported == null ||
        scoreRange == null ||
        scoreRange.length != 2 || rankingAlgorithmId == null ||
        metaAttributeSet == null) {
      throw new InstantiationError ("missing or invalid parameter");
    }

    this.source = source;
    this.fieldsSupported = fieldsSupported;
    this.modifiersSupported = modifiersSupported;
    if (fieldModifierCombinations != null) {
      int len = fieldModifierCombinations.length;
      for (int i = 0 ; i < len ; i++) {
        if ( i % 2 == 0) {
          if ( !(fieldModifierCombinations[i] instanceof LSPField) ) {
            throw new InstantiationError ("bad fieldmodifiercombination");
          }
        }
        else {
          if ( !(fieldModifierCombinations[i] instanceof LSPModifier) ) {
            throw new InstantiationError ("bad fieldmodifiercombination");
          }
        }
      }
      this.fieldModifierCombinations = fieldModifierCombinations;
    }

    this.queryPartsSupported = queryPartsSupported;
    this.scoreRange = scoreRange;
    this.rankingAlgorithmId = rankingAlgorithmId;
    this.tokenizerIdList = tokenizerIdList;
    this.sampleDatabaseResults = sampleDatabaseResults;
    int len = stopWordList.length;
    this.stopWordList = stopWordList;
    this.turnOffStopWords = turnOffStopWords;
    this.metaAttributeSet = metaAttributeSet;
    this.classification = classification;
  }

  /**
   * What "source" (aka subcollection, backendlsp, etc.) this meta-attributes
   * object describes. This should be the same as the result of the
   * {@link edu.columbia.cs.sdarts.backend.BackEndLSP#getName() getName()} method of
   * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSP}.
   * @return the source name
   */
  public String getSource () {
    return source;
  }
  
  public String getClassification () {
	return classification;
  }
	
  public void setClassification(String val) {
	this.classification = val;
  }

  /**
   * What fields are supported in the underlying collection
   * @return what fields are supported in the underlying collection
   */
  public LSPField[] getFieldsSupported () {
    return fieldsSupported;
  }

  /**
   * What modifiers are supported in the underlying collection
   * @return what modifiers are supported in the underlying collection
   */
  public LSPModifier[] getModifiersSupported () {
    return modifiersSupported;
  }

  /**
   * What field/modifier combinations are supported in the underlying collection
   * @return an <code>Object[]</code> where every even element is a
   * supported <code>LSPField</code> and every odd element is a
   * supported <code>LSPModifier</code> that can be used with the preceding
   * <code>LSPField</code>.
   */
  public LSPObject[] getFieldModifierCombinations () {
    return fieldModifierCombinations;
  }

  /**
   * What query parts are supported in the underlying collection.
   * According the STARTS spec, this is either "R", "F", or "RF".
   * "R" is for ranking and "F" is for filter.
   * @return what query parts are supported in the underlying collection
   */
  public String getQueryPartsSupported () {
    return queryPartsSupported;
  }

  /**
   * What is the score range of the underlying collection
   * @return a <code>double</code> array of length 2. The first element
   * is minimum possible score, and the second is maximum.
   */
  public double[] getScoreRange () {
    return scoreRange;
  }

  /**
   * What ranking algorithm is used by the underlying collection
   * @return what ranking algorithm is used by the underlying collection
   */
  public String getRankingAlgorithmId () {
    return rankingAlgorithmId;
  }

  /**
   * What tokenizers are used by the underlying collection
   * @return what tokenizers are used by the underlying collection
   */
  public String[] getTokenizerIdList () {
    return tokenizerIdList;
  }

  /**
   * Returns a URL for a place to find sample results from a query to
   * the underlying collection
   * @return a URL for a place to find sample results from a query to
   * the underlying collection
   */
  public String getSampleDatabaseResults () {
    return sampleDatabaseResults;
  }

  /**
   * What stop words are used in the underlying collection
   * @return what stop words are used in the underlying collection
   */
  public String[] getStopWordList () {
    return stopWordList;
  }

  /**
   * Whether the stop words can be turned off in a search of the
   * underlying collection
   * @return whether the stop words can be turned off in a search of the
   * underlying collection
   */
  public boolean turnOffStopWords () {
    return turnOffStopWords;
  }

  /**
   * Returns the <code>LSPMetaAttributeSet</code> contained within; this
   * is an object that further define the back-end collection
   * @return the <code>LSPMetaAttributeSet</code> contained within
   */
  public LSPMetaAttributeSet getMetaAttributeSet () {
    return metaAttributeSet;
  }

  /**
   * @return the version of STARTS protocol. Set to <code>Starts 1.0</code>
   */
  public String getVersion () {
    return version;
  }


  public void toXML (XMLWriter writer) throws IOException {
    writer.setDefaultFormat();
    writer.enterNamespace(STARTS.NAMESPACE_NAME);
    writer.printStartElement("smeta-attributes", true);
    writer.printNamespaceDeclaration(STARTS.NAMESPACE_NAME, STARTS.NAMESPACE_VALUE);
    writer.printAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    writer.printAttribute("xsi:schemaLocation", STARTS.NAMESPACE_VALUE + " " + writer.GetDocTypeMapEntry(STARTS.NAMESPACE_NAME + ":smeta-attributes"));
    writer.printAttribute ("version", version);
    writer.printStartElementClose();
    writer.indent();
    writer.printEntireElement("source", source);
	writer.printEntireElement("classification", classification);
    writer.printStartElement ("fields-supported");
    writer.indent();
    int len = fieldsSupported.length;
    for (int i = 0 ; i < len ; i++) {
      fieldsSupported[i].toXML (writer);
    }
    writer.enterNamespace(STARTS.NAMESPACE_NAME);
    writer.unindent();
    writer.printEndElement ("fields-supported");
    writer.printStartElement ("modifiers-supported");
    writer.indent();
    len = modifiersSupported.length;
    for (int i = 0 ; i < modifiersSupported.length ; i++) {
      modifiersSupported[i].toXML (writer);
    }
    writer.enterNamespace(STARTS.NAMESPACE_NAME);
    writer.unindent();
    writer.printEndElement ("modifiers-supported");
    if (fieldModifierCombinations != null && fieldModifierCombinations.length > 0) {
      len = fieldModifierCombinations.length;
      writer.printStartElement("field-modifier-combinations");
      writer.indent();
      for (int i = 0 ; i < len ; i++) {
        LSPObject obj = fieldModifierCombinations[i];
        obj.toXML (writer);
      }
      writer.enterNamespace(STARTS.NAMESPACE_NAME);
      writer.unindent();
      writer.printEndElement("field-modifier-combinations");
    }
    
    writer.printStartElement("query-parts-supported", true);
    writer.printAttribute ("parts", queryPartsSupported);
    writer.printEmptyElementClose();
    writer.printStartElement ("score-range", true);
    writer.printAttribute ("lower", scoreRange[0]);
    writer.printAttribute ("upper", scoreRange[1]);
    writer.printEmptyElementClose();
    writer.printEntireElement ("ranking-algorithm-id", rankingAlgorithmId);
	writer.printEntireElement ("classification", classification);
    if (tokenizerIdList != null && tokenizerIdList.length > 0) {
      writer.printStartElement("tokenizer-id-list");
      writer.indent();
      len = tokenizerIdList.length;
      for (int i = 0 ; i < len ; i++) {
        writer.printEntireElement ("tokenizer",tokenizerIdList[i]);
      }
      writer.unindent();
      writer.printEndElement ("tokenizer-id-list");
    }
    writer.printEntireElement ("sample-database-results",
                                 sampleDatabaseResults);
	if (stopWordList != null && stopWordList.length > 0)
	{
	      writer.printStartElement ("stop-word-list");
	      len = stopWordList.length;
	      writer.indent();
	      for (int i = 0 ; i < len ; i++) {
	        writer.printEntireElement ("word", stopWordList[i]);
	      }
	      writer.unindent();
	      writer.printEndElement ("stop-word-list");
	}
    
	writer.printStartElement ("turnoff-stop-words", true);
	writer.printAttribute("value", turnOffStopWords);
	writer.printEmptyElementClose();
	metaAttributeSet.toXML (writer);
	writer.enterNamespace(STARTS.NAMESPACE_NAME);
	writer.unindent();
	writer.printEndElement("smeta-attributes");
 
    writer.exitNamespace();
    writer.flush();
  }
}
