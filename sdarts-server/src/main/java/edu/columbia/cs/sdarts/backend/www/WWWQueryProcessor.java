

package edu.columbia.cs.sdarts.backend.www;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.SAXException;

import edu.columbia.cs.sdarts.backend.BackEndException;
import edu.columbia.cs.sdarts.backend.LSPDocSorter;
import edu.columbia.cs.sdarts.backend.StandardQueryProcessor;
import edu.columbia.cs.sdarts.common.LSPDoc;
import edu.columbia.cs.sdarts.common.LSPField;
import edu.columbia.cs.sdarts.common.LSPMetaAttributes;
import edu.columbia.cs.sdarts.common.LSPQuery;
import edu.columbia.cs.sdarts.common.LSPResults;
import edu.columbia.cs.sdarts.common.LSPSortByField;
import edu.columbia.cs.sdarts.common.LSPSource;
import edu.columbia.cs.sdarts.util.HTTPRequest;

/**
 * Manages the querying of the web search engine that is being wrapped.
 * Each time a query comes in, the following happens:
 * <ul>
 * <li>The query is modified to support only the field/modifier combinations
 * mentioned in the meta-attributes (same as the superclass,
 * {@link edu.columbia.cs.sdarts.backend.StandardQueryProcessor StandardQueryProcessor}
 * <li>Validates the query using the following rules:
 * <ul>
 * <li>Filter and ranking presence must match the "query-parts-supported"
 * element of the meta-attributes (same as <code>StandardQueryProcessor</code>),
 * <li> "sort-by-fields" must be a subset of "answer-fields"
 * </ul>
 * <li>Translates the query into a CGI request, using the
 * {@link edu.columbia.cs.sdarts.backend.www.WWWQueryTranslator WWWQueryTranslator}.
 * <li>Creates a {@link edu.columbia.cs.sdarts.backend.www.WWWSession WWWSession}, which
 * invokes the CGI request, and
 * converts incoming HTML result pages into {@link edu.columbia.cs.sdarts.common.LSPDoc LSPDoc}
 * collections.
 * <li>The session is continually refreshed until the site can no longer provide
 * any more hits (i.e. there is no "more" button at the bottom of the returned
 * HTML page), or the "maxDocs" attribute of the <code>LSPQuery</code> has
 * been exceeded.
 * <li>Performs the following post-processing:
 * <ul>
 * <li>Eliminates documents whose score falls below the "minScore" attribute
 * of the <code>LSPQuery</code>
 * <li>Shows only the requested "answer-fields" (<b>Note:</b> This actually
 * happens earlier, but we mention it here so you can see all the post-processing
 * activities together.)
 * <li>Sorts the documents using an {@link edu.columbia.cs.sdarts.backend.LSPDocSorter LSPDocSorter}.
 * </ul>
 * <li>Packages everything as an {@link edu.columbia.cs.sdarts.common.LSPResults LSPResults} and
 * returns it.
 * </ul>
 * A developer should pay careful attention to what we said happens in
 * post-processing, so that they do not waste time trying to duplicate these
 * steps when writing their <code>www_results.xsl</code> file.
 * @see edu.columbia.cs.sdarts.backend.www.WWWQueryTranslator
 * @see edu.columbia.cs.sdarts.backend.www.WWWSession
 * @see edu.columbia.cs.sdarts.backend.LSPDocSorter
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @author <i>modified by:</i> <a href="mailto:jb605@cs.columbia.edu">Jiangcheng Bao</a>
 * @version 1.0
 */
public class WWWQueryProcessor extends StandardQueryProcessor {
  private String              backEndLSPName;
  private WWWQueryTranslator  queryTranslator;
  private LSPMetaAttributes   metaAttributes;

  /**
   * Construct a <code>WWWQueryProcessor</code> that uses an
   * <code>LSPMetaAttributes</code> object to tell it what fields,
   * modifiers, and field/modifier combinations are legal. In addition,
   * the class needs to know the name of the <code>BackEndLSP</code> that
   * is using it, in order to figure out the path to the directory where
   * the needed stylesheets are stored.
   * @param backEndLSPName the name of the <code>BackEndLSP</code> using
   * this query processor
   * @param metaAttributes the <code>LSPMetaAttributes</code> object
   * used to tell the class what fields, modifiers, and field/modifier
   * combinations are legal
   */
  public WWWQueryProcessor (String backEndLSPName, LSPMetaAttributes metaAttributes)
    throws BackEndException {
    super (metaAttributes);
    this.metaAttributes  = metaAttributes;
    this.backEndLSPName  = backEndLSPName;
    this.queryTranslator = new WWWQueryTranslator (backEndLSPName);
  }

  /**
   * Performs a query on the web site. Translates the <code>LSPQuery</code>
   * into a CGI request using the
   * {@link edu.columbia.cs.sdarts.backend.www.WWWQueryTranslator WWWQueryTranslator}.
   * Instantiates a {@link edu.columbia.cs.sdarts.backend.www.WWWSession WWWSession} and
   * calls <code>getDocs()</code> on it, until the session runs out of documents
   * or the maximum required number of documents has been retrieved.
   * @param query the <code>LSPQuery</code> to perform
   */
  protected final LSPResults queryImpl (LSPQuery query) {
    LSPDoc[] docs = new LSPDoc[0];
    int count = 0;
    try {
      // Set up session
      HTTPRequest request = queryTranslator.translate(query);
      LSPField[] answerFields = query.getAnswerFields();
      if (answerFields == null) {
        answerFields = metaAttributes.getFieldsSupported();
      }
      WWWSession session =
        new WWWSession (backEndLSPName, request,
                        query.getMaxDocs(), answerFields);

      List docList = new ArrayList (query.getMaxDocs());
      while (!session.isEmpty()) {
        LSPDoc[] newDocs;
        try {
          newDocs = session.getDocs();
        } catch (Exception e) {
          e.printStackTrace(System.err);
          System.err.println(e.toString());
          newDocs = new LSPDoc[0];
        }
        if (newDocs == null || newDocs.length == 0) {
          /* comments  and modification by Jiangcheng Bao
           * why do we break when no docs get in this try?
           * because if there is a more button, the page must have some results
           *
           * but when we come to multi-level results
           * the higher level may have no results
           * thus, we should not break when no new docs get in this try
           * we should continue, until session.isEmpty() returns true
           *
           */
          continue;
        }
        List l = Arrays.asList (newDocs);
        docList.addAll (l);
      }

      docs = postProcess (docList, query);
      count = session.getNumAvailable();

    } catch (MalformedURLException e) {
      e.printStackTrace(System.err);
    } catch (SAXException e) {
      e.printStackTrace(System.err);
    } catch (BackEndException e) {
      e.printStackTrace(System.err);
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }

    return new LSPResults (query.getFilter(), query.getRanking(),
                           new LSPSource (backEndLSPName), docs,
                           count);
  }

  /**
   * Performs the same test as the method it overrides: checks against
   * the <code>queryPartsSupported</code> property of the
   * <code>LSPMetaAttributes</code>. In addition, it will throw an
   * exception if any of the "sort-by-fields" are not part of the
   * "answer-fields". (If "answer-fields" is <code>null</code>, then
   * it is replaced by all legal fields, as defined in the meta-attributes,
   * during the test. Also, note that "score" is a legal sort-by-field
   * no matter what.)
   */
  protected void validateQuery (LSPQuery query) throws BackEndException {
    super.validateQuery (query);
    LSPField[] answerFields = query.getAnswerFields();
    LSPSortByField[] sortByFields = query.getSortByFields();
    if (answerFields == null) {
      answerFields = getMetaAttributes().getFieldsSupported();
    }

    if (sortByFields != null) {
      int slen = sortByFields.length;
      int alen = answerFields.length;

      for (int i = 0 ; i < slen ; i++) {
        if (sortByFields[i].isScore()) continue;

        boolean foundMatch = false;
        LSPField sortByField = sortByFields[i].getField();
        for (int j = 0 ; j < alen ; j++) {
          if (sortByField.equals(answerFields[j])) {
            foundMatch = true;
            break;
          }
        }

        if (!foundMatch) {
          throw new BackEndException
            ("Sort-by-fields must be subset of answer-fields");
        }
      }
    }
  }

  private LSPDoc[] postProcess (List docList, LSPQuery query) {
    // Get all needed fields
    double minScore = query.getMinDocScore();
    final LSPSortByField[] sortByFields = query.getSortByFields();

    // remove below minscore
    for (Iterator it = docList.iterator() ; it.hasNext() ; ) {
      LSPDoc doc = (LSPDoc) it.next();
      if (doc.getRawScore() < minScore) {
        it.remove();
      }
    }

    // sort
    if (sortByFields != null) {
      LSPDocSorter.sortLSPDocs (docList, sortByFields);
    }

    return (LSPDoc[]) docList.toArray(new LSPDoc[0]);
  }
}
