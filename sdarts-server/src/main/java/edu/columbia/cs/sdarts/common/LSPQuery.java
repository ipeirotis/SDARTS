
package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;


/**
 * Represents an "squery", as defined by the STARTS 1.0 specification.
 * The main query object, generated on the front end and sent to the back
 * end. Contains all the criteria needed for a search. The back end responds
 * to an <code>LSPQuery</code> with an
 * {@link edu.columbia.cs.sdarts.common.LSPResults LSPResults}
 * <p>
 * Typically, a back-end developer calls the various accessor methods on this
 * query object to find out how to set up the query to the wrapped collection.
 * Important elements of an <code>LSPQuery</code>, following the STARTS 1.0
 * specification, are:<p>
 * <ul>
 * <li>Filter ({@link edu.columbia.cs.sdarts.common.LSPFilter LSPFilter}) - see the STARTS
 * specification.
 * <li>Ranking ({@link edu.columbia.cs.sdarts.common.LSPRanking LSPRanking}) - see the STARTS
 * specification.
 * <li>Source ({@link edu.columbia.cs.sdarts.common.LSPSource LSPSource[]}) - see the STARTS
 * specification; note that each "source" corresponds to the name of
 * a <code>BackEndLSP</code> to query, though by the time the query reaches
 * a given <code>BackEndLSP</code>, this of course does not matter.
 * <li>Fields to appear in answer ({@link edu.columbia.cs.sdarts.common.LSPField LSPField[]})
 * <li>Fields to sort by ({@link edu.columbia.cs.sdarts.common.LSPSortByField LSPSortByField[]})
 * <li>Whether to drop stop words (<code>dropStop</code>)
 * <li>Minimum document score (<code>minDocScore</code>)
 * <li>Maximum number of documents to return (<code>maxDocs</code>)
 * </ul>
 * The back-end developer may need
 * to modify the <code>LSPQuery</code> to conform to what filter and ranking
 * the underlying collection can actually handle. See
 * {@link edu.columbia.cs.sdarts.backend.QueryProcessor} for more information on how and
 * why this happens. When this happens, the developer should <b>clone the
 * <code>LSPQuery</code>. A clone of an
 * <code>LSPQuery</code> is <b>deep</b> for its <code>LSPFilter</code> and
 * <code>LSPRanking</code> (i.e. the clone makes copies of these), and
 * <b>shallow</b> for all other fields. A backend developer should avoid
 * changing anything in a cloned <code>LSPQuery</code> other than filter
 * and ranking.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LSPQuery extends LSPObject implements Cloneable {
    // ------------ FIELDS ------------
    private LSPFilter         filter;
    private LSPRanking        ranking;
    private LSPSource[]       sources;
    private LSPField[]        answerFields;
    private LSPSortByField[]  sortByFields;
    private String            version;
    private String            defaultAttributeSet;
    private boolean           dropStop;
    private double            minDocScore;
    private int               maxDocs;



    // ------------ METHODS ------------
    // -------- CONSTRUCTORS --------
    /**
     * Creates an <code>LSPQuery</code>. Typically, users of the SDARTS API
     * will never need to do this.
     * @param version the version of STARTS being used (1.0)
     * @param dropStop whether the search should try to drop stopwords
     * @param defaultAttributeSet this is nearly always "basic1"
     * @param minDocScore the minimum score for documents to be included
     * in the results
     * @param maxDocs the maximum number of documents to return
     */
    public LSPQuery (String version, boolean dropStop,
                     String defaultAttributeSet, double minDocScore,
                     int maxDocs) {
      this.version = version;
      this.defaultAttributeSet = defaultAttributeSet;
      this.dropStop = dropStop;
      this.minDocScore = minDocScore;
      this.maxDocs = maxDocs;
    }


    // -------- ACCESSORS --------
    /**
     * Sets the <code>LSPFilter</code> of the query. The back-end developer
     * may need this method of the filter has to be altered, due to
     * restrictions of the underlying colelction.
     * @param filter the new filter
     */
    public void setFilter (LSPFilter filter) {
      this.filter = filter;
    }

    /**
     * Returns the <code>LSPFilter</code> used in the query
     * @return the <code>LSPFilter</code> used in the query
     */
    public LSPFilter getFilter () {
      return filter;
    }

    /**
     * Sets the <code>LSPRanking</code> of the query. The back-end developer
     * may need this method of the filter has to be altered, due to
     * restrictions of the underlying colelction.
     * @param ranking the new ranking
     */
    public void setRanking (LSPRanking ranking) {
      this.ranking = ranking;
    }

    /**
     * Returns the <code>LSPRanking</code> used in the query
     * @return the <code>LSPRanking</code> used in the query
     */
    public LSPRanking getRanking () {
      return ranking;
    }

    /**
     * Sets the "sources" that the query should access. These are
     * just another name for subcollections, aka <code>BackEndLSPs</code>.
     * @param sources the sources for the query
     */
    public void setSources (LSPSource[] sources) {
      this.sources = sources;
    }

    /**
     * Returns the sources used in the query.
     * @return the sources used in the query.
     */
    public LSPSource[] getSources () {
      return sources;
    }

    /**
     * Sets the fields to appear in the query's response
     * @param answerFields the fields to appear in the query's response
     */
    public void setAnswerFields (LSPField[] answerFields) {
      this.answerFields = answerFields;
    }

    /**
     * Returns the fields to appear in the query's response
     * @return the fields to appear in the query's response
     */
    public LSPField[] getAnswerFields () {
      return answerFields;
    }

    /**
     * Returns the fields to sort the docs in the query's response
     * @return the fields to sort the docs in the query's response
     */
    public LSPSortByField[] getSortByFields () {
      return sortByFields;
    }

    /**
     * Sets the fields to sort the docs in the query's response
     * @param sortByFields the fields to sort the docs in the query's response
     */
    public void setSortByFields (LSPSortByField[] sortByFields) {
      this.sortByFields = sortByFields;
    }

    /**
     * Returns whether to drop stop words in the query
     * @return whether to drop stop words in the query
     */
    public boolean getDropStop () {
      return dropStop;
    }

    /**
     * Returns the minimum score a document needs to be included
     * in the results
     * @return the minimum score a document needs to be included
     * in the results
     */
    public double getMinDocScore () {
      return minDocScore;
    }

    /**
     * Returns the maximum number of documents to be included
     * in the results
     * @return the maximum number of documents to be included
     * in the results
     */
    public int getMaxDocs () {
      return maxDocs;
    }

    /**
     * Returns what version of protocol is being used. This is always
     * STARTS 1.0
     * @return what version of protocol is being used. This is always
     * STARTS 1.0
     */
    public String getVersion () {
      return version;
    }

    /**
     * Returns what attribute set is being used. This is always
     * "basic1"
     * @return what attribute set is being used. This is always
     * "basic1"
     */
    public String getDefaultAttributeSet () {
      return defaultAttributeSet;
    }



    public void toXML (XMLWriter writer) throws IOException {
      writer.setDefaultFormat();
      writer.setIsolateAttributes (true);
      writer.enterNamespace(STARTS.NAMESPACE_NAME);
      writer.printStartElement ("squery", true);
      writer.printNamespaceDeclaration(STARTS.NAMESPACE_NAME, STARTS.NAMESPACE_VALUE);
	  writer.printAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
	  writer.printAttribute("xsi:schemaLocation", STARTS.NAMESPACE_VALUE + " " + writer.GetDocTypeMapEntry(STARTS.NAMESPACE_NAME + ":smeta-attributes"));
      writer.printAttribute ("version", version);
      writer.printAttribute ("drop-stop", dropStop);
      writer.printAttribute ("default-attr-set", defaultAttributeSet);
      writer.printAttribute ("min-doc-score", minDocScore);
      writer.printAttribute ("max-docs", maxDocs);
      writer.printStartElementClose ();
      writer.setIsolateAttributes (false);

      if (filter != null) {
        writer.indent();
        filter.toXML (writer);
        writer.unindent();
      }
      if (ranking != null) {
        writer.indent();
        ranking.toXML (writer);
        writer.unindent();
      }
      if (sources != null) {
        writer.indent();
        int len = sources.length;
        for (int i = 0 ; i < len ; i++) {
          sources[i].toXML (writer);
        }
        writer.unindent();
      }
      if (answerFields != null) {
        writer.indent();
        writer.printStartElement ("answer-fields");
        writer.indent();
        int len = answerFields.length;
        for (int i = 0 ; i < len ; i++) {
          answerFields[i].toXML (writer);
        }
        writer.unindent();
        writer.printEndElement("answer-fields");
        writer.unindent();
      }
      if (sortByFields != null) {
        writer.indent();
        writer.printStartElement ("sort-by-fields");
        writer.indent();
        int len = sortByFields.length;
        for (int i = 0 ; i < len ; i++) {
          sortByFields[i].toXML (writer);
        }
        writer.unindent();
        writer.printEndElement ("sort-by-fields");
        writer.unindent();
      }
      writer.printEndElement("squery");
      writer.exitNamespace();
    }

    public Object clone() {
      LSPQuery query = new LSPQuery (version, dropStop, defaultAttributeSet,
                                     minDocScore, maxDocs);
      // Deep clone of filter
      if (filter != null) {
        query.setFilter ((LSPFilter) filter.clone());
      }

      // Deep clone of ranking
      if (ranking != null) {
        query.setRanking ((LSPRanking) ranking.clone());
      }

      // Shallow clones of all else
      query.setSources (sources);
      query.setAnswerFields (answerFields);
      query.setSortByFields (sortByFields);

      return query;
    }
}


