

package edu.columbia.cs.sdarts.common;

import java.io.IOException;

import edu.columbia.cs.sdarts.util.XMLWriter;


/**
 * Represents a "filter", as defined in STARTS 1.0. A filter is used to specify
 * the actual filtering terms of the search.
 * <p>
 * Sometimes, a back-end developer
 * need never instantiate an <code>LSPFilter</code>, but simply read
 * one that has been passed from the front end inside an
 * {@link edu.columbia.cs.sdarts.common.LSPQuery LSPQuery}. The
 * {@link edu.columbia.cs.sdarts.common.LSPResults LSPResults}
 * object that the back end returns must include the actual filter used.
 * <p>
 * In the case where the wrapped collection does not fully support the
 * requested filter, the back end developer must instantiate a new
 * filter that represents the supported subset of the requested filter,
 * and include it in the <code>LSPResults</code> object. See
 * {@link edu.columbia.cs.sdarts.backend.QueryProcessor} and
 * {@link edu.columbia.cs.sdarts.backend.StandardQueryProcessor} for help in doing this.
 * An <code>LSPFilter</code> can be altered using the various
 * <code>set()</code> methods described below. In addition, an
 * <code>LSPFilter</code> is <code>Cloneable</code>; in this way, a
 * parent <code>LSPQuery</code> can be cloned, and the filter in the clone
 * can be manipulated directly using the <code>set()</code> methods.
 * An <code>LSPFilter</code> makes only a shallow clone.
 * <p>
 * A filter can be of three types:<br>
 * <ul>
 * <li><code>TERM</code>: a single {@link edu.columbia.cs.sdarts.common.LSPTerm LSPTerm}
 * <li><code>TERM_PROXOP_TERM</code>: an <code>LSPTerm</code>, followed by an
 * {@link edu.columbia.cs.sdarts.common.LSPProxOp LSPProxOp}, followed by an
 * <code>LSPTerm</code>.
 * <li><code>FILTER_BOOLEANOP_FILTER</code>: an <code>LSPFilter</code>,
 * followed by an {@link edu.columbia.cs.sdarts.common.LSPBooleanOp LSPBooleanOp},
 * followed by an <code>LSPFilter</code>.
 * </ul>
 * Accessors for the type, and for all possible elements within the
 * filter, are included in this class.
 * <p>
 * @author <a href="mailto:ngreen@cs.columbia.edu">Noah Green</a>
 * @version 1.0
 */
public class LSPFilter extends LSPObject implements Cloneable {
    // ------------ FIELDS ------------
    public static final int TERM                     = 0;
    public static final int TERM_PROXOP_TERM         = 1;
    public static final int FILTER_BOOLEANOP_FILTER  = 2;
    public int type = TERM;

    public LSPTerm term1;
    public LSPProxOp proxOp;
    public LSPTerm term2;

    public LSPFilter filter1;
    public LSPBooleanOp booleanOp;
    public LSPFilter filter2;

    // ------------ METHODS ------------
    // -------- CONSTRUCTORS --------
    /**
     * Creates an instance of type <code>TERM</code>
     * @param term the one term in the filter
     */
    public LSPFilter (LSPTerm term) {
      set (term);
    }

    /**
     * Creates an instance of type <code>TERM_PROXOP_TERM</code>
     * @param term1 the first term
     * @param proxOp the proximity operator
     * @param term2 the second term
     */
    public LSPFilter (LSPTerm term1, LSPProxOp proxOp, LSPTerm term2) {
      set (term1, proxOp, term2);
    }

    /**
     * Creates an instance of type <code>FILTER_BOOLEANOP_FILTER</code>
     * @param filter1 the first filter
     * @param booleanOp the boolean operator
     * @param filter2 the second filter
     */
    public LSPFilter (LSPFilter filter1, LSPBooleanOp booleanOp,
                       LSPFilter filter2) {
      set (filter1, booleanOp, filter2);
    }


    // -------- ACCESSORS --------
    /**
     * Return the type of filter this is: either <code>TERM</code>,
     * <code>TERM_PROXOP_TERM</code>, or <code>FILTER_BOOLEANOP_FILTER</code>.
     * @return the type of filter this is: either <code>TERM</code>,
     * <code>TERM_PROXOP_TERM</code>, or <code>FILTER_BOOLEANOP_FILTER</code>.
     */
    public int getType () {
      return type;
    }

    /**
     * Return the <code>LSPTerm</code> associated with this filter,
     * if type is <code>TERM</code>. If type is <code>TERM_PROXOP_TERM</code>,
     * will return the first LSPTerm. Else, returns <code>null</code>.
     * @return the <code>LSPTerm</code> associated with this filter,
     * if type is <code>TERM</code>. If type is <code>TERM_PROXOP_TERM</code>,
     * will return the first LSPTerm. Else, returns <code>null</code>.
     */
    public LSPTerm getTerm () {
      return term1;
    }


    /**
     * Return the <code>LSPTerm</code> associated with this filter,
     * if type is <code>TERM</code>. If type is <code>TERM_PROXOP_TERM</code>,
     * will return the first LSPTerm. Else, returns <code>null</code>.
     * @return the <code>LSPTerm</code> associated with this filter,
     * if type is <code>TERM</code>. If type is <code>TERM_PROXOP_TERM</code>,
     * will return the first LSPTerm. Else, returns <code>null</code>.
     */
    public LSPTerm getFirstTerm () {
      return term1;
    }

    /**
     * Return the <code>LSPProxOp</code> associated with this filter,
     * if type is <code>TERM_PROXOP_TERM</code>. Else, returns
     * <code>null</code>.
     * @return the <code>LSPProxOp</code> associated with this filter,
     * if type is <code>TERM_PROXOP_TERM</code>. Else, returns
     * <code>null</code>.
     */
    public LSPProxOp getProxOp () {
      return proxOp;
    }

    /**
     * Return the second <code>LSPTerm</code> associated with this filter,
     * if type is <code>TERM_PROXOP_TERM</code>. Else, returns
     * <code>null</code>.
     * @return the second <code>LSPTerm</code> associated with this filter,
     * if type is <code>TERM_PROXOP_TERM</code>. Else, returns
     * <code>null</code>.
     */
    public LSPTerm getSecondTerm () {
      return term2;
    }

    /**
     * Return the first <code>LSPFilter</code> associated with this filter,
     * if type is <code>FILTER_BOOLEANOP_FILTER</code>. Else, returns
     * <code>null</code>.
     * @return the first <code>LSPFilter</code> associated with this filter,
     * if type is <code>FILTER_BOOLEANOP_FILTER</code>. Else, returns
     * <code>null</code>.
     */
    public LSPFilter getFirstFilter () {
      return filter1;
    }

    /**
     * Return the <code>LSPBooleanOp</code> associated with this filter,
     * if type is <code>FILTER_BOOLEANOP_FILTER</code>. Else, returns
     * <code>null</code>.
     * @return the <code>LSPBooleanOp</code> associated with this filter,
     * if type is <code>FILTER_BOOLEANOP_FILTER</code>. Else, returns
     * <code>null</code>.
     */
    public LSPBooleanOp getBooleanOp () {
      return booleanOp;
    }

    /**
     * Return the second <code>LSPFilter</code> associated with this filter,
     * if type is <code>FILTER_BOOLEANOP_FILTER</code>. Else, returns
     * <code>null</code>.
     * @return the second <code>LSPFilter</code> associated with this filter,
     * if type is <code>FILTER_BOOLEANOP_FILTER</code>. Else, returns
     * <code>null</code>.
     */
    public LSPFilter getSecondFilter () {
      return filter2;
    }

    /**
     * Convert the <code>LSPFilter</code> into a filter of type
     * <code>TERM</code>
     * @param term the term in the filter
     */
    public void set (LSPTerm term) {
      if (term == null) {
        throw new IllegalArgumentException ("null parameter");
      }
      this.term1 = term;
      this.type = TERM;
      term2 = null;
      proxOp = null;
      booleanOp = null;
      filter1 = null;
      filter2 = null;
    }

    /**
     * Convert the <code>LSPFilter</code> into a filter of type
     * <code>TERM_PROXOP_TERM</code>
     * @param term1 the first term
     * @param proxOp the proximity operator
     * @param term2 the second term
     */
    public void set (LSPTerm term1, LSPProxOp proxOp, LSPTerm term2) {
      if (term1 == null || proxOp == null || term2 == null) {
        throw new IllegalArgumentException ("null parameter");
      }
      this.term1 = term1;
      this.proxOp = proxOp;
      this.term2 = term2;
      this.type = TERM_PROXOP_TERM;
      booleanOp = null;
      filter1 = null;
      filter2 = null;
    }

    /**
     * Convert the <code>LSPFilter</code> into a filter of type
     * <code>FILTER_BOOLEANOP_FILTER</code>
     * @param filter1 the first filter
     * @param booleanOp the boolean operator
     * @param filter2 the second filter
     */
    public void set (LSPFilter filter1, LSPBooleanOp booleanOp,
                     LSPFilter filter2 ) {
      if (filter1 == null || booleanOp == null || filter2 == null) {
        throw new IllegalArgumentException ("null parameter");
      }
      this.filter1 = filter1;
      this.booleanOp = booleanOp;
      this.filter2 = filter2;
      this.type = FILTER_BOOLEANOP_FILTER;
      term1 = null;
      proxOp = null;
      term2 = null;
    }

    public void toXML (XMLWriter writer) throws IOException {
      writer.setDefaultFormat();
      writer.enterNamespace(STARTS.NAMESPACE_NAME);
      writer.printStartElement ("filter");
      writer.indent();
      switch (type) {
        case TERM:
          term1.toXML (writer);
        break;

        case TERM_PROXOP_TERM:
          term1.toXML (writer);
          proxOp.toXML (writer);
          term2.toXML (writer);
        break;

        case FILTER_BOOLEANOP_FILTER:
          filter1.toXML (writer);
          booleanOp.toXML (writer);
          filter2.toXML (writer);
        break;
      }
      writer.unindent();
      writer.printEndElement ("filter");
      writer.flush();
      writer.exitNamespace();
    }

    public Object clone () {
      LSPFilter newFilter = null;
      switch (type) {
        case TERM:
        newFilter = new LSPFilter (term1);
        break;

        case TERM_PROXOP_TERM:
        newFilter = new LSPFilter (term1, proxOp, term2);
        break;

        case FILTER_BOOLEANOP_FILTER:
        newFilter = new LSPFilter (filter1, booleanOp, filter2);
        break;
      }
      return newFilter;
    }
}
