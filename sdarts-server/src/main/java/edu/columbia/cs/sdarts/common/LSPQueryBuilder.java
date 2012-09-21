
package edu.columbia.cs.sdarts.common;

import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import org.xml.sax.AttributeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import sdlip.SDLIPException;
import edu.columbia.cs.sdarts.util.IntStack;
import edu.columbia.cs.sdarts.util.SDARTSHandlerBase;
import edu.columbia.cs.sdarts.util.UnsynchStack;

import edu.columbia.cs.sdarts.frontend.SDARTSConfigBuilder;
import edu.columbia.cs.sdarts.frontend.SDARTSConfig;
import edu.columbia.cs.sdarts.common.STARTS;

/**
 * Creates an {@link edu.columbia.cs.sdarts.common.LSPQuery LSPQuery} from incoming STARTS XML.
 * Typically, this XML is being read by the
 * {@link edu.columbia.cs.sdarts.frontend.FrontEndLSP FrontEndLSP} from one of the parameters
 * of its <code>search()</code> method. As it says in the <code>FrontEndLSP</code>,
 * some information from the other SDLIP parameters must be merged with
 * the information in the STARTS header. In particular:
 * <ul>
 * <li>The "sources" in the <code>LSPQuery</code>, that is the,
 * {@link edu.columbia.cs.sdarts.backend.BackEndLSP BackEndLSPs} to be searched, are a union
 * of the "sources" in the STARTS XML, and the "subcollections" parameter from
 * the SDLIP method call.
 * <li>The number of documents to be returned is the MAX of the
 * "max-docs" attribute of the STARTS XML, and the "numdocs" parameter from the
 * SDLIP method call.
 * </ul>
 * To achieve this policy, the <code>LSPQueryBuilder</code> simply accepts
 * the STARTS XML, the SDLIP "subcollections", and the SDLIP "numdocs" in
 * its factory method, and performs the necessary logic when creating the
 * <code>LSPQuery</code>.
 *
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @author <i>modified by:</i> <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 * @version 1.0
 */
public class LSPQueryBuilder {
  /**
   * Creates an <code>LSPQuery</code> from incoming STARTS XML and
   * the relevant SDLIP parameters.
   * @param reader the <code>Reader</code> from which the STARTS XML
   * can be read
   * @param numDocs the "numdocs" parameter from <code>sdlip.Search.search()</code>
   * @param subCols the "subcollections" parameter from <code>sdlip.Search.search()</code>
   */
  public static LSPQuery fromXML (Reader reader,
                                  int numDocs,
                                  String[] subCols)
    throws SDLIPException, javax.xml.parsers.ParserConfigurationException {
      LSPQuery query = null;
      try {
        LQBHandler handler = new LQBHandler (numDocs, subCols);
        query = handler.parse (reader);
      }
      catch (IOException e) {
        throw new SDLIPException (SDLIPException.SERVER_ERROR_EXC, e.getMessage());
      }
      catch (SAXException e) {
        e.printStackTrace();
        throw new SDLIPException (SDLIPException.MALFORMED_XML_EXC, e.getMessage());
      }
      return query;
  }

  private static class LQBHandler extends SDARTSHandlerBase {
    // ------------ FIELDS ------------
    // -------- CONSTANTS --------
    private static final int NONE            = -1;
    private static final int STARTS_HEADER   = 0;
    private static final int SQUERY          = 1;
    private static final int FILTER          = 2;
    private static final int RANKING         = 3;
    private static final int SOURCE          = 4;
    private static final int ANSWER_FIELDS   = 5;
    private static final int SORT_BY_FIELDS  = 6;
    private static final int TERM            = 7;
    private static final int PROX_OP         = 8;
    private static final int BOOLEAN_OP      = 9;
    private static final int FIELD           = 10;
    private static final int SORT_BY_FIELD   = 11;
    private static final int SCORE           = 12;
    private static final int MODIFIER        = 13;
    private static final int VALUE           = 14;

    // -------- PARSING --------
    IntStack state   = new IntStack();
    LSPQuery query;
    UnsynchStack filters       = new UnsynchStack();
    UnsynchStack rankings      = new UnsynchStack();
    UnsynchStack proxOps       = new UnsynchStack();
    UnsynchStack booleanOps    = new UnsynchStack();
    UnsynchStack sources       = new UnsynchStack();
    UnsynchStack answerFields  = new UnsynchStack();
    UnsynchStack sortByFields  = new UnsynchStack();
    UnsynchStack fields        = new UnsynchStack();
    UnsynchStack modifiers     = new UnsynchStack();
    LSPValue     value;
    UnsynchStack terms         = new UnsynchStack();
    LSPProxOp    proxOp;
    UnsynchStack booleanOp = new UnsynchStack();
    int      numDocs;
    String[] subCols;


    // ------------ METHODS ------------
    // -------- CONSTRUCTOR --------
    LQBHandler (int numDocs, String[] subCols) throws SAXException, javax.xml.parsers.ParserConfigurationException 
    {
      this.numDocs = numDocs;
      this.subCols = subCols;
      state.push (NONE);
    }

    // -------- CONSTRUCTOR --------
    LSPQuery parse (Reader reader) throws IOException, SAXException, SDLIPException
    {
		SDARTSConfig config = SDARTSConfigBuilder.fromXML();
    	
		super.EnableSchemaChecking(true);
		super.EnableValidation(true);
		super.setSchemaLocation(STARTS.NAMESPACE_VALUE, config.getStartsDtdURL());
		super.parse (new InputSource (reader));
		
		return query;
    }

    // -------- DOCUMENT HANDLER INTERFACE --------
    public void startElement (String name, AttributeList atts)
	throws SAXException {
        name = super.removeNamespace(name);
        atts = super.removeNamespace(atts);

	    try {
            if (name.equals ("starts-header")) {
              state.push (STARTS_HEADER);
            }
            else if (name.equals ("squery")) {
              state.push (SQUERY);
              boolean dropStop = true;
              try {
                dropStop = new Boolean (atts.getValue ("drop-stop")).
                            booleanValue();
              } catch (Exception e) {}

              double minScore = 0.0;
              try {
                minScore = new Double (atts.getValue ("min-doc-score")).
                            doubleValue();
              } catch (Exception e) {}

              // Note how we take max of STARTS and SDLIP parameter
              // If STARTS parameter is blank, use SDLIP parameter
              int maxDocs = 0;
              try {
                maxDocs = new Integer (atts.getValue ("max-docs")).intValue();
                maxDocs = Math.max (maxDocs, numDocs);
              }
              catch (Exception e) {
                maxDocs = numDocs;
              }

              String version = "Starts 1.0";
              try {
                if ( atts.getValue ("version") != null )
                	version = atts.getValue ("version");
              } catch (Exception e) {}

              String defaultAttrSet = "basic-1";
              try {
              	if ( atts.getValue ("default-attr-set") != null )
                	defaultAttrSet = atts.getValue ("default-attr-set");
              } catch (Exception e) {}

              query =
                new LSPQuery (version, dropStop,
                              defaultAttrSet, minScore, maxDocs);
            }
            else if (name.equals ("filter")) {
              state.push (FILTER);
              booleanOp.push(null);
            }
            else if (name.equals ("ranking")) {
              state.push (RANKING);
            }
            else if (name.equals ("source")) {
              state.push (SOURCE);
            }
            else if (name.equals ("answer-fields")) {
              state.push (ANSWER_FIELDS);
            }
            else if (name.equals ("sort-by-fields")) {
              state.push (SORT_BY_FIELDS);
            }
            else if (name.equals ("sort-by-field")) {
              state.push (SORT_BY_FIELD);
              LSPSortByField sortByField =
                new LSPSortByField (atts.getValue ("ascending-descending"));
              sortByFields.push (sortByField);
            }
            else if (name.equals ("score")) {
              state.push (SCORE);
              LSPSortByField sortByField =
                new LSPSortByField ();
              sortByFields.push (sortByField);
            }
            else if (name.equals ("field")) {
              state.push (FIELD);
              String typeSet= "basic1";
              try {
              	if ( atts.getValue ("type-set") != null ) 
                	typeSet = atts.getValue ("type-set");
              } catch (Exception e) {}

              String fieldName = atts.getValue ("name");
              fields.push (new LSPField(typeSet, fieldName));
            }
            else if (name.equals ("modifier")) {
              state.push (MODIFIER);
              String typeSet= "basic1";
              try {
				if ( atts.getValue ("typeset") != null ) 
                	typeSet = atts.getValue ("typeset");
              }
              catch (Exception e) {}

              modifiers.push (new LSPModifier (typeSet,atts.getValue("name")));
            }
            else if (name.equals ("value")) {
              state.push (VALUE);
            }
            else if (name.equals ("term")) {
              state.push (TERM);
              double weight = -1;
              try {
                weight = new Double (atts.getValue ("weight")).doubleValue();
              } catch (Exception e) {}

              LSPTerm term = null;
              if (weight != -1) {
                term = new LSPTerm (weight);
              }
              else {
                term = new LSPTerm();
              }
              terms.push (term);
            }
            else if (name.equals ("prox-op")) {
              state.push (PROX_OP);
              proxOp = new LSPProxOp (
                new Integer (atts.getValue ("proximity")).intValue(),
                new Boolean (atts.getValue ("word-order-matters")).
                              booleanValue());
            }
            else if (name.equals ("boolean-op")) {
              state.push (BOOLEAN_OP);
				booleanOp.push(new LSPBooleanOp (atts.getValue ("name")));
            }
            else {
              throw new SAXException (name);
            }
	    }
  	    catch (Exception e) {
            e.printStackTrace();
	        processException ("startElement ("+name+"): " + e.getMessage());
	    }
    }

    public void characters (char[] ch, int start, int length)
	throws SAXException {
        String value = new String (ch, start, length);
        switch (state.peek()) {
          case SOURCE:
            sources.push (new LSPSource (value));
          break;

          case VALUE:
            this.value = new LSPValue (value);
          break;
        }
    }

    public void endElement (String name) throws SAXException {
      int currentState  = state.pop();
      int previousState = state.peek();

      switch (currentState) {
          case SQUERY:
            if (!filters.empty()) {
              query.setFilter ((LSPFilter) filters.pop());
            }
            if (!rankings.empty()) {
              query.setRanking ((LSPRanking) rankings.pop());
            }
            // sources are union of SDLIP and STARTS
            Set sourceSet = new HashSet ();
            sourceSet.addAll (sources);
            if (subCols != null) {
              int len = subCols.length;
              for (int i = 0 ; i < len ; i++) {
                sourceSet.add (new LSPSource (subCols[i]));
              }
            }
            query.setSources
              ((LSPSource[]) sourceSet.toArray(new LSPSource[0]));
            sources.clear();
            if (!answerFields.empty()) {
              query.setAnswerFields
                ((LSPField[]) answerFields.toArray (new LSPField[0]));
              answerFields.clear();
            }
            if (!sortByFields.empty()) {
              query.setSortByFields
                ((LSPSortByField[]) sortByFields.toArray
                  (new LSPSortByField[0]));
              sortByFields.clear();
            }
          break;

          case FILTER:

            LSPFilter filter = null;
            LSPBooleanOp boolOp;
			  if ((boolOp=(LSPBooleanOp)booleanOp.pop()) != null ) {
              LSPFilter filter2 = (LSPFilter) filters.pop();
              LSPFilter filter1 = (LSPFilter) filters.pop();
              filter =
                new LSPFilter (filter1, boolOp, filter2);
            booleanOp.pop();
            }
            else if (proxOp != null) {
              LSPTerm term2 = (LSPTerm) terms.pop();
              LSPTerm term1 = (LSPTerm) terms.pop();

              filter =
                new LSPFilter (term1, proxOp, term2);
              proxOp = null;
            }
            else {
              filter = new LSPFilter ((LSPTerm) terms.pop());

            }
            filters.push (filter);
          break;

          case RANKING:
            LSPRanking ranking = null;
            if ( (rankings.peek() != null) && (rankings.peek(1) != null) ) {
              LSPRanking ranking2 = (LSPRanking) rankings.pop();
              LSPRanking ranking1 = (LSPRanking) rankings.pop();
              ranking =
                //new LSPRanking (ranking1, booleanOp, ranking2);
                new LSPRanking (ranking1, (LSPBooleanOp)booleanOp.pop(), ranking2);
              booleanOp = null;
            }
            else if (proxOp != null) {
              LSPTerm term2 = (LSPTerm) terms.pop();
              LSPTerm term1 = (LSPTerm) terms.pop();

              ranking =
                new LSPRanking (term1, proxOp, term2);
              proxOp = null;
            }
            else {
              ranking =
                new LSPRanking ((LSPTerm[]) terms.toArray(new LSPTerm[0]));
              terms.clear();
            }
            rankings.push (ranking);
          break;

          case TERM:
            LSPTerm term = (LSPTerm) terms.peek();
            if (!fields.isEmpty()) {
              term.setField ((LSPField) fields.pop());
            }
            if (!modifiers.isEmpty()) {
              term.setModifiers ((LSPModifier[]) modifiers.toArray(new LSPModifier[0]));
              modifiers.clear();
            }
            term.setValue (value);
            value = null;
          break;

          case ANSWER_FIELDS:
            answerFields.addAll (fields);
            fields.clear();
          break;

          case SORT_BY_FIELD:
            LSPSortByField sbf = (LSPSortByField) sortByFields.peek();
            LSPField f = (LSPField) fields.pop();
            sbf.setField (f);
          break;

          default:
          break;
        }

      currentState = previousState;
    }
  }
}
